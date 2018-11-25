package com.example.zenoinc.rucyc.utilities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    private Activity activity;

    public File imageFile;

    public ImageUtils(Activity activity){
        this.activity = activity;
    }

    public File createImageFile() throws IOException {
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storagedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Rucyc/");
        if (! storagedir.exists())
            if (! storagedir.mkdirs()) return null;
        return File.createTempFile(imageFileName, ".jpg", storagedir);
    }

    // untuk apa?
    public Bitmap createBitmap(String imagepath) throws IOException {
        return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), Uri.parse(imagepath));
    }

    // untuk apa?
    public void storeImage(Bitmap image, String prefix, String filename, int quality) {
        File imageFile = getOutputImageFile(prefix, filename);

        if (imageFile == null) {
            Log.e("ERROR", "Error saat membuat file, cek perizinan : ");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("ERROR","File tidak ditemukan : " + e.getMessage());
        } catch (IOException e) {
            Log.e("ERROR","Error saat mengakses file : " + e.getMessage());
        }
    }

    private  File getOutputImageFile(String prefix, String filename){
        File storageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + activity.getApplicationContext().getPackageName()
                + "/files/" + prefix);

        if (! storageDir.exists()){
            if (! storageDir.mkdirs()){
                return null;
            }
        }

        File imageFile;
        imageFile = new File(storageDir.getPath() + "/" + filename);
        return imageFile;
    }

}

