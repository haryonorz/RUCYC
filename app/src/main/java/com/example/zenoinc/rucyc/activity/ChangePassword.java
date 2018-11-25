package com.example.zenoinc.rucyc.activity;

import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.mobapphome.mahencryptorlib.MAHEncryptor;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {
    private ImageView back;
    private List<TextInputEditText> userListET;
    private List<TextInputLayout> userListLET;
    private Button save;
    private ProgressBar savePB;

    private UserDB udb;
    private User user;

    private int userEListResId[] = {R.id.currentPass_editText, R.id.newPass_editText, R.id.verifyPass_editText};

    private int userLListResId[] = {R.id.currentPass_layout, R.id.newPass_layout, R.id.verifyPass_layout};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        udb = new UserDB(this);
        user = udb.getUser();

        userListET = new ArrayList<>();
        userListLET = new ArrayList<>();

        for(int userResId : userEListResId)
            userListET.add((TextInputEditText) findViewById(userResId));
        for(int userLResId : userLListResId)
            userListLET.add((TextInputLayout) findViewById(userLResId));

        for (final TextInputEditText userET : userListET) {
            userET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 0)
                        userET.setBackgroundResource(R.drawable.input_edittext_error);
                    else
                        userET.setBackgroundResource(R.drawable.input_edittext);
                }
            });
        }

        back = (ImageView) findViewById(R.id.back_imageView);
        save = (Button) findViewById(R.id.save_button);
        savePB = (ProgressBar) findViewById(R.id.save_progressBar);

        initComponents();
    }

    private void initComponents() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean fillChecked = true;
                for(int i=0;i<3;i++){
                    if(userListET.get(i).getText().length()==0){
                        fillChecked = false;
                        userListET.get(i).setBackgroundResource(R.drawable.input_edittext_error);
                    }
                }

                if(!fillChecked){
                    viewErrorDesc(getString(R.string.not_filled));
                    return;
                }

                for(TextInputEditText userET : userListET)
                    userET.clearFocus();

                if(checkOldPassword(userListET.get(0).getText().toString())) {

                        new ChangePass().execute(
                                userListET.get(0).getText().toString(),
                                userListET.get(1).getText().toString()
                        );
                }else
                    viewErrorDesc(getString(R.string.password_not_match));
            }
        });
    }

    private void viewErrorDesc(final String desc){
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(ChangePassword.this, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }

    public boolean checkOldPassword(String password){
        try{
            MAHEncryptor mahEncryptor = MAHEncryptor.newInstance("RUCYCRUCYCRUCYCRUCYRUCY");
            password = mahEncryptor.encode(password);
            if(user.getPassword().equals(password))
                return true;
        } catch (Exception e){
            e.getStackTrace();
        }
        return false;
    }

    private class ChangePass extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private MAHEncryptor mahEncryptor;
        private JsonNode rootnode;
        private ObjectMapper mapper;

        private String password, npassword, vpassword;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
            SetProgressBarChangePass(true);
        }

        protected Integer doInBackground(String... params){
            password = params[0];
            npassword = params[1];
            changePass();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode==140){
                    udb.updatePassword(npassword);
                    viewErrorDesc(getString(R.string.change_password_success));
                    SuccessSending();
                } else if(statusCode==147) {
                    viewErrorDesc(getString(R.string.password_not_match));
                } else {
                    viewErrorDesc(getString(R.string.change_password_error_0));
                }
            }
            SetProgressBarChangePass(false);
        }

        private void changePass(){
            try {
                mahEncryptor = MAHEncryptor.newInstance("RUCYCRUCYCRUCYCRUCYRUCY");
                password = mahEncryptor.encode(password);
                npassword = mahEncryptor.encode(npassword);
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("password", password);
                params.put("npassword", npassword);

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/user/change/password");

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
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    viewErrorDesc(getString(R.string.change_password_error_0));
                    new AuthUser(ChangePassword.this).execute("");
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.change_password_error_0));
                    viewErrorDesc(getString(R.string.send_error));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.change_password_error_0));
                viewErrorDesc(getString(R.string.send_error));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.change_password_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.change_password_error_0));
                viewErrorDesc(getString(R.string.send_error));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void SuccessSending(){
        finish();
    }

    private void SetProgressBarChangePass(boolean b){
        if(b){
            save.setBackgroundResource(R.drawable.grey_button);
            save.setText("");
            savePB.setVisibility(View.VISIBLE);
        } else{
            save.setBackgroundResource(R.drawable.rucyc_button);
            save.setText(getString(R.string.save));
            savePB.setVisibility(View.GONE);
        }
    }
}
