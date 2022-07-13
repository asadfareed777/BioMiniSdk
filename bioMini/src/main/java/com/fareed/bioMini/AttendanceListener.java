package com.fareed.bioMini;

import android.graphics.Bitmap;

import com.suprema.IBioMiniDevice;

public interface AttendanceListener {
    void clockInCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void clockOutCompleted(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
    void showMessage(String message);
}
