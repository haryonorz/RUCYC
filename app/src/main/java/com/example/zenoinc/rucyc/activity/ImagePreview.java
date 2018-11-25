package com.example.zenoinc.rucyc.activity;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.zenoinc.rucyc.R;

public class ImagePreview extends AppCompatActivity {
    private ProgressBar pbimg;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        ImageView img = findViewById(R.id.img);
        pbimg = findViewById(R.id.pbimg);

        Bundle extras = getIntent().getExtras();
        if (extras != null) url = extras.getString("url");

        if(url!=null){
            url = url.replace("small", "large");
            url = url.replace("medium", "large");
        }

        if(url != null){
            if(!isFinishing())
                Glide.with(this).load(url).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target,
                                                boolean isFirstResource) {
                        pbimg.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        pbimg.setVisibility(View.GONE);
                        return false;
                    }
                }).into(img);
        }
    }
}
