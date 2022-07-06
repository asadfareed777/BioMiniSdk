package com.fareed.bioMini;

import android.graphics.Bitmap;

import com.suprema.IBioMiniDevice;

public interface CustomImageListener {
    void customImageCaptureCompleted(byte[] raw, CaptureImageType captureImageType, Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate);
}
