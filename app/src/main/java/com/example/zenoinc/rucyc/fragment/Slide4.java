package com.example.zenoinc.rucyc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.activity.Login;
import com.example.zenoinc.rucyc.activity.Register;
import com.example.zenoinc.rucyc.model.PrefManager;

public class Slide4 extends Fragment {
    private Activity activity;
    private Context context;
    private Button login;
    private TextView create;
    private PrefManager prefManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slide4, container, false);
        create = view.findViewById(R.id.signup_TextView);
        login = view.findViewById(R.id.signin_Button);
        activity = getActivity();
        context = getContext();
        prefManager = new PrefManager(context);
        initComponents();
        return view;
    }
    private void initComponents(){
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefManager.setFirstTimeLaunch(false);
                Intent i = new Intent(getActivity(),Register.class);
                startActivity(i);

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefManager.setFirstTimeLaunch(false);
                Intent i = new Intent(getActivity(),Login.class);
                startActivity(i);
            }
        });
    }
}
