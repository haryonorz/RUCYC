package com.example.zenoinc.rucyc.activity;

import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatusActivity extends AppCompatActivity {
    private String cyclingActivity, runningActivity;
    private double cyclingTime, cyclingDistance, cyclingCalories, runningTime, runningDistance, runningCalories;
    private int seconds, hours, minutes, secs;

    private UserDB udb;
    private User user;

    private ConstraintLayout statusLayout;
    private ImageView back;
    private SwipeRefreshLayout swipe;
    private List<TextView> requestListTV;

    private int requestListResId[] = {R.id.totActivityCycling_textView, R.id.timeSpentCycling_textView,
            R.id.totDistanceCycling_textView, R.id.totCaloriesBurnedCycling_textView, R.id.totActivityRunning_textView,
            R.id.timeSpentRunning_textView, R.id.totDistanceRunning_textView, R.id.totCaloriesBurnedRunning_textView};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        if(DataInfo.token == null)
            new AuthUser(this).execute("");

        udb = new UserDB(this);
        user = udb.getUser();
        initComponents();
    }

    private void initComponents(){
        requestListTV = new ArrayList<>();

        for (int i = 0; i < requestListResId.length; i++) {
            requestListTV.add((TextView) findViewById(requestListResId[i]));
        }

        statusLayout = findViewById(R.id.status_layout);
        back = findViewById(R.id.back_imageView);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        swipe = findViewById(R.id.swipe);
        swipe.setEnabled(false);
        swipe.setColorSchemeResources(R.color.colorPrimary);

        getStatusInformation();
    }

    private void setInformationSatus(){
        requestListTV.get(0).setText(cyclingActivity + " Activity");
        resetTime();
        seconds = (int) cyclingTime;
        hours = seconds/3600;
        minutes = (seconds%3600)/60;
        secs = seconds%60;
        String timeF1;
        if(hours==0){
            timeF1 = String.format("%02dm %02ds", minutes, secs);
        }else {
            timeF1 = String.format("%dh %02dm %02ds", hours, minutes, secs);
        }
        requestListTV.get(1).setText(timeF1);
        Double distanceKm1 = cyclingDistance / 1000.00;
        String distanceF1 = String.format("%.2f Km", distanceKm1);
        requestListTV.get(2).setText(distanceF1);
        String caloriesF1 = String.format("%.2f Kcal", cyclingCalories);
        requestListTV.get(3).setText(caloriesF1);

        requestListTV.get(4).setText(runningActivity + " Activity");
        resetTime();
        seconds = (int) runningTime;
        hours = seconds/3600;
        minutes = (seconds%3600)/60;
        secs = seconds%60;
        String timeF2;
        if(hours==0){
            timeF2 = String.format("%02dm %02ds", minutes, secs);
        }else {
            timeF2 = String.format("%dh %02dm %02ds", hours, minutes, secs);
        }
        requestListTV.get(5).setText(timeF2);
        Double distanceKm2 = runningDistance / 1000.00;
        String distanceF2 = String.format("%.2f Km", distanceKm2);
        requestListTV.get(6).setText(distanceF2);
        String caloriesF2 = String.format("%.2f Kcal", runningCalories);
        requestListTV.get(7).setText(caloriesF2);

        statusLayout.setVisibility(View.VISIBLE);
        swipe.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshStatusInformation();
                statusLayout.setVisibility(View.GONE);
            }
        });
        swipe.setRefreshing(false);
        swipe.setEnabled(true);
    }

    public void resetTime(){
        seconds = 0;
        hours = 0;
        minutes = 0;
        secs = 0;
    }


    public void refreshStatusInformation(){
        getStatusInformation();
    }

    private void getStatusInformation(){
        new GetSatusActivity().execute();
    }

    public class GetSatusActivity extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode, runningNode, cyclingNode;
        private ObjectMapper mapper;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
        }

        protected Integer doInBackground(String... params){
            GetSatusActivity();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if(result == HttpURLConnection.HTTP_OK) {
                switch(statusCode){
                    case 220:
                        setInformationSatus();
                        break;
                    default:
                        viewErrorDesc(StatusActivity.this.getString(R.string.get_feed_error_0));
                        break;
                }
            } else {
            }
        }

        private void GetSatusActivity(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);

                String request = HTTPTools.PostParamaters(params);
                url=new URL(base+"/activity/status?"+request);

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10000);
                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode==220){
                        cyclingNode = rootnode.get("cycling_status");
                        runningNode = rootnode.get("running_status");
                        cyclingActivity = cyclingNode.get("cactivity").asText();
                        cyclingTime =  cyclingNode.get("ctime").asDouble();
                        cyclingDistance =  cyclingNode.get("cdistance").asDouble();;
                        cyclingCalories =  cyclingNode.get("ccalories").asDouble();
                        runningActivity =  runningNode.get("cactivity").asText();
                        runningTime =  runningNode.get("ctime").asDouble();
                        runningDistance =  runningNode.get("cdistance").asDouble();
                        runningCalories =  runningNode.get("ccalories").asDouble();
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                    viewErrorDesc(StatusActivity.this.getString(R.string.get_feed_error_0));
                    new AuthUser(StatusActivity.this).execute("");
                    viewErrorDesc(StatusActivity.this.getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(StatusActivity.this.getString(R.string.get_feed_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(StatusActivity.this.getString(R.string.get_feed_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(StatusActivity.this.getString(R.string.get_feed_error_0));
                viewErrorDesc(StatusActivity.this.getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(StatusActivity.this.getString(R.string.get_feed_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void viewErrorDesc(final String desc){
        StatusActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(StatusActivity.this, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }
}
