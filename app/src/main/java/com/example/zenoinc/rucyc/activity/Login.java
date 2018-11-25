package com.example.zenoinc.rucyc.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.connection.SetFCMToken;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mobapphome.mahencryptorlib.MAHEncryptor;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Login extends AppCompatActivity implements View.OnClickListener{
    private List<TextInputEditText> loginListET;
    private List<TextInputLayout> loginListLET;
    private ProgressBar loginPB;
    private Button loginBtn;
    private int loginListResId[] = { R.id.email_editText, R.id.password_editText};
    private int loginLListResId[] = { R.id.email_layout, R.id.password_layout};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        initComponents();
    }

    private void initComponents(){
        TextView registerTextView = findViewById(R.id.register_textView);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString spannableString =  new SpannableString(getString(R.string.login_dont_have_account));
        spannableString.setSpan(
                new StyleSpan(Typeface.NORMAL), 0,
                spannableString.length(), 0);
        builder.append(spannableString);
        builder.append(" ");
        spannableString = new SpannableString(getString(R.string.login_register_here));
        spannableString.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.colorGStart2)),0,
                spannableString.length(), 0);
        builder.append(spannableString);
        registerTextView.setText(builder, TextView.BufferType.SPANNABLE);
        loginListET = new ArrayList<>();
        loginListLET = new ArrayList<>();
        for(int i=0;i<loginListResId.length;i++) {
            loginListET.add((TextInputEditText) findViewById(loginListResId[i]));
            loginListLET.add((TextInputLayout) findViewById(loginLListResId[i]));
        }
        for(final TextInputEditText loginET : loginListET){
            loginET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    if(editable.length()==0)
                        loginET.setBackgroundResource(R.drawable.input_edittext_error);
                    else
                        loginET.setBackgroundResource(R.drawable.input_edittext);
                }
            });
        }
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        loginPB = findViewById(R.id.login_progressBar);

        loginBtn = findViewById(R.id.login_button);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        boolean fillChecked = true;
        for(int i=0;i<loginListET.size();i++){
            if(loginListET.get(i).getText().length()==0){
                fillChecked = false;
                loginListET.get(i).setBackgroundResource(R.drawable.input_edittext_error);
            }
        }
        if(!fillChecked){
            viewErrorDesc(getString(R.string.not_filled));
            return;
        }
        for(TextInputEditText loginET : loginListET)
            loginET.clearFocus();
        new LoginUser().execute(
                loginListET.get(0).getText().toString(),
                loginListET.get(1).getText().toString());
    }

    private void SetProgressBar(boolean b){
        if(b){
            loginBtn.setBackgroundResource(R.drawable.grey_button);
            loginBtn.setText("");
            loginPB.setVisibility(View.VISIBLE);
        } else{
            loginBtn.setBackgroundResource(R.drawable.rucyc_button);
            loginBtn.setText(getString(R.string.signIn));
            loginPB.setVisibility(View.GONE);
        }
    }

    private void FillInformation(){
        startActivity(new Intent(Login.this, FillInformation.class));
        finish();
    }

    private void SuccessLogin(){
        startActivity(new Intent(Login.this, Menu.class));
        finish();
    }

    private void viewErrorDesc(final String desc){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(Login.this, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }

    public class LoginUser extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;
        private int uStatusCode;
        private URL url;
        private HttpURLConnection con = null;
        private MAHEncryptor mahEncryptor;
        private JsonNode rootnode, addressNode, profileNode;
        private ObjectMapper mapper;
        private String email, password;
        private UserDB udb;
        private String tagError = "Sending to Server Error";
        protected  void onPreExecute(){
            SetProgressBar(true);
            udb = new UserDB(Login.this);
        }
        @Override
        protected Integer doInBackground(String... params) {
            email = params[0];
            password = params[1];
            loginUser();
            return responseCode;
        }
        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode == 130 || statusCode == 0) {
                    if(uStatusCode == 0)
                        FillInformation();
                    else if(uStatusCode == 1)
                        SuccessLogin();
                    new SetFCMToken(Login.this).execute(FirebaseInstanceId.getInstance().getToken());
                }
            }
            SetProgressBar(false);
        }
        private void loginUser(){
            try {
                mahEncryptor = MAHEncryptor.newInstance("RUCYCRUCYCRUCYCRUCYRUCY");
                password = mahEncryptor.encode(password);

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
                            viewErrorDesc(Login.this.getString(R.string.login_blacklist));
                            viewErrorDesc(Login.this.getString(R.string.login_blacklist1));
                            uStatusCode = 3;
                        } else if(userStatus.equals("incomplete")){
                            DataInfo.token = rootnode.get("token").asText();
                            uStatusCode = 0;
                            getUserProfile();
                        } else {
                            DataInfo.token = rootnode.get("token").asText();
                            uStatusCode = 1;
                            getUserProfile();
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    viewErrorDesc(Login.this.getString(R.string.login_wrong_email_password));
                } else {
                    viewErrorDesc(Login.this.getString(R.string.error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Login.this.getString(R.string.login_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Login.this.getString(R.string.login_error_0));
                viewErrorDesc(Login.this.getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Login.this.getString(R.string.login_error_0));

            } finally {
                if(con != null) con.disconnect();
            }
        }
        private void getUserProfile(){
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
                        profileNode = rootnode.get("profile");

                        udb.insertNewUserDB(
                                profileNode.get("user_id").asText(),
                                profileNode.get("fname").asText(),
                                profileNode.get("lname").asText(),
                                email, password, DataInfo.token);

                        if(uStatusCode != 0){
                            Date birthdate = new Date();
                            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                            try {
                                birthdate = inFormat.parse(profileNode.get("birthdate").asText());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                            Log.e("Test", "lewat");
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
                            Log.e("Test", "lewat");
                        }
                    }
                } else {
                    viewErrorDesc(Login.this.getString(R.string.login_error_1));
                    Log.e(tagError, String.valueOf(responseCode));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Login.this.getString(R.string.login_error_1));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Login.this.getString(R.string.login_error_1));
                viewErrorDesc(Login.this.getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Login.this.getString(R.string.login_error_1));
            }
        }
    }
}
