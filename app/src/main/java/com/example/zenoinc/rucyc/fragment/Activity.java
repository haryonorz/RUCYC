package com.example.zenoinc.rucyc.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.activity.ActivityResult;
import com.example.zenoinc.rucyc.service.MulaiActivity;
import com.example.zenoinc.rucyc.service.StartActivity;

public class Activity extends Fragment {
    private CardView running, cycling;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_choose_activity, container, false);
        running = view.findViewById(R.id.running_layout);
        cycling = view.findViewById(R.id.cycling_layout);

        running.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),StartActivity.class);
                i.putExtra("typeActivity", "Running");
                startActivity(i);
            }
        });

        cycling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),StartActivity.class);
                i.putExtra("typeActivity", "Cycling");
                startActivity(i);
            }
        });

        return view;
    }
}
