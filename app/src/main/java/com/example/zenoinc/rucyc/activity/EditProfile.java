package com.example.zenoinc.rucyc.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.db.User;
import com.example.zenoinc.rucyc.db.UserDB;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yalantis.ucrop.UCrop;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

public class EditProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private UserDB udb;
    private User user;

    private ImageView back,changeProfilePhoto;
    private List<TextInputEditText> userListET;
    private List<TextInputLayout> userListLET;
    private String gender;
    private Button save;
    private ProgressBar savePB;

    private File photoFile;
    private Date birthdate;

    public static final int CAMERA_REQUEST = 3;
    public static final int GALLERY_REQUEST = 4;

    private Map<Integer, String> provinceList = new LinkedHashMap<>();
    private Map<Integer, String> cityList = new LinkedHashMap<>();
    private Map<Integer, String> districtList = new LinkedHashMap<>();
    private int selectedProvinceId, selectedCityId, selectedDistrictId;

    private int userEListResId[] = {R.id.fname_editText, R.id.lname_editText, R.id.email_editText,
            R.id.date_birthdate_editText, R.id.month_birthdate_editText, R.id.year_birthdate_editText,
            R.id.height_editText, R.id.weight_editText, R.id.gender_editText,
            R.id.address_editText,R.id.province_editText, R.id.city_editText,
            R.id.district_editText};

    private int userLListResId[] = { R.id.fname_layout, R.id.lname_layout, R.id.email_layout,
            R.id.date_birthdate_layout, R.id.month_birthdate_layout, R.id.year_birthdate_layout,
            R.id.height_layout, R.id.weight_layout, R.id.gender_layout,
            R.id.address_layout, R.id.province_layout, R.id.city_layout,
            R.id.district_layout};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
        changeProfilePhoto = (ImageView) findViewById(R.id.photoProfile_imageView);
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

        user = udb.getUser();
        birthdate = user.getBirthdate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(user.getBirthdate());

        setImageProfile(changeProfilePhoto);
        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDialog();
            }
        });

        userListET.get(0).setText(user.getFname());
        userListET.get(1).setText(user.getLname());
        userListET.get(2).setText(user.getEmail());

        for(int i=3;i<6;i++){
            userListET.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = new DatePickerDialog(
                            EditProfile.this, EditProfile.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH));
                    dpd.show();
                }
            });
        }
        userListET.get(3).setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        String[] nameMonth = getResources().getStringArray(R.array.name_of_month);
        userListET.get(4).setText(nameMonth[calendar.get(Calendar.MONTH)]);
        userListET.get(5).setText(String.valueOf(calendar.get(Calendar.YEAR)));
        userListET.get(6).setText(user.getHeight());
        userListET.get(7).setText(user.getWeight());
        userListET.get(9).setText(user.getAddress());

        selectedProvinceId = user.getProvince();
        selectedCityId = user.getCity();
        selectedDistrictId = user.getDistrict();
        new GetAllAddress().execute(0);
        new GetAllAddress().execute(1, selectedProvinceId);
        new GetAllAddress().execute(2, selectedCityId);
        SetGenderDialog();

        for(int i=3;i<12;i++)
            userListET.get(i).setBackgroundResource(R.drawable.input_edittext);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean fillChecked = true;
                for(int i=0;i<13;i++){
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

                new ChangeProfile().execute(
                        userListET.get(0).getText().toString(),
                        userListET.get(1).getText().toString(),
                        userListET.get(2).getText().toString(),
                        userListET.get(6).getText().toString(),
                        userListET.get(7).getText().toString(),
                        userListET.get(8).getText().toString(),
                        userListET.get(9).getText().toString()
                );
            }
        });
    }

    private void SetGenderDialog(){
        userListET.get(8).setText(user.getGender());
        userListET.get(8).setEnabled(true);
        userListET.get(8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(EditProfile.this);;
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
                        EditProfile.this, android.R.layout.select_dialog_singlechoice);
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
                        userListET.get(8).setText(gender);
                    }
                });
                builderSingle.show();
            }
        });
        userListET.get(8).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
    }

    private class ChangeProfile extends AsyncTask<String, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode;
        private ObjectMapper mapper;

        private String fname, lname, email, weight, height, gender, address;

        private String tagError = "Sending to Server Error";

        protected  void onPreExecute(){
            SetProgressBarProfile(true);
        }

        protected Integer doInBackground(String... params){
            fname = params[0];
            lname = params[1];
            email = params[2];
            height = params[3];
            weight = params[4];
            gender = params[5];
            address = params[6];
            changeProfile();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK){
                if(statusCode==120){
                    udb.updateProfileUserDB(fname, lname, email, birthdate, weight, height, gender, address, selectedProvinceId,
                            selectedCityId, selectedDistrictId );
                    viewErrorDesc(getString(R.string.change_profile_success));
                    SuccessSending();
                } else if(statusCode==121){
                    viewErrorDesc(getString(R.string.email_hint));
                } else if(statusCode==122){
                    viewErrorDesc(getString(R.string.email_exists));
                } else {
                    viewErrorDesc(getString(R.string.change_profile_error_0));
                }
            }
            SetProgressBarProfile(false);
        }

        private void changeProfile(){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthdate);
            String sBirthDate = String.valueOf(calendar.get(Calendar.YEAR));
            sBirthDate += "/" + String.valueOf(calendar.get(Calendar.MONTH)+1);
            sBirthDate += "/" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("fname", fname);
                params.put("lname", lname);
                params.put("email", email);
                params.put("birthdate", sBirthDate);
                params.put("weight", weight);
                params.put("height", height);
                params.put("gender", gender);
                params.put("address", address);
                params.put("province", selectedProvinceId);
                params.put("city", selectedCityId);
                params.put("district", selectedDistrictId);

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/user/change/profile");

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
                    viewErrorDesc(getString(R.string.change_profile_error_0));
                    new AuthUser(EditProfile.this).execute("");
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.change_profile_error_0));
                    viewErrorDesc(getString(R.string.send_error));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.change_profile_error_0));
                viewErrorDesc(getString(R.string.send_error));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.change_profile_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.change_profile_error_0));
                viewErrorDesc(getString(R.string.send_error));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void SuccessSending(){
        finish();
    }

    private void SetProgressBarProfile(boolean b){
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

    private void viewErrorDesc(final String desc){
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(EditProfile.this, desc, Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }

    private void setDialog() {
        final Dialog dialog = new Dialog(EditProfile.this);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(R.color.colorTransparent);
        View view = LayoutInflater.from(EditProfile.this).inflate(R.layout.dialog_all, null);
        dialog.setContentView(view);
        ListView list = dialog.findViewById(R.id.listview_dialog);
        String[] items = {getString(R.string.gallery), getString(R.string.camera)};
        list.setAdapter(new ArrayAdapter<>(EditProfile.this, R.layout.list_dialog, items));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();
                if(position==0)
                    checkPermission(GALLERY_REQUEST);
                else
                    checkPermission(CAMERA_REQUEST);

            }
        });
        TextView cancelBtn = dialog.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void checkPermission(int requestCode){
        switch(requestCode){
            case GALLERY_REQUEST:
            case CAMERA_REQUEST:
                if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                    ActivityCompat.requestPermissions(EditProfile.this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, requestCode);
                } else if(requestCode==GALLERY_REQUEST){
                    gallery();
                } else {
                    camera();
                }
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch(requestCode){
            case GALLERY_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    gallery();
                break;
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    camera();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST) {
                String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File uCrop = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/uCrop");
                if(!uCrop.exists())
                    if(!uCrop.mkdir()) return;
                Uri destinationUri = Uri.parse(uCrop.toString() + File.separator + imageFileName);
                Uri selectedImageUri;
                if(requestCode == GALLERY_REQUEST)
                    selectedImageUri = data.getData();
                else
                    selectedImageUri = Uri.fromFile(getPhotoFile());
                UCrop.Options options = new UCrop.Options();
                options.setCompressionQuality(70);
                UCrop.of(selectedImageUri, destinationUri)
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(1024,1024)
                        .withOptions(options)
                        .start(this);
            } else if (requestCode == UCrop.REQUEST_CROP) {
               uploadImage(UCrop.getOutput(data));
            }
        } else if(resultCode == RESULT_CANCELED && requestCode == CAMERA_REQUEST){
            File imageFile = getPhotoFile();
            if(!imageFile.delete())
                Log.e("Error", "Error saat menghapus file : " + imageFile.getAbsoluteFile());
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.e("Error", "Error saat menghapus file : " + cropError.getMessage());
        }
    }

    public File getPhotoFile(){
        return photoFile;
    }

    public void uploadImage(Uri image){
        new ImageUpload().execute(image);
    }

    public void gallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    public void camera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            getString(R.string.file_provider_authority), photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            } catch (IOException e) {
                e.getStackTrace();
            }
        }
    }

    private void setImageProfile(final ImageView imageView){
        if(user.getPhoto() != null){
            if(user.getPhoto() != 0)
                if(!isFinishing()) {
                    String url = DataInfo.url + "/user/profile/image?user_id=" + user.getUser_id()
                            + "&size=medium" + "&photo=" + user.getPhoto();
                    RequestOptions requestOptions =
                            new RequestOptions().placeholder(R.drawable.bg_photo);
                    Glide.with(this).asBitmap().load(url).apply(requestOptions).into(imageView);
                }
                else imageView.setBackgroundResource(R.drawable.bg_photo);
        } else imageView.setBackgroundResource(R.drawable.bg_photo);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        try {
            birthdate = inFormat.parse(year + "/" + (month + 1) + "/" + day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        userListET.get(3).setText(String.valueOf(day));
        String[] nameMonth = getResources().getStringArray(R.array.name_of_month);
        userListET.get(4).setText(nameMonth[month]);
        userListET.get(5).setText(String.valueOf(year));
    }

    public static File createImageFile() throws IOException {
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storagedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Rucyc/");
        if (! storagedir.exists())
            if (! storagedir.mkdirs()) return null;
        return File.createTempFile(imageFileName, ".jpg", storagedir);
    }

    public class ImageUpload extends AsyncTask<Uri, Void, Integer> {
        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private JsonNode rootnode;
        private ObjectMapper mapper;
        private final ProgressDialog dialog;

        private int photo;

        private String tagError = "Sending to Server Error";

        private ImageUpload(){
            dialog = new ProgressDialog(EditProfile.this);
        }

        protected  void onPreExecute(){
            dialog.setMessage(getString(R.string.uploading_photo));
            dialog.setCancelable(false);
            dialog.show();
        }

        protected Integer doInBackground(Uri... params) {
            upload(params[0]);
            return null;
        }

        protected void onPostExecute(Integer result) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if(statusCode == 150) {
                    udb.updatePhotoUserDB(photo);
                    user = udb.getUser();
                    setImageProfile(changeProfilePhoto);
                }
            }
            dialog.dismiss();
        }

        private void upload(Uri imageUri){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);

                String request = HTTPTools.PostParamaters(params);

                url = new URL(base+"/user/profile/upload?"+request);

                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(10000);

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap bm = MediaStore.Images.Media.getBitmap(EditProfile.this.getContentResolver(), imageUri);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
                builder.addPart("image", new ByteArrayBody(bos.toByteArray(),fileName));

                HttpEntity entity = builder.build();

                con.setRequestProperty("Content-Length", String.valueOf(entity.getContentLength()));
                con.setRequestProperty("Connection", "Keep-Alive");
                con.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                entity.writeTo(con.getOutputStream());
                os.close();
                con.connect();

                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode == 150){
                        photo = rootnode.get("photo").asInt();
                    }
                }else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    viewErrorDesc(getString(R.string.uploading_photo_error_0));
                    new AuthUser(EditProfile.this).execute();
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.uploading_photo_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.uploading_photo_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.uploading_photo_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.uploading_photo_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void SetProvinceDialog(){
        userListET.get(10).setEnabled(true);
        userListET.get(10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(EditProfile.this);;
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
                        EditProfile.this, android.R.layout.select_dialog_singlechoice);
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
                                userListET.get(10).setText(entry.getValue());
                                userListET.get(11).setEnabled(false);
                                userListET.get(12).setEnabled(false);
                                userListET.get(11).setText("");
                                userListET.get(11).setBackgroundResource(R.drawable.input_edittext);
                                userListET.get(11).setText("");
                                userListET.get(12).setBackgroundResource(R.drawable.input_edittext);
                                userListET.get(12).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                userListET.get(12).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                break;
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });
        userListET.get(10).setEnabled(true);
        userListET.get(10).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
        userListET.get(10).setText(provinceList.get(selectedProvinceId));
    }

    private void SetCityDialog(){
        userListET.get(11).setEnabled(true);
        userListET.get(11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(EditProfile.this);
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
                        EditProfile.this, android.R.layout.select_dialog_singlechoice);
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
                                userListET.get(11).setText(entry.getValue());
                                userListET.get(12).setEnabled(false);
                                userListET.get(12).setText("");
                                userListET.get(12).setBackgroundResource(R.drawable.input_edittext);
                                userListET.get(12).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                new GetAllAddress().execute(2, selectedCityId);
                                break;
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });
        userListET.get(11).setEnabled(true);
        userListET.get(11).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
        userListET.get(11).setText(cityList.get(selectedCityId));
    }

    private void SetDistrictDialog() {
        userListET.get(12).setEnabled(true);
        userListET.get(12).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(EditProfile.this);
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
                        EditProfile.this, android.R.layout.select_dialog_singlechoice);
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
                                userListET.get(12).setText(entry.getValue());
                                break;
                            }
                        }
                    }
                });
                builderSingle.show();
            }
        });
        userListET.get(12).setEnabled(true);
        userListET.get(12).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_spinner, 0);
        userListET.get(12).setText(districtList.get(selectedDistrictId));
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
                    new AuthUser(EditProfile.this).execute();
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
}
