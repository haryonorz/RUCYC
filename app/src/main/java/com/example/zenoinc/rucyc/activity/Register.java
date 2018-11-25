package com.example.zenoinc.rucyc.activity;

import android.content.Intent;
import android.net.Uri;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private List<TextInputEditText> registerListET;
    private List<TextInputLayout> registerListLET;
    private CheckBox registerCB;
    private ProgressBar registerPB;
    private Button registerBtn;

    private UserDB udb;

    private boolean agree = false;

    private int registerListResId[] = {R.id.fname_editText, R.id.lname_editText,
            R.id.email_editText, R.id.password_editText};
    private int registerLListResId[] = {R.id.fname_layout, R.id.lname_layout,
            R.id.email_layout, R.id.password_layout};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseApp.initializeApp(this);
        initComponents();
    }

    private void initComponents() {
        TextView agreementTextView = findViewById(R.id.agreement_TextView);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.register_agreement_description));
        builder.append(" ");
        SpannableString spannableString1 = new SpannableString(getString(R.string.register_agreement_user));
        spannableString1.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.colorGStart2)), 0,
                spannableString1.length(), 0);
        builder.append(spannableString1);
        builder.append(" ");
        builder.append(getString(R.string.register_agreement_and));
        builder.append(" ");
        SpannableString spannableString2 = new SpannableString(getString(R.string.register_agreement_privacy));
        spannableString2.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.colorGStart2)), 0,
                spannableString2.length(), 0);
        builder.append(spannableString2);
        agreementTextView.setText(builder, TextView.BufferType.SPANNABLE);
        agreementTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DataInfo.url + "/agreement")));
            }
        });

        registerListET = new ArrayList<>();
        registerListLET = new ArrayList<>();

        for (int i = 0; i < registerListResId.length; i++) {
            registerListET.add((TextInputEditText) findViewById(registerListResId[i]));
            registerListLET.add((TextInputLayout) findViewById(registerLListResId[i]));
        }

        for (final TextInputEditText registerET : registerListET) {
            registerET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 0)
                        registerET.setBackgroundResource(R.drawable.input_edittext_error);
                    else
                        registerET.setBackgroundResource(R.drawable.input_edittext);
                }
            });
        }
        registerCB = findViewById(R.id.agreement_CheckBox);
        registerCB.setOnCheckedChangeListener(this);
        registerPB = findViewById(R.id.register_progressBar);

        registerBtn = findViewById(R.id.register_Button);
        registerBtn.setOnClickListener(this);

        udb = new UserDB(this);
    }

    @Override
    public void onClick(View v) {
        boolean fillChecked = true;
        for (int i = 0; i < registerListET.size(); i++) {
            if (registerListET.get(i).getText().length() == 0) {
                fillChecked = false;
                registerListET.get(i).setBackgroundResource(R.drawable.input_edittext_error);
            }
        }

        if (!fillChecked) {
            viewErrorDesc(getString(R.string.not_filled));
            return;
        }

        if (!agree) {
            viewErrorDesc(getString(R.string.register_agreement_not_checked));
            return;
        }

        for (TextInputEditText registerET : registerListET)
            registerET.clearFocus();

        new RegisteringUser().execute(
                registerListET.get(0).getText().toString(),
                registerListET.get(1).getText().toString(),
                registerListET.get(2).getText().toString(),
                registerListET.get(3).getText().toString());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean b) {
        if (b) {
            registerBtn.setEnabled(true);
            registerBtn.setBackgroundResource(R.drawable.rucyc_button);
        } else {
            registerBtn.setEnabled(false);
            registerBtn.setBackgroundResource(R.drawable.grey_button);
        }
        agree = b;
    }

    private void SetProgressBar(boolean b) {
        if (b) {
            registerBtn.setBackgroundResource(R.drawable.rucyc_button);
            registerBtn.setText("");
            registerPB.setVisibility(View.VISIBLE);
        } else {
            registerBtn.setBackgroundResource(R.drawable.rucyc_button);
            registerBtn.setText(getString(R.string.next));
            registerPB.setVisibility(View.GONE);
        }
    }

    private void SuccessRegistering() {
        new SetFCMToken(Register.this).execute(FirebaseInstanceId.getInstance().getToken());
        Intent intent = new Intent(Register.this, FillInformation.class);
        finishAffinity();
        startActivity(intent);
    }

    private class RegisteringUser extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private MAHEncryptor mahEncryptor;
        private JsonNode rootnode;
        private ObjectMapper mapper;

        private String uid, fname, lname, email, password, token;

        private String tagError = "Sending to Server Error";

        protected void onPreExecute() {
            SetProgressBar(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            fname = params[0];
            lname = params[1];
            email = params[2];
            password = params[3];
            registeringUser();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode==100){
                    udb.insertNewUserDB(uid, fname, lname, email, password, token);
                    DataInfo.user_id = uid;
                    DataInfo.token = token;
                    SuccessRegistering();
                } else if(statusCode==101){
                    viewErrorDesc(getString(R.string.email_exists));
                } else if(statusCode==106) {
                    viewErrorDesc(getString(R.string.email_password_error));
                    viewErrorDesc(getString(R.string.email_hint));
                    viewErrorDesc(getString(R.string.password_hint));
                } else {
                    viewErrorDesc(Register.this.getString(R.string.register_error_0));
                }
            }
            SetProgressBar(false);
        }

        private void registeringUser(){
            try {
                mahEncryptor = MAHEncryptor.newInstance("RUCYCRUCYCRUCYCRUCYRUCY");
                password = mahEncryptor.encode(password);
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("fname", fname);
                params.put("lname", lname);
                params.put("email", email);
                params.put("password", password);
                params.put("type", "user");

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/user/signup");
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
                    if(statusCode == 100) {
                        uid = rootnode.get("user_id").asText();
                        token = rootnode.get("token").asText();
                    }
                } else {
                    viewErrorDesc(Register.this.getString(R.string.register_error_0));
                    viewErrorDesc(Register.this.getString(R.string.send_error));
                }

            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Register.this.getString(R.string.register_error_0));
                viewErrorDesc(Register.this.getString(R.string.send_error));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Register.this.getString(R.string.register_error_0));
                viewErrorDesc(Register.this.getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(Register.this.getString(R.string.register_error_0));
                viewErrorDesc(Register.this.getString(R.string.send_error));
            } finally {
                if(con != null) con.disconnect();
            }
        }

    }

    private void viewErrorDesc(final String desc){
        Register.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(Register.this, desc, Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }
}
