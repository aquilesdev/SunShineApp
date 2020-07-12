package com.example.android.sunshine.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.R;

import java.util.ArrayList;
import java.util.Arrays;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;

    private ArrayAdapter<String> forecastAdapter;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        ArrayList<String> weekForecast = new ArrayList<String>();


        weekForecast.addAll( Arrays.asList(
                "Seg - Ensolarado - 40/32",
                "Ter - Frio - 32/02",
                "Qua - Húmido - 24/03",
                "Qui - Ensolarado - 33/30",
                "Sex - Ensorado - 23/21",
                "Sab - Chuva - 15/09",
                "Dom - Sereno - 16/10"));

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

}
