package com.example.zenoinc.rucyc.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.zenoinc.rucyc.utilities.DataInfo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class SetFCMToken extends AsyncTask<String, Void, Integer> {
    private Context context;

    private String base = DataInfo.url;
    private String request;
    private int responseCode;

    private URL url;
    private HttpURLConnection con = null;

    private String tagError = "Sending to Server Error";

    public SetFCMToken(Context context){
        this.context = context;
    }

    protected Integer doInBackground(String... params){
        setFCMToken(params[0]);
        return responseCode;
    }

    private void setFCMToken(String fcm){
        try {
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("token", DataInfo.token);
            params.put("fcm", fcm);

            byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

            url=new URL(base+"/user/fcm");

            HttpURLConnection.setFollowRedirects(false);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(10000);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            con.setDoOutput(true);
            con.getOutputStream().write(postDataBytes);

            responseCode = con.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                new AuthUser(context).execute();
            }
        } catch (Exception e){
            Log.e(tagError, e.getMessage());
        } finally {
            if(con != null) con.disconnect();
        }
    }
}