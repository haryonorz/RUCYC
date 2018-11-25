package com.example.zenoinc.rucyc.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.fragment.Slide1;
import com.example.zenoinc.rucyc.fragment.Slide2;
import com.example.zenoinc.rucyc.fragment.Slide3;
import com.example.zenoinc.rucyc.fragment.Slide4;
import com.example.zenoinc.rucyc.model.PrefManager;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class Intro extends AppCompatActivity implements ViewPager.OnPageChangeListener{
    private ViewPager viewPager;
    private CircleIndicator indicator;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchLoginScreen();
            finish();
        }
        initComponents();
    }

    private void launchLoginScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(Intro.this, Login.class));
        finish();
    }

    private void initComponents() {
        viewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        indicator = findViewById(R.id.indicator);
        adapter.addFragment(new Slide1());
        adapter.addFragment(new Slide2());
        adapter.addFragment(new Slide3());
        adapter.addFragment(new Slide4());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        indicator.setViewPager(viewPager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        indicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onBackPressed(){
        if(viewPager.getCurrentItem()==0){
            finish();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        private void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }
}
