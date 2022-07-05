package com.example.biominisdk;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.fareed.bioMini.BioMetricListener;
import com.fareed.bioMini.BioMetricUtility;
import com.suprema.IBioMiniDevice;

public class MainActivity extends AppCompatActivity implements BioMetricListener {

    private BioMetricUtility bioMetricUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bioMetricUtility = BioMetricUtility.getInstance(this);
    }


    @Override
    protected void onDestroy() {
        bioMetricUtility.destructEverything();
        super.onDestroy();
    }

    @Override
    public void captureCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {

    }

    @Override
    public void enrollCompleted() {

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