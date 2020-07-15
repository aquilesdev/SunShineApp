package com.example.android.sunshine.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ForecastFragment extends Fragment {

    private MainViewModel mViewModel;

    private ArrayAdapter<String> forecastAdapter;

    public static ForecastFragment newInstance() {
        return new ForecastFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.forecast_fragment, container, false);


        ArrayList<String> weekForecast = new ArrayList<String>();

        forecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast
        );

        ListView listViewForecast = (ListView)rootView.findViewById(R.id.list_view_forecast);

        listViewForecast.setAdapter(forecastAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment_menu, menu);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_refresh){

            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute();

            return  true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }




    public class FetchWeatherTask extends AsyncTask<Void, Void,String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... voids) {


            BufferedReader reader = null;
            String forecastJsonStr = "";
            HttpURLConnection urlConnection = null;

            String lat ="8.83682";
            String lon ="13.23432";
            String units ="metrics";
            String API_KEY = "0f3a537550556f99cf31b92f723423d1";
            String EXCLUDE_VALUE = "hourly,minutely";
            int NUM_DAYS = 8;

            try
            {
                final String FORECAST_BASE_URL = "https://api.openweathermap.org/data/2.5/onecall";
                final String LAT_PARAM = "lat";
                final String LON_PARAM = "lon";
                final String UNIT_PARAM = "units";
                final String APPID_PARAM = "appid";
                final String EXCLUDE_PARAM ="exclude";


                Uri buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                  .appendQueryParameter(LAT_PARAM, lat)
                                  .appendQueryParameter(LON_PARAM, lon)
                                  .appendQueryParameter(EXCLUDE_PARAM, EXCLUDE_VALUE)
                                  .appendQueryParameter(UNIT_PARAM, units)
                                  .appendQueryParameter(APPID_PARAM, API_KEY)
                                  .build();

                URL url = new URL(buildUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while((line = reader.readLine())!=null){
                    stringBuffer.append(line + "\n");
                }

                if(stringBuffer.length() == 0){
                    return null;
                }

                forecastJsonStr = stringBuffer.toString();
                Log.d(LOG_TAG, "Forecast json string: " + forecastJsonStr);


            }
            catch(IOException ex){
                Log.e(LOG_TAG, ex.getMessage(), ex);

            }
            finally {
                if(urlConnection != null ){
                    urlConnection.disconnect();
                }

                if(reader!=null){

                    try{
                        reader.close();
                    }
                    catch (final IOException ex){
                        Log.e(LOG_TAG, "Error closing stream", ex);
                    }
                }

            }

            try{
                return getWeatherDataFromJson(forecastJsonStr, NUM_DAYS);
            }
            catch (JSONException ex){
                Log.e(LOG_TAG, ex.getMessage(),ex);
                ex.printStackTrace();
            }

            return  new String[NUM_DAYS];

        }


        private String getReadableDateString(Calendar calendar){


            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EE MM dd");

            String weekDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT_FORMAT, Locale.US);
            String month = calendar.getDisplayName(Calendar.MONDAY,Calendar.SHORT_FORMAT, Locale.US);

            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return weekDay + ", " + month + " " + day;
        }

        private String formatHighLows(double high, double low){

            long roundHigh = Math.round(high);
            long roundLow  = Math.round(low);

            String highLowStr = roundHigh +"/" + roundLow;
            return highLowStr;
        }


        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {

            final String OWM_DAILY = "daily";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_DAILY);

            Calendar dayTime = Calendar.getInstance();

            int startDay = dayTime.get(Calendar.DATE);

            String[] resultStr = new String[numDays];



            for(int i = 0; i < numDays; i++){

                String day;
                String description;
                String highAndlow;
                Calendar calendar = Calendar.getInstance();

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime;
                calendar.set(Calendar.DATE, startDay+i);

                dateTime = calendar.get(Calendar.DAY_OF_WEEK);

                Log.v(LOG_TAG, "Datetime" + dateTime);

                day = getReadableDateString(calendar);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temp = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double max = temp.getDouble(OWM_MAX);
                double min = temp.getDouble(OWM_MIN);

                highAndlow = formatHighLows(max,min);

                resultStr[i] = day + " - " + description + " - " + highAndlow;


            }

            for(String s: resultStr){
                Log.v(LOG_TAG, "Forecast entry" + s);
            }

            return  resultStr;

        }

        @Override
        protected void onPostExecute(String[] result) {

            if(result!=null){

                forecastAdapter.clear();
                for (String s: result) {
                    forecastAdapter.add(s);
                }
            }


        }
    }

}
