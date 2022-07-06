package com.example.biominisdk;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fareed.bioMini.BioMetricListener;
import com.fareed.bioMini.BioMetricUtility;
import com.fareed.bioMini.Fingers;
import com.fareed.bioMini.Utils;
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
        // Capture just capture FP. it does not save in db
        findViewById(R.id.btn_capture).setOnClickListener(view -> {
            bioMetricUtility.setBiometricFinger(Fingers.LeftIndex);
            bioMetricUtility.captureFingerPrint();
        });
        // Capture just capture FP and saves in local db
        findViewById(R.id.btn_enroll).setOnClickListener(view -> {
            bioMetricUtility.enrollFingerPrint();
        });
        // Verifies a user
        findViewById(R.id.btn_verify).setOnClickListener(view -> {
            bioMetricUtility.verifyFingerPrint();
        });
        // Check in
        findViewById(R.id.btn_clock_in).setOnClickListener(view -> {
            bioMetricUtility.clockInUser();
        });
        // Check out
        findViewById(R.id.btn_clock_out).setOnClickListener(view -> {
            bioMetricUtility.clockOutUser();
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
        Toast.makeText(this, "Fingerprint Enrolled Successfully", Toast.LENGTH_SHORT).show();
        Glide.with(this).load(capturedImage).into(imageViewFingerPrint);
        // Perform Custom action here
        bioMetricUtility.setCaptureParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SECURITY_LEVEL, IBioMiniDevice.TemplateType.SUPREMA.value()));
    }

    @Override
    public void verificationCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate, boolean registeredUser) {
        Toast.makeText(this, "Fingerprint Verified Successfully", Toast.LENGTH_SHORT).show();
        Glide.with(this).load(capturedImage).into(imageViewFingerPrint);
        // Perform Custom action here
        if (registeredUser){

        }
    }

    @Override
    public void clockInCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        Toast.makeText(this, "Fingerprint Clocked In Successfully", Toast.LENGTH_SHORT).show();
        Glide.with(this).load(capturedImage).into(imageViewFingerPrint);
        // Perform Custom action here
        String currentTime = Utils.getCurrentTime();
    }

    @Override
    public void clockOutCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        Toast.makeText(this, "Fingerprint Clocked In Successfully", Toast.LENGTH_SHORT).show();
        Glide.with(this).load(capturedImage).into(imageViewFingerPrint);
        // Perform Custom action here
        String currentTime = Utils.getCurrentTime();
    }

    @Override
    public void getAllFingerPrints() {

    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}