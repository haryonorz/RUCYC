package com.example.zenoinc.rucyc.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.service.MulaiActivity;
import com.example.zenoinc.rucyc.service.StartActivity;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            public void run(){
                UserDB udb = new UserDB(SplashScreen.this);
                User user = udb.getUser();
                if(user!=null){
                    if(user.getFilled()){
                        startActivity(new Intent(SplashScreen.this, Menu.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashScreen.this, FillInformation.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(SplashScreen.this, Intro.class));
                    finish();
                }
            }
        }, 1000);
    }
}
