package com.example.biominisdk;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fareed.bioMini.BioMetricListener;
import com.fareed.bioMini.BioMetricUtility;
import com.fareed.bioMini.DbHelper;
import com.suprema.IBioMiniDevice;

public class MainActivity extends AppCompatActivity implements BioMetricListener {

    private BioMetricUtility bioMetricUtility;
    private ImageView imageViewFingerPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bioMetricUtility = BioMetricUtility.getInstance(this);
        imageViewFingerPrint = findViewById(R.id.iv_fingerprint);
        setClickListeners();
    }

    private void setClickListeners() {
        findViewById(R.id.btn_capture).setOnClickListener(view -> {
            bioMetricUtility.captureFingerPrint();
        });
    }


    @Override
    protected void onDestroy() {
        bioMetricUtility.destructEverything();
        super.onDestroy();
    }

    @Override
    public void captureCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        Toast.makeText(this, "Fingerprint Captured Successfully", Toast.LENGTH_SHORT).show();
        Glide.with(this).load(capturedImage).into(imageViewFingerPrint);
    }

    @Override
    public void enrollCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {

    }

    @Override
    public void verificationCompleted() {

    }

    @Override
    public void clockInCompleted() {

    }

    @Override
    public void clockOutCompleted() {

    }

    @Override
    public void getAllFingerPrints() {

    }
}