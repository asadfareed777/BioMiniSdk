package com.fareed.bioMini;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Log;

import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class BioMetricUtility {

    private static BioMetricUtility bioMetricUtility = null;
    private IBioMiniDevice mCurrentDevice = null;
    public static final boolean mbUsbExternalUSBManager = false;
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Activity mainContext;
    private ActionType actionType = null;
    private BioMetricListener bioMetricListener;
    private DbHelper dbHelper;
    private BioMiniFactory mBioMiniFactory = null;
    private final IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();
    private final CaptureResponder mCaptureResponseDefault = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {
            Log.i("BioMetric", "onCapture : Capture successful!");
            //enableButton(enroll);
            Log.i("BioMetric", ((IBioMiniDevice) context).popPerformanceLog());
            if ((capturedImage != null)) {
                if (actionType == ActionType.Capture) {
                    actionCapture(capturedImage,capturedTemplate);
                } else if (actionType == ActionType.Enroll) {
                    actionEnroll(capturedImage,capturedTemplate);
                } else if (actionType == ActionType.Verify) {
                    // actionVerify();
                } else if (actionType == ActionType.ClockIn) {
                    //  actionClockIn();
                } else if (actionType == ActionType.ClockOut) {
                    //  actionClockOut();
                } else if (actionType == ActionType.FetchAll) {
                    //  actionFetchAll();
                }
            }
            return true;
        }

        @Override
        public void onCaptureError(Object contest, int errorCode, String error) {
            Log.i("BioMetric", "onCaptureError : " + error + " ErrorCode :" + errorCode);
        }
    };

    private void actionEnroll(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        dbHelper.addNewFingerPrint(capturedTemplate.data);
        bioMetricListener.enrollCompleted(capturedImage,capturedTemplate);
    }

    private void actionCapture(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        bioMetricListener.captureCompleted(capturedImage,capturedTemplate);
    }

    private BioMetricUtility(Activity mainContext) {
        this.mainContext = mainContext;
        dbHelper = new DbHelper(mainContext);
        bioMetricListener = (BioMetricListener) mainContext;
        mCaptureOptionDefault.frameRate = IBioMiniDevice.FrameRate.SHIGH;
        if (mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        restartBioMini();
    }

    public static BioMetricUtility getInstance(Activity mainContext) {
        if (bioMetricUtility == null) {
            return bioMetricUtility = new BioMetricUtility(mainContext);
        } else {
            return bioMetricUtility;
        }
    }

    private void restartBioMini() {
        if (mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        if (mbUsbExternalUSBManager) {
            mUsbManager = (UsbManager) mainContext.getSystemService(Context.USB_SERVICE);
            mBioMiniFactory = new BioMiniFactory(mainContext, mUsbManager) {
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    Log.i("BioMetric", "----------------------------------------");
                    Log.i("BioMetric", "Fingerprint Scanner Changed : " + event + " using external usb-manager");
                    Log.i("BioMetric", "----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
            //
            mPermissionIntent = PendingIntent.getBroadcast(mainContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            mainContext.registerReceiver(mUsbReceiver, filter);
            checkDevice();
        } else {
            mBioMiniFactory = new BioMiniFactory(mainContext) {
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    Log.i("BioMetric", "----------------------------------------");
                    Log.i("BioMetric", "Fingerprint Scanner Changed : " + event);
                    Log.i("BioMetric", "----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
        }
    }

    private void checkDevice() {
        if (mUsbManager == null) return;
        Log.i("BioMetric", "checkDevice");
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        while (deviceIter.hasNext()) {
            UsbDevice _device = deviceIter.next();
            if (_device.getVendorId() == 0x16d1) {
                //Suprema vendor ID
                mUsbManager.requestPermission(_device, mPermissionIntent);
            } else {

            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            if (mBioMiniFactory == null) return;
                            mBioMiniFactory.addDevice(device);
                            Log.i("BioMetric", String.format(Locale.ENGLISH, "Initialized device count- BioMiniFactory (%d)", mBioMiniFactory.getDeviceCount()));
                        }
                    } else {
                        Log.i("BioMetric", "permission denied for device" + device);
                    }
                }
            }
        }
    };

    private void handleDevChange(IUsbEventHandler.DeviceChangeEvent event, Object dev) {
        if (event == IUsbEventHandler.DeviceChangeEvent.DEVICE_ATTACHED && mCurrentDevice == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    while (mBioMiniFactory == null && cnt < 20) {
                        SystemClock.sleep(1000);
                        cnt++;
                    }
                    if (mBioMiniFactory != null) {
                        mCurrentDevice = mBioMiniFactory.getDevice(0);
                        Log.i("BioMetric", "Hardware Attached : " + mCurrentDevice);
                        if (mCurrentDevice != null /*&& mCurrentDevice.getDeviceInfo() != null*/) {
                            //enableButton(capture);
                            Log.i("BioMetric", " DeviceName : " + mCurrentDevice.getDeviceInfo().deviceName);
                            Log.i("BioMetric", "         SN : " + mCurrentDevice.getDeviceInfo().deviceSN);
                            Log.i("BioMetric", "SDK version : " + mCurrentDevice.getDeviceInfo().versionSDK);
                        }
                    }
                }
            }).start();
        } else if (mCurrentDevice != null && event == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && mCurrentDevice.isEqual(dev)) {
            //disableButton(capture);
            Log.i("BioMetric", "Fingerprint Scanner removed : " + mCurrentDevice);
            mCurrentDevice = null;
        }
    }

    public void abortAction() {
        if (mCurrentDevice != null) {
            // Abort capturing
            if (mCurrentDevice.isCapturing()) {
                mCurrentDevice.abortCapturing();
            }
        }
    }

    public void captureFingerPrint() {
        actionType = ActionType.Capture;
        if (mCurrentDevice != null) {
            //mCaptureOptionDefault.captureTimeout = (int)mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.TIMEOUT).value;
            mCurrentDevice.captureSingle(
                    mCaptureOptionDefault,
                    mCaptureResponseDefault,
                    true);
        }
    }

    public void enrollFingerPrint() {
        actionType = ActionType.Enroll;
        if (mCurrentDevice != null) {
            mCurrentDevice.captureSingle(
                    mCaptureOptionDefault,
                    mCaptureResponseDefault,
                    true);
        }
    }

    public void verifyFingerPrint() {
        // Coming Soon
    }

    public void clockInUser() {
        // Coming Soon
    }

    public void clockOutUser() {
        // Coming Soon
    }

    public void getUsersAttendance() {
        // Coming Soon
    }

    public void getRegisteredFingerprints() {
        // Coming Soon
    }

    public void destructEverything() {
        if (mBioMiniFactory != null) {
            mBioMiniFactory.close();
            mBioMiniFactory = null;
        }
        if (mbUsbExternalUSBManager) {
            mainContext.unregisterReceiver(mUsbReceiver);
        }
    }
}
