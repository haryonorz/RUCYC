package com.example.zenoinc.rucyc.service;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.activity.ActivityResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends FragmentActivity {
    private String typeActivity;
    private TextView activity,timer, distance, speed, playPauseTextView, stopTextView;
    private ImageView activityImg;
    private SeekBar playPauseSeeBar, stopSeekBar;

    private int seconds = 0;
    private boolean running = false;

    private static final String TAG = "StartActivity";
    public LocationService locationService;
    private GoogleMap map;
    private SupportMapFragment mapView;

    private Marker userPositionMarker;
    private Circle locationAccuracyCircle;
    private BitmapDescriptor userPositionMarkerBitmapDescriptor;
    private Polyline runningPathPolyline;
    private PolylineOptions polylineOptions;
    private int polylineWidth = 30;

    boolean zoomable = true;

    Timer zoomBlockingTimer;
    boolean didInitialZoom;
    private Handler handlerOnUIThread;

    private BroadcastReceiver locationUpdateReceiver;
    private BroadcastReceiver predictedLocationReceiver;

    /* Filater */
    private Circle predictionRange;
    BitmapDescriptor oldLocationMarkerBitmapDescriptor;
    BitmapDescriptor noAccuracyLocationMarkerBitmapDescriptor;
    BitmapDescriptor inaccurateLocationMarkerBitmapDescriptor;
    BitmapDescriptor kalmanNGLocationMarkerBitmapDescriptor;
    ArrayList<Marker> malMarkers = new ArrayList<>();
    final Handler handler = new Handler();

    private double distanceM;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_activity);

        Bundle extras = getIntent().getExtras();
        if (extras!=null){
            typeActivity = extras.getString("typeActivity");
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        activity = findViewById(R.id.activity_textView);
        activityImg = findViewById(R.id.activity_imageView);
        timer = findViewById(R.id.timer_textView);
        playPauseTextView = findViewById(R.id.playPause_textView);
        stopTextView = findViewById(R.id.stop_textView);
        playPauseSeeBar = findViewById(R.id.playPause_seekBar);
        stopSeekBar = findViewById(R.id.stop_seekBar);

        activity.setText(typeActivity);
        if (typeActivity.equals("Running")) {
            activityImg.setImageResource(R.drawable.ic_running);
        }else {
            activityImg.setImageResource(R.drawable.ic_cycling);
        }

        stopSeekBar.setEnabled(false);
        stopSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setBackgroundResource(R.drawable.rucyc_button);
                stopTextView.setText(getString(R.string.menu_swipe));
                seekBar.setThumb(getResources().getDrawable(R.drawable.ic_stop_touch));
                seekBar.setThumbOffset(0);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() == 100){
                    stopActivity();
                    playPauseTextView.setText("Play");
                    seekBar.setEnabled(false);
                    playPauseSeeBar.setProgress(0);
                    playPauseSeeBar.setThumb(getResources().getDrawable(R.drawable.ic_play));
                    playPauseSeeBar.setThumbOffset(0);
                    seekBar.setBackgroundResource(R.drawable.grey_button);
                    stopTextView.setText(getString(R.string.menu_stop));
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_stop));
                }
                seekBar.setProgress(0);
                seekBar.setThumbOffset(0);
            }
        });
        playPauseSeeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                String activityTV = playPauseTextView.getText().toString();
                if (activityTV.equals("Play")){
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_play_touch));
                    seekBar.setThumbOffset(0);
                }else {
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_pause_touch));
                    seekBar.setThumbOffset(0);
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String activityTV = playPauseTextView.getText().toString();
                if (seekBar.getProgress() == 100 && activityTV.equals("Play")){
                    playActivity();
                    playPauseTextView.setText("Pause");
                    stopSeekBar.setEnabled(true);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_pause));
                    stopSeekBar.setBackgroundResource(R.drawable.rucyc_button);
                }else if(seekBar.getProgress() == 100 && activityTV.equals("Pause")){
                    pauseActivity();
                    playPauseTextView.setText("Play");
                    stopSeekBar.setEnabled(true);
                    seekBar.setThumb(getResources().getDrawable(R.drawable.ic_play));
                }
                seekBar.setProgress(0);
                seekBar.setThumbOffset(0);

            }
        });
        runTimer();

        final Intent serviceStart = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(serviceStart);
        this.getApplication().bindService(serviceStart, serviceConnection, Context.BIND_AUTO_CREATE);

        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setZoomControlsEnabled(false);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //User has previously accepted this permission
                    if (ActivityCompat.checkSelfPermission(StartActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(false);
                    }
                } else {
                    //Not in api-23, no need to prompt
                    map.setMyLocationEnabled(false);
                }
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);

                map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int reason) {
                        if(reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                            Log.d(TAG, "onCameraMoveStarted after user's zoom action");

                            zoomable = false;
                            if (zoomBlockingTimer != null) {
                                zoomBlockingTimer.cancel();
                            }

                            handlerOnUIThread = new Handler();

                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    handlerOnUIThread.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            zoomBlockingTimer = null;
                                            zoomable = true;

                                        }
                                    });
                                }
                            };
                            zoomBlockingTimer = new Timer();
                            zoomBlockingTimer.schedule(task, 10 * 1000);
                            Log.d(TAG, "start blocking auto zoom for 10 seconds");
                        }
                    }
                });
            }
        });

        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location newLocation = intent.getParcelableExtra("location");

                drawLocationAccuracyCircle(newLocation);
                drawUserPositionMarker(newLocation);

                if (StartActivity.this.locationService.isLogging) {
                    addPolyline();
                }
                zoomMapTo(newLocation);

                /* Filter Visualization */
                drawMalLocations();

            }
        };

        predictedLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location predictedLocation = intent.getParcelableExtra("location");

                drawPredictionRange(predictedLocation);

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationUpdateReceiver,
                new IntentFilter("LocationUpdated"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                predictedLocationReceiver,
                new IntentFilter("PredictLocation"));

        oldLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.old_location_marker);
        noAccuracyLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.no_accuracy_location_marker);
        inaccurateLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.inaccurate_location_marker);
        kalmanNGLocationMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.kalman_ng_location_marker);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(false);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();

            if (name.endsWith("LocationService")) {
                locationService = ((LocationService.LocationServiceBinder) service).getService();

                locationService.startUpdatingLocation();


            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("LocationService")) {
                locationService.stopUpdatingLocation();
                locationService = null;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        if (this.mapView != null) {
            this.mapView.onPause();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (this.mapView != null) {
            this.mapView.onResume();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.mapView != null) {
            this.mapView.onDestroy();
        }

        try {
            if (locationUpdateReceiver != null) {
                unregisterReceiver(locationUpdateReceiver);
            }

            if (predictedLocationReceiver != null) {
                unregisterReceiver(predictedLocationReceiver);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if (this.mapView != null) {
            this.mapView.onStart();
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.mapView != null) {
            this.mapView.onStop();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (this.mapView != null) {
            this.mapView.onLowMemory();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (this.mapView != null) {
            this.mapView.onSaveInstanceState(outState);
        }
    }

    private void zoomMapTo(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (this.didInitialZoom == false) {
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f));
                this.didInitialZoom = true;
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Toast.makeText(this.getActivity(), "Inital zoom in process", Toast.LENGTH_LONG).show();
        }

        if (zoomable) {
            try {
                zoomable = false;
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng),
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                zoomable = true;
                            }

                            @Override
                            public void onCancel() {
                                zoomable = true;
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void drawUserPositionMarker(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(this.userPositionMarkerBitmapDescriptor == null){
            userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.user_position_point);
        }

        if (userPositionMarker == null) {
            userPositionMarker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(this.userPositionMarkerBitmapDescriptor));
        } else {
            userPositionMarker.setPosition(latLng);
        }
    }


    private void drawLocationAccuracyCircle(Location location){
        if(location.getAccuracy() < 0){
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.locationAccuracyCircle == null) {
            this.locationAccuracyCircle = map.addCircle(new CircleOptions()
                    .center(latLng)
                    .fillColor(Color.argb(64, 0, 0, 0))
                    .strokeColor(Color.argb(64, 0, 0, 0))
                    .strokeWidth(0.0f)
                    .radius(location.getAccuracy())); //set readius to horizonal accuracy in meter.
        } else {
            this.locationAccuracyCircle.setCenter(latLng);
        }
    }

    private void addPolyline() {
        ArrayList<Location> locationList = locationService.locationList;

        if (runningPathPolyline == null) {
            if (locationList.size() > 1){
                Location fromLocation = locationList.get(locationList.size() - 2);
                Location toLocation = locationList.get(locationList.size() - 1);

                LatLng from = new LatLng(((fromLocation.getLatitude())),
                        ((fromLocation.getLongitude())));

                LatLng to = new LatLng(((toLocation.getLatitude())),
                        ((toLocation.getLongitude())));

                this.runningPathPolyline = map.addPolyline(new PolylineOptions()
                        .add(from, to)
                        .width(polylineWidth).color(Color.parseColor("#801B60FE")).geodesic(true));
            }
        } else {
            Location toLocation = locationList.get(locationList.size() - 1);
            LatLng to = new LatLng(((toLocation.getLatitude())),
                    ((toLocation.getLongitude())));

            List<LatLng> points = runningPathPolyline.getPoints();
            points.add(to);

            runningPathPolyline.setPoints(points);
        }
    }

    private void clearPolyline() {
        if (runningPathPolyline != null) {
            runningPathPolyline.remove();
            runningPathPolyline = null;
        }
    }


    /* Filter Visualization */
    private void drawMalLocations(){
        drawMalMarkers(locationService.oldLocationList, oldLocationMarkerBitmapDescriptor);
        drawMalMarkers(locationService.noAccuracyLocationList, noAccuracyLocationMarkerBitmapDescriptor);
        drawMalMarkers(locationService.inaccurateLocationList, inaccurateLocationMarkerBitmapDescriptor);
        drawMalMarkers(locationService.kalmanNGLocationList, kalmanNGLocationMarkerBitmapDescriptor);
    }

    private void drawMalMarkers(ArrayList<Location> locationList, BitmapDescriptor descriptor){
        for(Location location : locationList){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .flat(true)
                    .anchor(0.5f, 0.5f)
                    .icon(descriptor));

            malMarkers.add(marker);
        }
    }

    private void drawPredictionRange(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (this.predictionRange == null) {
            this.predictionRange = map.addCircle(new CircleOptions()
                    .center(latLng)
                    .fillColor(Color.argb(50, 30, 207, 0))
                    .strokeColor(Color.argb(128, 30, 207, 0))
                    .strokeWidth(1.0f)
                    .radius(30)); //30 meters of the prediction range
        } else {
            this.predictionRange.setCenter(latLng);
        }

        this.predictionRange.setVisible(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                StartActivity.this.predictionRange.setVisible(false);
            }
        }, 2000);
    }

    public void clearMalMarkers(){
        for (Marker marker : malMarkers){
            marker.remove();
        }
    }

    private void stopActivity(){
        running = false;
        Log.e("waktu :", String.valueOf(seconds));
        double timeS = (double) seconds;

        float distanceM = StartActivity.this.locationService.stopLogging();
        Intent i = new Intent(StartActivity.this, ActivityResult.class);
        i.putExtra("typeActivity", typeActivity);
        i.putExtra("timeS", timeS);
        i.putExtra("distanceM", distanceM);
        i.putExtra("locationList", StartActivity.this.locationService.loclist);

        Log.e("jarak :", String.valueOf(distanceM));
        Log.e("loclist :", String.valueOf(StartActivity.this.locationService.loclist));
        seconds = 0;
        startActivity(i);
        finish();
    }


    private void playActivity(){
        running = true;
        clearPolyline();
        clearMalMarkers();

        StartActivity.this.locationService.startLogging();
    }

    private void pauseActivity(){
        running = false;
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds/3600;
                int minutes = (seconds%3600)/60;
                int secs = seconds%60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);
                timer.setText(time);
                if (running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
}
