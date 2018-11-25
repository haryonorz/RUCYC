package com.example.zenoinc.rucyc.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.LocaleList;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.fragment.Activity;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.example.zenoinc.rucyc.utilities.DateTimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ActivityWithInfo extends AppCompatActivity {

    private String news_id;
    private PercentRelativeLayout feed_layout;
    private SwipeRefreshLayout swipe;
    private List<TextView> requestListTV;
    private List<ImageView> requestListIV;

    private UserDB udb;
    private User user;

    private GoogleMap map;
    private SupportMapFragment mapView;

    private Polyline runningPathPolyline;
    private int polylineWidth = 30;

    ArrayList<Location> locationList;
    Date d;
    private String typeActivity, location, timePost;
    private double calories, distanceM, avSpeed, timeS;
    private boolean first = true;

    private int requestListResIdTV[] = {R.id.activity_textView, R.id.time_textView,
            R.id.calories_textView, R.id.distance_textView, R.id.avSpeed_textView, R.id.name_textView,
            R.id.timePost_textView};
    private int requestListResIdIV[] = {R.id.back_imageView, R.id.profile_imageView,
            R.id.settingPost_imageView, R.id.activity_imageView};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            news_id = String.valueOf(extras.getInt("news_id"));
        }

        if(DataInfo.token == null)
            new AuthUser(this).execute("");

        udb = new UserDB(this);
        user = udb.getUser();

        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapView.onCreate(savedInstanceState);
        initComponents();
    }

    private void initComponents(){

        swipe = findViewById(R.id.swipe);
        swipe.setEnabled(false);
        swipe.setColorSchemeResources(R.color.colorPrimary);

        feed_layout = findViewById(R.id.feed_layout);
        requestListTV = new ArrayList<>();
        requestListIV = new ArrayList<>();
        locationList = new ArrayList<>();

        for (int i = 0; i < requestListResIdTV.length; i++) {
            if (requestListResIdIV.length <= i){
                requestListTV.add((TextView) findViewById(requestListResIdTV[i]));
            }else {
                requestListTV.add((TextView) findViewById(requestListResIdTV[i]));
                requestListIV.add((ImageView) findViewById(requestListResIdIV[i]));
            }
        }

        requestListIV.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityWithInfo.this, Menu.class));
                finish();
            }
        });

        requestListIV.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataInfo.tabposition = 2;
                startActivity(new Intent(ActivityWithInfo.this, Menu.class));
                finish();
            }
        });

        requestListIV.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog();
            }
        });

        getFeedInformation();
    }

    private void setInformationFeed(){
        setImageProfile(requestListIV.get(1));
        requestListTV.get(5).setText(user.getFname()+" "+user.getLname());
        requestListTV.get(5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataInfo.tabposition = 2;
                startActivity(new Intent(ActivityWithInfo.this, Menu.class));
                finish();
            }
        });
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            d = sdf.parse(timePost);
        } catch (Exception e){
        }
        DateTimeUtils dtu = new DateTimeUtils(requestListTV.get(6));
        dtu.setDate(d);
        requestListTV.get(0).setText(typeActivity);
        if (typeActivity.equals("Running")) {
            requestListIV.get(3).setImageResource(R.drawable.ic_running);
        }else {
            requestListIV.get(3).setImageResource(R.drawable.ic_cycling);
        }
        int  seconds = (int) timeS;
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
        requestListTV.get(1).setText(timeF);

        String caloriesF = String.format("%.2f Kcal", calories);
        requestListTV.get(2).setText(caloriesF);
        Double distanceKm = distanceM / 1000.00;
        String distanceF = String.format("%.2f Km", distanceKm);
        requestListTV.get(3).setText(distanceF);

        String avSpeedF = String.format("%.2f Km/h", avSpeed);
        requestListTV.get(4).setText(avSpeedF);

        if (first){
            if(!location.equals("")){
                String[] latlongArray = location.split(";");

                for (int i=0; i < latlongArray.length; i++){
                    String[] latlong = latlongArray[i].split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    if (latlong.length!=1){
                        double longitude = Double.parseDouble(latlong[1]);
                        Log.e("location :", latitude + " " + longitude);
                        Location locations = new Location("");
                        locations.setLatitude(latitude);
                        locations.setLongitude(longitude);
                        locationList.add(locations);
                    }else {
                        Log.e("location :", String.valueOf(latitude));
                    }

                    Log.e("location :", String.valueOf(locationList));
                }
                first=false;
            }
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setZoomControlsEnabled(false);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //User has previously accepted this permission
                    if (ActivityCompat.checkSelfPermission(ActivityWithInfo.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(false);
                    }
                } else {
                    //Not in api-23, no need to prompt
                    map.setMyLocationEnabled(false);
                }
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);

                if(!location.equals("")){
                    Location locCenter = locationList.get(Math.round(((float) locationList.size())/2));

                    LatLng sMarker = new LatLng(locCenter.getLatitude(), locCenter.getLongitude());

                    addPolyline(locationList);

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(sMarker, 15));
                }
            }
        });

        feed_layout.setVisibility(View.VISIBLE);
        swipe.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearPolyline();
                refreshFeedInformation();
                feed_layout.setVisibility(View.GONE);
            }
        });
        swipe.setRefreshing(false);
        swipe.setEnabled(true);
    }

    private void clearPolyline() {
        if (runningPathPolyline != null) {
            runningPathPolyline.remove();
            runningPathPolyline = null;
        }
    }

    private void addPolyline(ArrayList<Location> locationList) {
        if (locationList.size() > 1){
            Location fromLocation = locationList.get(0);

            LatLng from = new LatLng(((fromLocation.getLatitude())),
                    ((fromLocation.getLongitude())));

            runningPathPolyline = map.addPolyline(new PolylineOptions()
                    .add(from)
                    .width(polylineWidth)
                    .color(Color.parseColor("#801B60FE"))
                    .geodesic(true));

            for(int i = 1; i<locationList.size();i++) {
                Location toLocation = locationList.get(i);
                LatLng to = new LatLng(((toLocation.getLatitude())),
                        ((toLocation.getLongitude())));

                List<LatLng> points = runningPathPolyline.getPoints();
                points.add(to);

                runningPathPolyline.setPoints(points);
            }
        }
    }

    private void setDialog() {
        final Dialog dialog = new Dialog(ActivityWithInfo.this);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(R.color.colorTransparent);
        View view = LayoutInflater.from(ActivityWithInfo.this).inflate(R.layout.dialog_all, null);
        dialog.setContentView(view);
        ListView list = dialog.findViewById(R.id.listview_dialog);
        String[] items = {"Remove Activity"};
        list.setAdapter(new ArrayAdapter<>(ActivityWithInfo.this, R.layout.list_dialog, items));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();
                if(position==0)
                    DialogRemoveActivity();
            }
        });
        TextView cancelBtn = dialog.findViewById(R.id.cancel);
        cancelBtn.setVisibility(View.GONE);
        dialog.show();
    }

    private void DialogRemoveActivity(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.dialog_remove));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new RemoveActivity().execute();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    private void setImageProfile(final ImageView imageView){
        if(user.getPhoto() != null){
            if(user.getPhoto() != 0) {
                if (!this.isFinishing()) {
                    String url = DataInfo.url + "/user/profile/image?user_id=" + user.getUser_id()
                            + "&size=medium" + "&photo=" + user.getPhoto();
                    RequestOptions requestOptions =
                            new RequestOptions().placeholder(R.drawable.bg_photo);
                    Glide.with(this).asBitmap().load(url).apply(requestOptions).into(imageView);
                }
            }else imageView.setBackgroundResource(R.drawable.bg_photo);
        } else imageView.setBackgroundResource(R.drawable.bg_photo);
    }

    public void refreshFeedInformation(){
        getFeedInformation();
    }

    private void getFeedInformation(){
        new GetFeedInformation().execute();
    }

    private class RemoveActivity extends AsyncTask<String, Void, Integer> {
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode;
        private ObjectMapper mapper;
        private String base = DataInfo.url;

        private ProgressDialog dialog;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
            dialog = new ProgressDialog(ActivityWithInfo.this);
            dialog.setMessage(getString(R.string.wait_remove_activity));
            dialog.setCancelable(false);
            dialog.show();
        }

        protected Integer doInBackground(String... params) {
            removeActivity();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            dialog.dismiss();
            if(result == HttpURLConnection.HTTP_OK) {
                switch(statusCode){
                    case 270:
                        viewErrorDesc(getString(R.string.success_remove_activity));
                        finish();
                        break;
                    default:
                        viewErrorDesc(ActivityWithInfo.this.getString(R.string.eror_remove_activity));
                        break;
                }
            }
        }

        private void removeActivity(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("news_id", news_id);

                String request = HTTPTools.PostParamaters(params);
                url=new URL(base+"/feed/remove?"+request);

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10000);
                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    viewErrorDesc(getString(R.string.eror_remove_activity));
                    new AuthUser(ActivityWithInfo.this).execute();
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.eror_remove_activity));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.eror_remove_activity));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.eror_remove_activity));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.eror_remove_activity));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    public class GetFeedInformation extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode, feedNode;
        private ObjectMapper mapper;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
        }

        protected Integer doInBackground(String... params){
            GetFeedInformation();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if(result == HttpURLConnection.HTTP_OK) {
                switch(statusCode){
                    case 260:
                        setInformationFeed();
                        break;
                    default:
                        viewErrorDesc(ActivityWithInfo.this.getString(R.string.get_feed_error_0));
                        break;
                }
            } else {
            }
        }

        private void GetFeedInformation(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("news_id", news_id);

                String request = HTTPTools.PostParamaters(params);
                url=new URL(base+"/feed/information?"+request);

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10000);
                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode==260){
                        feedNode = rootnode.get("feed");
                        typeActivity = feedNode.get("activity").asText();
                        calories =  feedNode.get("calories").asDouble();
                        timeS =  feedNode.get("time").asDouble();
                        distanceM =  feedNode.get("distance").asDouble();
                        avSpeed =  feedNode.get("average_speed").asDouble();
                        location =  feedNode.get("address1").asText();
                        if(location.equals("null")) location = "";
                        timePost =  feedNode.get("created_at").asText();
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                    viewErrorDesc(ActivityWithInfo.this.getString(R.string.get_feed_error_0));
                    new AuthUser(ActivityWithInfo.this).execute("");
                    viewErrorDesc(ActivityWithInfo.this.getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(ActivityWithInfo.this.getString(R.string.get_feed_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(ActivityWithInfo.this.getString(R.string.get_feed_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(ActivityWithInfo.this.getString(R.string.get_feed_error_0));
                viewErrorDesc(ActivityWithInfo.this.getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(ActivityWithInfo.this.getString(R.string.get_feed_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void viewErrorDesc(final String desc){
        ActivityWithInfo.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(ActivityWithInfo.this, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }
}
