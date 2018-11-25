package com.example.zenoinc.rucyc.connection;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.activity.FillInformation;
import com.example.zenoinc.rucyc.activity.Login;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AuthUser extends AsyncTask<String, Void, Integer> {
    private Context context;
    private String base = DataInfo.url;
    private int responseCode;
    private int statusCode;

    private URL url;
    private HttpURLConnection con = null;
    private JsonNode rootnode;
    private ObjectMapper mapper;

    private User user;
    private UserDB udb;

    private boolean menu = false;

    private String tagError = "Sending to Server Error";

    public AuthUser(Context context){
        this.context = context;
        this.udb = new UserDB(context);
        this.user = udb.getUser();
    }

    @Override
    protected Integer doInBackground(String... params) {
        if(params.length==1)
            menu = true;
        login(user.getEmail(), user.getPassword());
        return responseCode;
    }

    private void login(String email, String password) {
        try {
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("email", email);
            params.put("password", password);

            byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

            url=new URL(base+"/auth/login");
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
                if(statusCode==0) {
                    String userStatus = rootnode.get("ustatus").asText();
                    if (userStatus.equals("blacklist")) {
                        blackListUser();
                    } else if(userStatus.equals("incomplete")){
                        if(menu) UnFilledUser();
                        DataInfo.token = rootnode.get("token").asText();
                    } else {
                        DataInfo.token = rootnode.get("token").asText();
                        getUserProfile();
                    }
                }
            }
        } catch (Exception e){
            Log.e(tagError, e.getMessage());
        } finally {
            if(con != null) con.disconnect();
        }
    }

    private void UnFilledUser() {
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(context.getString(R.string.login_unfilled) +
                        "\n" + context.getString(R.string.login_unfilled1));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        udb.updateFilledStatus(false);
                        context.startActivity(new Intent(context, FillInformation.class));
                        ((Activity)context).finish();
                    }
                });
                builder.create().show();
            }
        });
    }


    private void blackListUser() {
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setMessage(context.getString(R.string.login_blacklist) +
                        "\n" + context.getString(R.string.login_blacklist1));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        udb.removeAll();
                        context.startActivity(new Intent(context, Login.class));
                        ((Activity)context).finish();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void getUserProfile() {
        try {
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("token", DataInfo.token);

            byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

            url=new URL(base+"/user/profile");
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
                if(statusCode==130) {
                    JsonNode profileNode = rootnode.get("profile");

                    Date birthdate = new Date();
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                    try {
                        birthdate = inFormat.parse(profileNode.get("birthdate").asText());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    udb.updateInformationUserDB(
                            profileNode.get("photo").asInt(),
                            birthdate,
                            profileNode.get("weight").asText(),
                            profileNode.get("height").asText(),
                            profileNode.get("gender").asText(),
                            profileNode.get("address").asText(),
                            profileNode.get("province").asInt(),
                            profileNode.get("city").asInt(),
                            profileNode.get("district").asInt(),
                            !profileNode.get("status").asText().equals("incomplete"),
                            profileNode.get("status").asText(), DataInfo.token
                    );
                }
            }
        } catch (Exception e){
            Log.e(tagError, e.getMessage());
        }
    }
}