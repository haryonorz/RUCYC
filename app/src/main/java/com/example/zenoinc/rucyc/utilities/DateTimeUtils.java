package com.example.zenoinc.rucyc.utilities;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    private TextView tdate;

    public DateTimeUtils(){
    }

    public DateTimeUtils(TextView tdate){
        this.tdate = tdate;
    }

    public void setDate(Date date){
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();

        long diff = now.getTime() - date.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        String sdate;

        if(diffDays>=1){
            sdate = new SimpleDateFormat("d MMM", Locale.ENGLISH).format(date);
            tdate.setText(sdate);
        } else if(diffHours>=1){
            sdate = diffHours + " hour ago";
            tdate.setText(sdate);
        } else if(diffMinutes>=1){
            sdate = diffMinutes + " minutes ago";
            tdate.setText(sdate);
        } else {
            sdate = "a few moment ago";
            tdate.setText(sdate);
        }
    }

    public String getStringDate(Date date){
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();

        long diff = now.getTime() - date.getTime();

        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        String sdate;

        if(diffDays>=1){
            sdate = new SimpleDateFormat("d MMM", Locale.ENGLISH).format(date);
        } else if(diffHours>=1){
            sdate = diffHours + " hour ago";
        } else if(diffMinutes>=1){
            sdate = diffMinutes + " minutes ago";
        } else {
            sdate = "beberapa saat yang lalu";
        }
        return sdate;
    }
}
