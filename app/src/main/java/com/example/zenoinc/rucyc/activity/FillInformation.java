
package com.example.zenoinc.rucyc.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FillInformation extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener{

    private List<TextInputEditText> infoListET;
    private List<TextInputLayout> infoListLET;
    private TextView nameTV;
    private Button saveBtn;
    private ProgressBar savePB;
    private Date birthdate;

    private Map<Integer, String> provinceList = new LinkedHashMap<>();
    private Map<Integer, String> cityList = new LinkedHashMap<>();
    private Map<Integer, String> districtList = new LinkedHashMap<>();
    private int selectedProvinceId, selectedCityId, selectedDistrictId;;
    private String gender;

    private UserDB udb;

    private int infoListResId[] = { R.id.date_birthdate_editText, R.id.month_birthdate_editText,
            R.id.year_birthdate_editText, R.id.weight_editText, R.id.height_editText, R.id.gender_editText,
            R.id.address_editText, R.id.province_editText, R.id.city_editText, R.id.district_editText};

    private int infoLListResId[] = { R.id.date_birthdate_layout, R.id.month_birthdate_layout,
            R.id.year_birthdate_layout, R.id.weight_layout, R.id.height_layout, R.id.gender_layout,
            R.id.address_layout, R.id.province_layout, R.id.city_layout, R.id.district_layout};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_information);
        if(DataInfo.token==null)
            new AuthUser(FillInformation.this).execute();
        initComponents();
    }

    private void initComponents() {
        infoListET = new ArrayList<>();
        infoListLET = new ArrayList<>();

        for(int i=0;i<infoListResId.length;i++) {
            infoListET.add((TextInputEditText) findViewById(infoListResId[i]));
            infoListLET.add((TextInputLayout) findViewById(infoLListResId[i]));
        }

        for(final TextInputEditText infoET : infoListET){
            infoET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.length()==0)
                        infoET.setBackgroundResource(R.drawable.input_edittext_error);
                    else
                        infoET.setBackgroundResource(R.drawable.input_edittext);
                }
                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }

        for(int i=0;i<3;i++){
            infoListET.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = new DatePickerDialog(
                            FillInformation.this, FillInformation.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH));
                    dpd.show();
                }
            });
        }

        savePB = findViewById(R.id.save_progressBar);

        saveBtn = findViewById(R.id.save_button);
        saveBtn.setOnClickListener(this);

        nameTV = findViewById(R.id.name_textView);

        udb = new UserDB(this);
        User user = udb.getUser();
        if(user==null)
            return;

        nameTV.setText(user.getFname());

        new GetAllAddress().execute(0);
        SetGenderDialog();
    }

    @Override
    public void onClick(View view) {
        boolean fillChecked = true;
        for(int i=0;i<infoListET.size();i++){
            if(infoListET.get(i).getText().length()==0){
                fillChecked = false;
                infoListET.get(i).setBackgroundResource(R.drawable.input_edittext_error);
            }
        }

        if(!fillChecked){
            viewErrorDesc(getString(R.string.not_filled));
            return;
        }

        for(TextInputEditText infoET : infoListET)
            infoET.clearFocus();

        new SendInformation().execute(
                infoListET.get(3).getText().toString(),
                infoListET.get(4).getText().toString(),
                infoListET.get(6).getText().toString()
        );
    }

    private void SetProgressBar(boolean b){
        if(b){
            saveBtn.setBackgroundResource(R.drawable.grey_button);
            saveBtn.setText("");
            savePB.setVisibility(View.VISIBLE);
        } else{
            saveBtn.setBackgroundResource(R.drawable.rucyc_button);
            saveBtn.setText(getString(R.string.save));
            savePB.setVisibility(View.GONE);
        }
    }

    private void SuccessSending(){
        startActivity(new Intent(FillInformation.this, Menu.class));
        finish();
    }

    private void SetGenderDialog(){
        infoListET.get(5).setEnabled(true);
        infoListET.get(5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(FillInformation.this);;
                builderSingle.setTitle(getString(R.string.select_gender));
                builderSingle.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK){
                            dialog.cancel();
                            return true;
                        }
                        return false;
                    }
                });
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        FillInformation.this, android.R.layout.select_dialog_singlechoice);
                arrayAdapter.add("Male");
                arrayAdapter.add("Female");
                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gender = arrayAdapter.getItem(which);
                        infoListET.get(5).setText(gender);
                    }
                });
                builderSingle.show();
            }
        });
        infoListET.get(5).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
    }

    private void SetProvinceDialog(){
        infoListET.get(7).setEnabled(true);
        infoListET.get(7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(FillInformation.this);;
                builderSingle.setTitle(getString(R.string.select_province));
                builderSingle.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK){
                            dialog.cancel();
                            return true;
                        }
                        return false;
                    }
                });
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        FillInformation.this, android.R.layout.select_dialog_singlechoice);
                for(Map.Entry<Integer, String> entry : provinceList.entrySet())
                    arrayAdapter.add(entry.getValue());
                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(Map.Entry<Integer, String> entry : provinceList.entrySet()){
                            if(arrayAdapter.getItem(which).equals(entry.getValue())){
                                selectedProvinceId = entry.getKey();
                                cityList.clear();
                                districtList.clear();
                                selectedCityId = -1;
                                selectedDistrictId = -1;
                                new GetAllAddress().execute(1, selectedProvinceId);
                                infoListET.get(7).setText(entry.getValue());
                                infoListET.get(8).setEnabled(false);
                                infoListET.get(9).setEnabled(false);
                                infoListET.get(8).setText("");
                                infoListET.get(8).setBackgroundResource(R.drawable.input_edittext);
                                infoListET.get(9).setText("");
                                infoListET.get(9).setBackgroundResource(R.drawable.input_edittext);
                                infoListET.get(8).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                infoListET.get(9).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                break;
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });
        infoListET.get(7).setEnabled(true);
        infoListET.get(7).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
    }

    private void SetCityDialog(){
        infoListET.get(8).setEnabled(true);
        infoListET.get(8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(FillInformation.this);;
                builderSingle.setTitle(getString(R.string.select_city));
                builderSingle.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK){
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        FillInformation.this, android.R.layout.select_dialog_singlechoice);
                for(Map.Entry<Integer, String> entry : cityList.entrySet())
                    arrayAdapter.add(entry.getValue());
                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(Map.Entry<Integer, String> entry : cityList.entrySet()){
                            if(arrayAdapter.getItem(which).equals(entry.getValue())){
                                selectedCityId = entry.getKey();
                                districtList.clear();
                                selectedDistrictId = -1;
                                infoListET.get(8).setText(entry.getValue());
                                infoListET.get(9).setEnabled(false);
                                infoListET.get(9).setText("");
                                infoListET.get(9).setBackgroundResource(R.drawable.input_edittext);
                                infoListET.get(9).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                new GetAllAddress().execute(2, selectedCityId);
                                break;
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });
        infoListET.get(8).setEnabled(true);
        infoListET.get(8).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
    }

    private void SetDistrictDialog() {
        infoListET.get(9).setEnabled(true);
        infoListET.get(9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(FillInformation.this);
                builderSingle.setTitle(getString(R.string.select_district));
                builderSingle.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        FillInformation.this, android.R.layout.select_dialog_singlechoice);
                for (Map.Entry<Integer, String> entry : districtList.entrySet())
                    arrayAdapter.add(entry.getValue());
                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Map.Entry<Integer, String> entry : districtList.entrySet()) {
                            if (arrayAdapter.getItem(which).equals(entry.getValue())) {
                                selectedDistrictId = entry.getKey();
                                infoListET.get(9).setText(entry.getValue());
                                break;
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });
        infoListET.get(9).setEnabled(true);
        infoListET.get(9).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        try {
            birthdate = inFormat.parse(year + "/" + (month + 1) + "/" + day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        infoListET.get(0).setText(String.valueOf(day));
        String[] nameMonth = getResources().getStringArray(R.array.name_of_month);
        infoListET.get(1).setText(nameMonth[month]);
        infoListET.get(2).setText(String.valueOf(year));
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
            else if(type==2)
                cityId = params[1];
            getAllAddress();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode==100){
                    if(type==0 && provinceList.size()!=0){
                        SetProvinceDialog();
                    } else if(type==1 && cityList.size()!=0) {
                        SetCityDialog();
                    } else if(type==2 && districtList.size()!=0) {
                        SetDistrictDialog();
                    }
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
                                if(type==0)
                                    provinceList.put(node.get("id").asInt(), node.get("name").asText());
                                else if(type==1)
                                    cityList.put(node.get("id").asInt(), node.get("name").asText());
                                else if(type==2)
                                    districtList.put(node.get("id").asInt(), node.get("name").asText());

                            }
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    new AuthUser(FillInformation.this).execute();
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

    private class SendInformation extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode;
        private ObjectMapper mapper;

        private String weight, height, address;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
            SetProgressBar(true);
        }

        protected Integer doInBackground(String... params){
            weight = params[0];
            height = params[1];
            address = params[2];
            sendInformation();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode==110){
                    udb.updateFillUserDB(birthdate, weight, height, gender, address, selectedProvinceId, selectedCityId,
                            selectedDistrictId);
                    SuccessSending();
                } else {
                    viewErrorDesc(FillInformation.this.getString(R.string.fill_error_0));
                }
            }
            SetProgressBar(false);
        }

        private void sendInformation(){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthdate);
            String sBirthDate = String.valueOf(calendar.get(Calendar.YEAR));
            sBirthDate += "/" + String.valueOf(calendar.get(Calendar.MONTH)+1);
            sBirthDate += "/" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("birthdate", sBirthDate);
                params.put("weight", weight);
                params.put("height", height);
                params.put("gender", gender);
                params.put("address", address);
                params.put("province", selectedProvinceId);
                params.put("city", selectedCityId);
                params.put("district", selectedDistrictId);

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/user/fillinfo");

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
                    viewErrorDesc(getString(R.string.fill_error_0));
                    new AuthUser(FillInformation.this).execute();
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.fill_error_0));
                    Log.e(tagError, String.valueOf(responseCode));
                    viewErrorDesc(getString(R.string.send_error));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.fill_error_0));
                viewErrorDesc(getString(R.string.send_error));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.fill_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.fill_error_0));
                viewErrorDesc(getString(R.string.send_error));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void viewErrorDesc(final String desc){
        FillInformation.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(FillInformation.this, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }
}
