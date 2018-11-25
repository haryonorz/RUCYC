package com.example.zenoinc.rucyc.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.model.Feed;
import com.example.zenoinc.rucyc.utilities.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FeedAdapter extends BaseAdapter {
    private Context context;
    private List<Feed> feedListActivity;
    Date d;

    private static class ViewHolder {
        int cardList[] = { R.id.history_layout };
        int textList[] = { R.id.typeActivity_textView, R.id.timePost_textView, R.id.duration_textView, R.id.distance_textView };
        List<CardView> layoutList;
        List<TextView> textViewList;
        ImageView photoTypeActivity;
    }

    public FeedAdapter(Context context, List<Feed> list) {
        this.context = context;
        this.feedListActivity = new ArrayList<>();
        this.feedListActivity.addAll(list);
    }

    public void addFeedItemToAdapter(List<Feed> list) {
        if(list!=null) feedListActivity.addAll(list);
        notifyDataSetChanged();
    }

    public void removeFeedItem(int index) {
        feedListActivity.remove(index);
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return feedListActivity.size();
    }

    @Override
    public Object getItem(int position){
        return feedListActivity.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent){
        final ViewHolder holder;
        if (rowView == null){
            LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_feed, parent, false);
            holder = new ViewHolder();
            holder.layoutList = new ArrayList<>();
            holder.textViewList = new ArrayList<>();
            for(int card : holder.cardList)
                holder.layoutList.add((CardView) rowView.findViewById(card));
            for(int text : holder.textList)
                holder.textViewList.add((TextView) rowView.findViewById(text));
            holder.photoTypeActivity = rowView.findViewById(R.id.typeActivity_imageView);
            rowView.setTag(holder);
        } else holder = (ViewHolder)rowView.getTag();


        String news_id = feedListActivity.get(position).getHistory().get(0);
        String Activity = feedListActivity.get(position).getHistory().get(1);
        String date = feedListActivity.get(position).getHistory().get(2);
        Integer Time = Integer.valueOf(feedListActivity.get(position).getHistory().get(3));
        Double distanceM = Double.valueOf(feedListActivity.get(position).getHistory().get(4));

        int  seconds = Time;
        int hours = seconds/3600;
        int minutes = (seconds%3600)/60;
        int secs = seconds%60;
        String timeF;
        if(minutes==0 && hours==0){
            timeF = String.format("%02ds", secs);
        }else if(hours==0){
            timeF = String.format("%02dm %02ds", minutes, secs);
        }else {
            timeF = String.format("%dh %02dm %02ds", hours, minutes, secs);
        }

        Double distanceKm = distanceM / 1000.00;
        String distanceF = String.format("%.2f Km", distanceKm);

        holder.textViewList.get(0).setText(Activity);
        if (Activity.equals("Running")) {
            holder.photoTypeActivity.setImageResource(R.drawable.running);
        }else {
            holder.photoTypeActivity.setImageResource(R.drawable.cycling);
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = sdf.parse(date);
        } catch (Exception e){
        }
        DateTimeUtils dtu = new DateTimeUtils(holder.textViewList.get(1));
        dtu.setDate(d);
        holder.textViewList.get(2).setText(timeF);
        holder.textViewList.get(3).setText(distanceF);

        return rowView;
    }
}