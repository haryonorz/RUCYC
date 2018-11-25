package com.example.zenoinc.rucyc.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.service.StartActivity;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityResult extends AppCompatActivity {
    private String typeActivity;
    private double calories, distanceM, distanceKm, avSpeed, timeS, timeH, timeM, MET;
    private TextView activityTV, caloriesTV, timeTV, distanceTV, avSpeedTV;
    private ImageView activityImg, backActivity, save;
    private ProgressBar savePB;

    private UserDB udb;
    private User user;

    ArrayList<Location> locationList;
    private String location;
    private GoogleMap map;
    private SupportMapFragment mapView;

    private Marker userPositionMarker;
    private Circle locationAccuracyCircle;
    private BitmapDescriptor userPositionMarkerBitmapDescriptor;
    private Polyline runningPathPolyline;
    private PolylineOptions polylineOptions;
    private int polylineWidth = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if(DataInfo.token == null)
            new AuthUser(this).execute("");

        Bundle extras = getIntent().getExtras();
        if (extras!=null){
            typeActivity = extras.getString("typeActivity");
            timeS = extras.getDouble("timeS");
            float distance = extras.getFloat("distanceM");
            distanceM = (double) distance;
            location = extras.getString("locationList");
            Log.e("jarak diresult :", String.valueOf(distanceM));
            Log.e("location diresult :", String.valueOf(location));
        }

        udb = new UserDB(this);
        user = udb.getUser();

        locationList = new ArrayList<>();
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
                }else {Log.e("location :", String.valueOf(latitude));
                }
            }
        }

        activityTV = findViewById(R.id.activity_textView);
        activityImg = findViewById(R.id.activity_imageView);
        timeTV = findViewById(R.id.time_textView);
        distanceTV = findViewById(R.id.distance_textView);
        avSpeedTV = findViewById(R.id.avSpeed_textView);
        caloriesTV = findViewById(R.id.calories_textView);

        backActivity = findViewById(R.id.back_imageView);
        save = findViewById(R.id.save_imageView);
        savePB = findViewById(R.id.save_progressBar);

        activityTV.setText(typeActivity);
        Log.e("info :", typeActivity);
        Log.e("info :", String.valueOf(timeS));
        if (typeActivity.equals("Running")) {
            activityImg.setImageResource(R.drawable.ic_running);
        }else {
            activityImg.setImageResource(R.drawable.ic_cycling);
        }

        int  seconds = (int) timeS;
        int hours = seconds/3600;
        int minutes = (seconds%3600)/60;
        int secs = seconds%60;String timeF;
        if(minutes==0 && hours==0){
            timeF = String.format("%02ds", secs);
        }else if(hours==0){
            timeF = String.format("%02dm %02ds", minutes, secs);
        }else {
            timeF = String.format("%dh %02dm %02ds", hours, minutes, secs);
        }
        timeTV.setText(timeF);

        timeH = timeS / 3600.00;
        timeM = timeS / 60.00;
        distanceKm = distanceM / 1000.00;
        String distanceF = String.format("%.2f Km", distanceKm);
        distanceTV.setText(distanceF);
        if (distanceKm!=0){
            avSpeed = distanceKm/timeH;
            String avSpeedF = String.format("%.2f Km/h", avSpeed);
            avSpeedTV.setText(avSpeedF);

            if (typeActivity.equals("Cycling")){
                if (avSpeed < 16.00 ){
                    MET = 4.0;
                } else if (avSpeed >= 16.00 && avSpeed <= 19.31){
                    MET = 6.0;
                } else if (avSpeed > 19.31 && avSpeed <= 22.53){
                    MET = 8.0;
                } else if (avSpeed > 22.53 && avSpeed <= 25.74){
                    MET = 10.0;
                } else if (avSpeed > 25.74 && avSpeed <= 30.57){
                    MET = 12.0;
                } else {
                    MET = 16.0;
                }
            }else{
                if (avSpeed < 6.40 ){
                    MET = 5.0;
                } else if (avSpeed >= 6.40 && avSpeed <= 8.00){
                    MET = 8.3;
                } else if (avSpeed > 8.00 && avSpeed <= 9.60){
                    MET = 9.8;
                } else if (avSpeed > 9.60 && avSpeed <= 11.26){
                    MET = 11.0;
                } else if (avSpeed > 11.26 && avSpeed <= 12.87){
                    MET = 11.8;
                } else if (avSpeed > 12.87 && avSpeed <= 14.48){
                    MET = 12.8;
                } else if (avSpeed > 14.48 && avSpeed <= 16.00){
                    MET = 14.5;
                } else if (avSpeed > 16.00 && avSpeed <= 17.70){
                    MET = 16.0;
                } else if (avSpeed > 17.70 && avSpeed <= 19.31){
                    MET = 19.0;
                } else if (avSpeed > 19.31 && avSpeed <= 20.92){
                    MET = 19.8;
                } else {
                    MET = 23.0;
                }
            }
            calories = (MET*3.5*(Double.valueOf(user.getWeight())*2.2)/200.0) * timeM;

            String caloriesF = String.format("%.2f Kcal", calories);
            caloriesTV.setText(caloriesF);
        }

        backActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityResult.this, Menu.class));
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostActivity().execute(
                        typeActivity,
                        String.valueOf(timeS),
                        String.valueOf(calories),
                        String.valueOf(distanceM),
                        String.valueOf(avSpeed),
                        location);
            }
        });


        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setZoomControlsEnabled(false);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //User has previously accepted this permission
                    if (ActivityCompat.checkSelfPermission(ActivityResult.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(false);
                    }
                } else {
                    //Not in api-23, no need to prompt
                    map.setMyLocationEnabled(false);
                }
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);

                if(!location.equals("") && location.length() > 1){
                    Location locCenter = locationList.get(Math.round(((float) locationList.size())/2));

                    LatLng sMarker = new LatLng(locCenter.getLatitude(), locCenter.getLongitude());

                    addPolyline(locationList);

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(sMarker, 15));
                }

            }
        });
    }

    private void SetProgressBar(boolean b) {
        if (b) {
            save.setVisibility(View.GONE);
            savePB.setVisibility(View.VISIBLE);
        } else {
            save.setVisibility(View.VISIBLE);
            savePB.setVisibility(View.GONE);
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

    private class PostActivity extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode;
        private ObjectMapper mapper;

        private String activity, locationList;
        private int time;
        private double calories, distance, avSpeed, timeS;

        private String tagError = "Sending to Server Error";

        protected void onPreExecute() {
            SetProgressBar(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            activity = params[0];
            timeS = Double.valueOf(params[1]);
            time = (int) timeS;
            calories = Double.valueOf(params[2]);
            distance = Double.valueOf(params[3]);
            avSpeed = Double.valueOf(params[4]);
            locationList = params[5];
            postActivity();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                switch (statusCode) {
                    case 200:
                        viewErrorDesc(getString(R.string.post_success));
                        SuccessSending();
                        break;
                    case 207:
                        blackListUser();
                        break;
                    default:
                        viewErrorDesc(getString(R.string.send_error_0));
                        break;
                }
            }
            SetProgressBar(false);
        }

        private void postActivity(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("activity", activity);
                params.put("calories", calories);
                params.put("time", time);
                params.put("distance", distance);
                params.put("average_speed", avSpeed);
                params.put("address1", locationList);

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/activity");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(10000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                con.setDoOutput(true);
                con.getOutputStream().write(postDataBytes);

                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode==200){
                        viewErrorDesc(getString(R.string.send_success));
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                    viewErrorDesc(getString(R.string.send_error_0));
                    new AuthUser(ActivityResult.this).execute("");
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.send_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.send_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.send_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.send_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }

    }

    private void blackListUser(){
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityResult.this);
                builder.setCancelable(false);
                builder.setMessage(getString(R.string.login_blacklist) +
                        "\n" + getString(R.string.login_blacklist1));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        udb.removeAll();
                        startActivity(new Intent(ActivityResult.this, Login.class));
                        finish();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void SuccessSending(){
        startActivity(new Intent(ActivityResult.this, Menu.class));
        finish();
    }

    private void viewErrorDesc(final String desc){
        ActivityResult.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(ActivityResult.this, desc, Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }
}
