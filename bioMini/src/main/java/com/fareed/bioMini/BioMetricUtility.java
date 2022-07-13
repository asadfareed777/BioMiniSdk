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
import android.widget.Toast;

import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class BioMetricUtility {

    private static BioMetricUtility bioMetricUtility = null;
    private IBioMiniDevice mCurrentDevice = null;
    public static final boolean mbUsbExternalUSBManager = false;
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final Activity mainContext;
    private ActionType actionType = null;
    private CaptureImageType captureImageType = null;
    private Fingers fingerType = Fingers.LeftThumb;
    private final BioMetricListener bioMetricListener;
    private final AttendanceListener attendanceListener;
    private final CustomImageListener customImageListener;
    private final DbHelper dbHelper;
    private BioMiniFactory mBioMiniFactory = null;
    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();
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
                    actionVerify(capturedImage,capturedTemplate);
                } else if (actionType == ActionType.ClockIn) {
                    actionClockIn(capturedImage,capturedTemplate);
                } else if (actionType == ActionType.ClockOut) {
                    actionClockOut(capturedImage,capturedTemplate);
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

    /**
     *  This callback handles custom capture like bmp,raw etc
     */
    private final CaptureResponder mCaptureResponseCustom = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {
            Log.i("BioMetric", "onCapture : Capture successful!");
            //enableButton(enroll);
            Log.i("BioMetric", ((IBioMiniDevice) context).popPerformanceLog());
            if ((capturedImage != null)) {
                if (captureImageType == CaptureImageType.CaptureImageAs19794) {
                    byte[] iso = mCurrentDevice.getCaptureImageAs19794_4();
                    customImageListener.customImageCaptureCompleted(iso,captureImageType, capturedImage, capturedTemplate);
                } else if (captureImageType == CaptureImageType.CaptureImageAsBmp) {
                    byte[] bmp = mCurrentDevice.getCaptureImageAsBmp();
                    customImageListener.customImageCaptureCompleted(bmp,captureImageType, capturedImage, capturedTemplate);
                }else if (captureImageType == CaptureImageType.CaptureImageAsRAW) {
                    byte[] raw = mCurrentDevice.getCaptureImageAsRAW_8();
                    customImageListener.customImageCaptureCompleted(raw,captureImageType,capturedImage,capturedTemplate);
                }else if (captureImageType == CaptureImageType.CaptureImageAsWsq) {
                  //  byte[] wsq = mCurrentDevice.getCaptureImageAsWsq(100,100,);
                   // customImageListener.customImageCaptureCompleted(wsq,captureImageType,capturedImage,capturedTemplate);
                    Toast.makeText(mainContext, "Coming soon", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }

        @Override
        public void onCaptureError(Object contest, int errorCode, String error) {
            Log.i("BioMetric", "onCaptureError : " + error + " ErrorCode :" + errorCode);
        }
    };

    /**
     *  This method is useful for check out
     */
    private void actionClockOut(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        // check if already exists
        boolean alreadyExists = isAlreadyExists(capturedTemplate);
        if (alreadyExists){
            attendanceListener.clockOutCompleted(capturedImage,capturedTemplate);
        }else {
            attendanceListener.showMessage("User is not registered");
        }
    }

    /**
     *  This method is useful for check in
     */
    private void actionClockIn(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        // check if already exists
        boolean alreadyExists = isAlreadyExists(capturedTemplate);
        if (alreadyExists){
            attendanceListener.clockInCompleted(capturedImage,capturedTemplate);
        }else {
            attendanceListener.showMessage("User is not registered");
        }
    }

    /**
     *  This method captures and verifies if user is enrolled already?
     */
    private void actionVerify(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        // check if already exists
        boolean alreadyExists = isAlreadyExists(capturedTemplate);
        if (alreadyExists){
            bioMetricListener.showMessage("This user already enrolled");
            bioMetricListener.verificationCompleted(capturedImage,capturedTemplate,true);
        }else {
            bioMetricListener.verificationCompleted(capturedImage,capturedTemplate,false);
            bioMetricListener.showMessage("This user is unidentified");
        }
    }

    /**
     *  This method captures and stores fingerprint in database
     */
    private void actionEnroll(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        // check if already exists
        boolean alreadyExists = isAlreadyExists(capturedTemplate);
        if (alreadyExists){
            bioMetricListener.showMessage("This user already enrolled");
        }else {
            dbHelper.addNewFingerPrint(capturedTemplate.data,fingerType.ordinal());
            bioMetricListener.enrollCompleted(capturedImage,capturedTemplate);
        }
    }

    /**
     *  This method checks if fingerprint already exists in local db
     */
    private boolean isAlreadyExists(IBioMiniDevice.TemplateData capturedTemplate) {
        boolean alreadyExists = false;
        List<byte[]> fingerprintList = dbHelper.getAllFingerPrint();
        for (int i = 0; i < fingerprintList.size(); i++) {
            byte[] fp = fingerprintList.get(i);
            if (mCurrentDevice != null) {
                if (mCurrentDevice.verify(capturedTemplate.data, fp)) {
                    alreadyExists = true;
                }
            }
        }
        return alreadyExists;
    }

    private void actionCapture(Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate) {
        bioMetricListener.captureCompleted(capturedImage,capturedTemplate);
    }

    private BioMetricUtility(Activity mainContext) {
        this.mainContext = mainContext;
        dbHelper = new DbHelper(mainContext);
        bioMetricListener = (BioMetricListener) mainContext;
        attendanceListener = (AttendanceListener) mainContext;
        customImageListener = (CustomImageListener) mainContext;
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

    /**
     *  This method checks all connected devices using usb manager
     */
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

    /**
     *  This method set parameters like SECURITY_LEVEL,TEMPLATE_TYPE,SENSITIVITY_LEVEL,TIMEOUT,
     *  SCANNING_MODE,FAST_MODE,ENABLE_AUTOSLEEP etc
     *  parameter = new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_TYPE, IBioMiniDevice.TemplateType.SUPREMA.value())
     */
    public void setCaptureParameter(IBioMiniDevice.Parameter parameter){
        if (mCurrentDevice != null){
            mCurrentDevice.setParameter(parameter);
        }
    }

    /**
     *  This method set TemplateType as ISO19794
     */
    public void setISO19794TemplateType(){
        if (mCurrentDevice != null){
            mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_TYPE,
                    IBioMiniDevice.TemplateType.ISO19794_2.value()));
        }
    }

    /**
     *  This method set TemplateType as ANSI378
     */
    public void setANSI378TemplateType(){
        if (mCurrentDevice != null){
            mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_TYPE,
                    IBioMiniDevice.TemplateType.ANSI378.value()));
        }
    }

    /**
     *  This method set TemplateType as SUPREMA
     */
    public void setSUPREMATemplateType(){
        if (mCurrentDevice != null){
            mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TEMPLATE_TYPE,
                    IBioMiniDevice.TemplateType.SUPREMA.value()));
        }
    }

    /**
     *  This method provides device details like deviceName,deviceSN,versionSDK
     *
     */
    public IBioMiniDevice.DeviceInfo getDeviceInfo(){
        if (mCurrentDevice != null){
            return mCurrentDevice.getDeviceInfo();
        }
        return null;
    }

    /**
     *  This method changes default LeftThumb to desired finger
     */
    public void setBiometricFinger(Fingers finger){
        fingerType = finger;
    }

    /**
     *  This method returns selected finger for authentication
     */
    public Fingers getSelectedBiometricFinger(){
        return fingerType;
    }

    /**
     *  This method set data of capture option
     * public int captureTimeout;
     * public boolean captureImage;
     * public boolean captureTemplate;
     * public FrameRate frameRate;
     */
    public void setCaptureOptions(IBioMiniDevice.CaptureOption captureOptions){
        this.mCaptureOptionDefault = captureOptions;
    }

    /**
     *  This method provides Biometric Sdk information
     */
    public String getSdkInfo(){
        if (mBioMiniFactory != null){
            return ""+mBioMiniFactory.getSDKInfo();
        }else {
            return "Something went wrong. Please try again later";
        }
    }

    /**
     *  This method provides last error code
     */
    public IBioMiniDevice.ErrorCode getLastErrorCode(){
        if (mCurrentDevice != null){
            return mCurrentDevice.getLastError();
        }else {
            return null;
        }
    }

    /**
     *  This method set encryption key for template data
     */
    public void setEncryptionKey(byte[] data){
        if (mCurrentDevice != null){
            mCurrentDevice.setEncryptionKey(data);
        }
    }

    /**
     *  This method encrypt template data
     */
    public byte[] encryptData(byte[] data){
        if (mCurrentDevice != null){
            return mCurrentDevice.encrypt(data);
        }else {
            return null;
        }
    }

    /**
     *  This method decrypt template data
     */
    public byte[] decryptData(byte[] data){
        if (mCurrentDevice != null){
            return mCurrentDevice.decrypt(data);
        }else {
            return null;
        }
    }

    /**
     *  This method checks whether device capturing or not
     */
    public boolean checkIsCapturing(byte[] data){
        if (mCurrentDevice != null){
            return mCurrentDevice.isCapturing();
        }else {
            return false;
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

    /**
     *  This method stops capturing fingerprint
     */
    public void abortAction() {
        if (mCurrentDevice != null) {
            // Abort capturing
            if (mCurrentDevice.isCapturing()) {
                mCurrentDevice.abortCapturing();
            }
        }
    }

    /**
     *  This method captures custom fingerprint types like bmp,raw,iso1979,wsq(inprogress) etc
     *  Note: This simply return capture data it does not handles db operations. You
     *  will need handle these by yourself. Db handles default type. Good luck
     */
    public void captureCustomFingerPrint(CaptureImageType captureImageType) {
        this.captureImageType = captureImageType;
        if (mCurrentDevice != null) {
            mCurrentDevice.captureSingle(
                    mCaptureOptionDefault,
                    mCaptureResponseCustom,
                    true);
        }
    }

    /**
     *  This method captures finger print and handles different features like
     *  simple capture,Enrollment,Verification,Check in,Check out, etc
     */
    public void captureFingerPrint(ActionType actionType) {
        this.actionType = actionType;
        if (mCurrentDevice != null) {
            mCurrentDevice.captureSingle(
                    mCaptureOptionDefault,
                    mCaptureResponseDefault,
                    true);
        }
    }

    public void getUsersAttendance() {
        // Coming Soon
    }

    public void getRegisteredFingerprints() {
        // Coming Soon
    }

    /**
     *  destroy created objects acts like onDestroy() of an activity
     */
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
