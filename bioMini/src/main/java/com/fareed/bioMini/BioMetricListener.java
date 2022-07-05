package com.fareed.bioMini;

import android.graphics.Bitmap;

import com.suprema.IBioMiniDevice;

public interface BioMetricListener {
    void captureCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void enrollCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void verificationCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate, boolean b);
    void clockInCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void clockOutCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void getAllFingerPrints();
    void showMessage(String message);
}
