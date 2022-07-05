package com.fareed.bioMini;

import android.graphics.Bitmap;

import com.suprema.IBioMiniDevice;

public interface BioMetricListener {
    void captureCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void enrollCompleted();
    void verificationCompleted();
    void clockInCompleted();
    void clockOutCompleted();
    void getAllFingerPrints();
}
