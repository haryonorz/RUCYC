package com.example.zenoinc.rucyc.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.fragment.Activity;
import com.example.zenoinc.rucyc.fragment.NewsFeed;
import com.example.zenoinc.rucyc.fragment.Profile;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

public class Menu extends AppCompatActivity {
    private TextView mTitle;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private BottomNavigationViewEx navigation;

    private BottomNavigationViewEx.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_news_feed:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_activity:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_user:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if(DataInfo.token==null)
            new AuthUser(Menu.this).execute("");

        initComponents();
    }

    private void initComponents(){
        viewPager = findViewById(R.id.navigationcontent);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.enableShiftingMode(false);
        navigation.enableItemShiftingMode(false);
        navigation.setTextVisibility(false);
        navigation.setIconSize(28,28);
        navigation.setIconsMarginTop(24);
        navigation.setItemHeight(BottomNavigationViewEx.dp2px(this, 52));
        navigation.setIconTintList(0,
                getResources().getColorStateList(R.color.selector_item_bottom_navigation));
        navigation.setIconTintList(1,
                getResources().getColorStateList(R.color.selector_item_bottom_navigation));
        navigation.setIconTintList(2,
                getResources().getColorStateList(R.color.selector_item_bottom_navigation));
        setupViewPager(viewPager);
        if (DataInfo.tabposition==2){
            navigation.setCurrentItem(2);
        }else{
            navigation.setCurrentItem(0);
        }
    }

    public void onBackPressed(){
        if(navigation.getCurrentItem() == 2){
            Profile profile = (Profile) adapter.getItem(2);
            switch(profile.getPosition()){
                case 0:
                    moveTaskToBack(true);
                    break;
                case 1:
                    profile.resetLayout();
                    break;
                default:
                    break;
            }
        } else {
            moveTaskToBack(true);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NewsFeed());
        adapter.addFragment(new Activity());
        adapter.addFragment(new Profile());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                DataInfo.tabposition = position;
                navigation.setCurrentItem(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void toProfile(){
        navigation.setCurrentItem(2);
    }

    public void resetProfileLayout(){
        Profile profile = (Profile) adapter.getItem(2);
        profile.resetLayout();
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
