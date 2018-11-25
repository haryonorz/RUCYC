package com.example.zenoinc.rucyc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.activity.About;
import com.example.zenoinc.rucyc.activity.ChangePassword;
import com.example.zenoinc.rucyc.activity.EditProfile;
import com.example.zenoinc.rucyc.activity.ImagePreview;
import com.example.zenoinc.rucyc.activity.Login;
import com.example.zenoinc.rucyc.activity.StatusActivity;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Profile extends Fragment{
    private Activity activity;
    private Context context;

    private UserDB udb;
    private User user;
    private List<TextView> userListTV;

    private TextView mTitle;
    private ImageView profilePhoto, setting, backToProfile, genderImg;
    private CardView statusLayout;

    private String cActivity;
    private double cDistance;

    private ConstraintLayout profileLayout, settingLayout;

    private byte position = 0;

    private Map<Integer, String> cityList = new LinkedHashMap<>();
    private int selectedProvinceId, selectedCityId;

    private int userTVListResId[] = { R.id.nameProfile_textView, R.id.gender_textView, R.id.city_textView,
            R.id.weight_textView,R.id.height_textView, R.id.totDistance_textView,
            R.id.totActivity_textView, R.id.editProfile_textView, R.id.changePass_textView,
            R.id.logout_textView, R.id.app_textView};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        activity = getActivity();
        context = getContext();

        if(DataInfo.token == null)
            new AuthUser(context).execute("");

        udb = new UserDB(context);
        user = udb.getUser();
        userListTV = new ArrayList<>();

        //mendapatkan component di editProfile dan Profile
        for(int userResId : userTVListResId)
            userListTV.add((TextView) view.findViewById(userResId));

        profileLayout = view.findViewById(R.id.profile_layout);
        settingLayout = view.findViewById(R.id.settings_layout);
        statusLayout = view.findViewById(R.id.status_layout);

        mTitle = view.findViewById(R.id.profile_title);

        profilePhoto = view.findViewById(R.id.photoProfile_imageView);
        setting = view.findViewById(R.id.setting_imageView);
        backToProfile = view.findViewById(R.id.back_imageView);
        genderImg = view.findViewById(R.id.gender_imageView);

        initComponents();

        return view;
    }

    private void initComponents() {
        Shader textShader=new LinearGradient(0, 0, 0, mTitle.getPaint().getTextSize()+25,
                new int[]{
                        ContextCompat.getColor(getActivity(), R.color.colorGStart2),
                        ContextCompat.getColor(getActivity(), R.color.colorGEnd2)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mTitle.getPaint().setShader(textShader);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSettingLayout();
            }
        });

        statusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),StatusActivity.class);
                startActivity(i);;
            }
        });

        profileLayout.setVisibility(View.VISIBLE);
        settingLayout.setVisibility(View.GONE);

       setImageProfile(profilePhoto);
       profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // nampilin foto
                if(user.getPhoto() != null) {
                    if (user.getPhoto() != 0) {
                        String url = DataInfo.url + "/user/profile/image?user_id=" + user.getUser_id()
                                + "&size=medium" + "&photo=" + user.getPhoto();
                        Intent i = new Intent(activity, ImagePreview.class);
                        i.putExtra("url", url);
                        startActivity(i);
                    }
                }
            }
       });

        userListTV.get(0).setText(user.getFname()+" " +user.getLname());
        Log.e("Info :", user.getGender());
        if(user.getGender().equals("Male")){
            genderImg.setImageResource(R.drawable.ic_male);
        }else{
            genderImg.setImageResource(R.drawable.ic_female);
        }
        userListTV.get(1).setText(user.getGender());
        userListTV.get(3).setText(user.getWeight() + " Kg");
        userListTV.get(4).setText(user.getHeight() + " Cm");

        new GetInfoActivity().execute();

        selectedProvinceId = user.getProvince();
        selectedCityId = user.getCity();
        new GetAllAddress().execute(1, selectedProvinceId);
    }

    public void resetLayout(){
        if(position==1)
            switchLayoutSetting();
        switchLayoutProfile();
        position = 0;
        user = udb.getUser();
        setImageProfile(profilePhoto);
        userListTV.get(0).setText(user.getFname()+" " +user.getLname());
        Log.e("Info :", user.getGender());
        if(user.getGender().equals("Male")){
            genderImg.setImageResource(R.drawable.ic_male);
        }else{
            genderImg.setImageResource(R.drawable.ic_female);
        }
        userListTV.get(1).setText(user.getGender());
        userListTV.get(3).setText(user.getWeight() + " Kg");
        userListTV.get(4).setText(user.getHeight() + " Cm");

        new GetInfoActivity().execute();

        selectedProvinceId = user.getProvince();
        selectedCityId = user.getCity();
        new GetAllAddress().execute(1, selectedProvinceId);
    }

    private void setSettingLayout(){
        position = 1;

        backToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetLayout();
            }
        });

        userListTV.get(7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),EditProfile.class);
                startActivity(i);;
            }
        });

        userListTV.get(8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),ChangePassword.class);
                startActivity(i);;
            }
        });

        userListTV.get(9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogLogout();
            }
        });

        userListTV.get(10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),About.class);
                startActivity(i);;
            }
        });

        switchLayoutProfile();
        switchLayoutSetting();
    }

    private void switchLayoutProfile(){
        if(profileLayout.getVisibility() == View.VISIBLE)
            profileLayout.setVisibility(View.GONE);
        else
            profileLayout.setVisibility(View.VISIBLE);
    }

    private void switchLayoutSetting(){
        if(settingLayout.getVisibility() == View.VISIBLE)
            settingLayout.setVisibility(View.GONE);
        else
            settingLayout.setVisibility(View.VISIBLE);
    }

    public byte getPosition(){
        return position;
    }

        private void DialogLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.dialog_logout));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SignOut().execute();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    private class SignOut extends AsyncTask<String, Void, Integer> {
        private URL url;
        private HttpURLConnection con = null;
        private String base = DataInfo.url;
        private String request;
        private int responseCode;

        private ProgressDialog dialog;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
            dialog = new ProgressDialog(context);
            dialog.setMessage(getString(R.string.wait_logout));
            dialog.setCancelable(false);
            dialog.show();
        }

        protected Integer doInBackground(String... params) {
            signout();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            dialog.dismiss();
            if(result == HttpURLConnection.HTTP_OK) {
                Intent i = new Intent(activity, Login.class);
                startActivity(i);
                activity.finish();
            }
        }

        private void signout(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/auth/logout");

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(10000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                con.setDoOutput(true);
                con.getOutputStream().write(postDataBytes);

                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    UserDB udb = new UserDB(context);
                    udb.removeAll();
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    viewErrorDesc(getString(R.string.logout_error_0));
                    new AuthUser(context).execute();
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.logout_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.logout_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.logout_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.logout_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }

        private void viewErrorDesc(final String desc){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(context, desc, Toast.LENGTH_SHORT);
                    TextView v = toast.getView().findViewById(android.R.id.message);
                    if( v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }
            });
        }
    }

    private void setImageProfile(final ImageView imageView){
        if(user.getPhoto() != null){
            if(user.getPhoto() != 0) {
                if (!activity.isFinishing()) {
                    String url = DataInfo.url + "/user/profile/image?user_id=" + user.getUser_id()
                            + "&size=medium" + "&photo=" + user.getPhoto();
                    RequestOptions requestOptions =
                            new RequestOptions().placeholder(R.drawable.bg_photo);
                    Glide.with(activity).asBitmap().load(url).apply(requestOptions).into(imageView);
                }
            }else imageView.setBackgroundResource(R.drawable.bg_photo);
        } else imageView.setBackgroundResource(R.drawable.bg_photo);
    }

    public class GetInfoActivity extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode, statusActivity;
        private ObjectMapper mapper;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
        }

        protected Integer doInBackground(String... params){
            GetInfoActivity();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if(result == HttpURLConnection.HTTP_OK) {
                switch(statusCode){
                    case 220:
                        Double distanceKm = cDistance / 1000.00;
                        String distanceF = String.format("%.2f Km", distanceKm);
                        userListTV.get(5).setText(distanceF);
                        userListTV.get(6).setText(cActivity + " Activity");
                        break;
                    default:
                        viewErrorDesc(Profile.this.getString(R.string.get_feed_error_0));
                        break;
                }
            } else {
            }
        }

        private void GetInfoActivity(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);

                String request = HTTPTools.PostParamaters(params);
                url=new URL(base+"/activity/info?"+request);

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10000);
                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode==220){
                        statusActivity = rootnode.get("status_activity");
                        cActivity = statusActivity.get("cactivity").asText();
                        cDistance =  statusActivity.get("cdistance").asDouble();;
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                    viewErrorDesc(getString(R.string.get_profile_error_0));
                    new AuthUser(context).execute("");
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.get_profile_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.get_profile_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.get_profile_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.get_profile_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private class GetAllAddress extends AsyncTask<Integer, Void, Integer> {
        private String base = DataInfo.url;
        private String request;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private ArrayNode ANode;
        private Iterator<JsonNode> AIterator;
        private JsonNode rootnode, node;
        private ObjectMapper mapper;

        private int type;
        private int provinceId, cityId;

        private String tagError = "Sending to Server Error";

        protected void onPreExecute(){
        }

        protected Integer doInBackground(Integer... params){
            type = params[0];
            if(type==1)
                provinceId = params[1];
            getAllAddress();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode==100){
                    if(type==1 && cityList.size()!=0)
                        userListTV.get(2).setText(cityList.get(selectedCityId));
                }
            }
        }

        private void getAllAddress(){
            try {
                if(type==0){
                    request = "";
                    url=new URL(base+"/user/province"+request);
                } else if(type==1) {
                    request = "?provinceId="+provinceId;
                    url=new URL(base+"/user/cities"+request);
                } else if(type==2) {
                    request = "?cityId=" + cityId;
                    url=new URL(base+"/user/districts"+request);
                }

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(10000);

                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode == 100) {
                        if(type==0)
                            ANode = (ArrayNode) rootnode.get("province");
                        else if(type==1)
                            ANode = (ArrayNode) rootnode.get("cities");
                        else if(type==2)
                            ANode = (ArrayNode) rootnode.get("districts");
                        AIterator = ANode.elements();
                        if(ANode.size()!=0) {
                            while (AIterator.hasNext()) {
                                node = AIterator.next();
                                if(type==1)
                                    cityList.put(node.get("id").asInt(), node.get("name").asText());
                            }
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    new AuthUser(context).execute();
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void viewErrorDesc(final String desc){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(context, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }
}
