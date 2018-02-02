package com.txbdc.repairmvp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Cole on 11/7/2017.
 */

public class MainPresenter implements MVP_Main.RequiredPresenterOps, MVP_Main.ProvidedPresenterOps {

    String TAG = "presenter";
    boolean thighZeroed, shinZeroed, footZeroed = false;
    boolean zeroingThigh, zeroingShin, zeroingFoot = false;
    float thighZeroValue, shinZeroValue, footZeroValue = 0;

    int zeroCountThigh, zeroCountShin, zeroCountFoot = 0;
    BleModel bleModel;
    Boolean isBound = true;
    Boolean recording = false;
    private final int thigh = 1;
    private final int shin = 2;
    private final int foot = 3;
    private final int pcm = 4;
    private final int gattConnected = 2;
    private final int gattDisconnected = 3;
    private int doubleClickCount = 0;
    boolean recordingAudio = false;

    boolean fileCreated = false;
    String fileName = "trialData_";
    String randomID = "0001_";
    String path = "/storage/emulated/0/";
    String string = "Hello World!";
    String dateTime = DateFormat.getDateTimeInstance().format(new Date());
    String fileType = ".txt";
    String fullPath = path+fileName+fileType;
    String fullPathData = path+fileName+"_data"+fileType;
    ArrayList<String> hipAngles = new ArrayList<String>();
    ArrayList<String> hipAcceleration = new ArrayList<String>();
    ArrayList<String> kneeAngles = new ArrayList<String>();
    ArrayList<String> kneeAcceleration = new ArrayList<String>();
    ArrayList<String> ankleAngles = new ArrayList<String>();
    ArrayList<String> ankleAcceleration = new ArrayList<String>();
    ArrayList<String> sensorData = new ArrayList<String>();





    // View reference. We use as a WeakReference
    // because the Activity could be destroyed at any time
    // and we don't want to create a memory leak
    private WeakReference<MVP_Main.RequiredViewOps> mView;
    Handler timerHandler =  new Handler();

    // Model reference
    //private MVP_Main.ProvidedModelOps mModel;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleModel.BleBinder binder = (BleModel.BleBinder) service;
            bleModel = binder.getService();
            isBound = true;
            bleModel.initializeBle();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleModel = null;
            isBound = false;
        }
    };

    /**
     * Presenter Constructor
     * @param view  MainActivity
     */
    public MainPresenter(MVP_Main.RequiredViewOps view) {
        mView = new WeakReference<>(view);
        Intent bleIntent = new Intent(getActivityContext(), BleModel.class);
        getActivityContext().startService(bleIntent);
        getActivityContext().bindService(bleIntent, mServiceConnection, getActivityContext().BIND_AUTO_CREATE);
        getActivityContext().registerReceiver(modelUpdateReceiver, new IntentFilter("bleModel"));
    }

    /**
     * Return the View reference.
     * Throw an exception if the View is unavailable.
     */
    private MVP_Main.RequiredViewOps getView() throws NullPointerException{
        if ( mView != null )
            return mView.get();
        else
            throw new NullPointerException("View is unavailable");
    }

    @Override
    public Context getAppContext() {
        try {
            return getView().getAppContext(); //mView.getAppContext();
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public Context getActivityContext() {
        try {
            return getView().getActivityContext(); //mView.getActivityContext();
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public void mainActivityStopped() {
        if(bleModel.checkIfScanning()){
            bleModel.stopScan();
        }
        //bleModel.unbindService(mServiceConnection);

    }

    @Override
    public void clickSensorButton(int sensor) {
        //before doing anything with the scanner make sure that the sensor button that was pressed is not already connected
        //check gatt connection status
        int gattStatus = bleModel.checkSensorStatus(sensor);
        if (gattStatus == gattConnected) {
            if(sensor == 4){
                if(!ffStatus){
                    triggerFirefly(startStim);
                    ffStatus = true;
                    timerHandler.postDelayed(stopStimulate, 2000);
                }

            }
            // whatever this button does if its already connected
            // this could do nothing if hold click tares and double click disconnects gatt
            doubleClickCount++;
            timerHandler.postDelayed(refreshDoubleClick, 500);
            if(doubleClickCount == 2){
                Log.v(TAG, "twice");
                if(sensor ==  thigh){
                    bleModel.disconnectSensor(sensor);
                    getView().thighDisconnected();
                }
                else if(sensor == shin){
                    bleModel.disconnectSensor(sensor);
                    getView().shinDisconnected();
                }
                else if(sensor == foot){
                    bleModel.disconnectSensor(sensor);
                    getView().footDisconnected();
                }
                // we want to say
                // getView().disconnected(sensor)
                doubleClickCount = 0;
            }
        }
        else {
            boolean scanning = bleModel.checkIfScanning();
            if (scanning) {
                // dont restart scan but send model the new sensor if necessary
                int previousSensor = bleModel.changeSensorToConnect(sensor);
                //tell the view what to change
                if (sensor != previousSensor) {
                    if (sensor == thigh) {
                        getView().searchingForThigh();
                    } else if (sensor == shin) {
                        getView().searchingForShin();
                    } else if (sensor == foot) {
                        getView().searchingForFoot();
                    }
                    else if (sensor == pcm){
                        getView().searchingForFF();
                    }
                    if (previousSensor == thigh) {
                        getView().notSearchingForThigh();
                    } else if (previousSensor == shin) {
                        getView().notSearchingForShin();
                    } else if (previousSensor == foot) {
                        getView().notSearchingForFoot();
                    }
                    else if (previousSensor == pcm){
                        //TODO: handle pcm view change
                    }
                }
                else{
                    bleModel.stopScan();
                    if(sensor == thigh){
                        getView().notSearchingForThighText();
                    }
                    else if(sensor == shin){
                        getView().notSearchingForShinText();
                    }
                    else if(sensor == foot){
                        getView().notSearchingForFootText();
                    }
                    else if(sensor == pcm){
                        getView().setTextDefault();
                    }
                }
            }
            else{
                bleModel.searchForBleDevice(sensor);
                if (sensor == thigh) {
                    getView().searchingForThigh();
                }
                if (sensor == shin) {
                    getView().searchingForShin();
                }
                if (sensor == foot) {
                    getView().searchingForFoot();
                }
                if (sensor == pcm){
                    getView().searchingForFF();
                }
            }
        }
    }

    @Override
    public void clickSensorButton(SensorView view, int sensor){
        getView().searchingForDevice(view);
        bleModel.searchForBleDevice(sensor);
    }

    @Override
    public void clickRecord() {
        if(recording){
            recordingAudio = false;
            getView().stopRecording();
        }
        else{
            recording = true;
            recordingAudio = true;
            getView().startRecording();
            //start runnable that collects and saves arrays every 30 seconds
        }
    }
    public static final byte[] startStim = {12, 1, 2, 3, 4, 60, 0, 0, 24, 83, 12, 13, (byte)0xc1};
    public static final byte[] stopStim = {2, 0, 0};
    boolean ffStatus = false;
    @Override
    public void clickPCM() {
        int fireflyConnected = checkSensorStatus(4);
        if (fireflyConnected == 0){
            bleModel.searchForBleDevice(4);
            getView().searchingForFF();
        }
        else if(fireflyConnected == 2){
            Log.v(TAG, "stimulate");
            if(!ffStatus){
                triggerFirefly(startStim);
                ffStatus = true;
                timerHandler.postDelayed(stopStimulate, 2000);
            }

            //TODO: stimulate ff
        }
    }

    @Override
    public void disconnectFF() {
        if(checkSensorStatus(4) == 2){
            bleModel.disconnectSensor(4);
        }
    }

    public void triggerFirefly(byte[] onOff)
    {
        if(checkSensorStatus(4) == 2){
            bleModel.FIREFLY_CHARACTERISTIC2.setValue(onOff);
            bleModel.triggerFF();
        }
    }

    Runnable stopStimulate = new Runnable() {
        @Override
        public void run() {
            triggerFirefly(stopStim);
            ffStatus = false;
        }
    };

    @Override
    public void sendViewToPresenterTest(SensorView sensor) {

    }

    @Override
    public boolean getAudioRecordingStatus(){
        return recordingAudio;
    }

    @Override
    public int checkSensorStatus(int sensor) {
        return bleModel.checkSensorStatus(sensor);
    }

    Runnable refreshDoubleClick = new Runnable(){
        @Override
        public void run(){
            doubleClickCount = 0;
        }
    };

    @Override
    public void zeroSensor(int sensor) {
        if(sensor == thigh){
            zeroCountThigh = 0;
            zeroingThigh = true;
            //so that the logic will allow for rezeroing the sensors
            thighZeroed = false;
            thighZeroValue = 0.0f;
            //possibly need to add a call to zero the progress bars in the view
        }
        else if(sensor == shin){
            zeroCountShin = 0;
            zeroingShin = true;
            shinZeroed = false;
            shinZeroValue = 0.0f;
        }
        else if(sensor == foot){
            zeroCountFoot = 0;
            zeroingFoot = true;
            footZeroed = false;
            footZeroValue = 0.0f;
        }
    }



    public void charValueUpdate(int sensor, float value) {
        //Log.v(TAG, "sensor no. " + sensor + " value: " + value);
        if (sensor == thigh){
            if(thighZeroed){
                //find value's difference from thighZeroValue
                value = findCalibratedValue(thighZeroValue, value);
                //Log.v(TAG, "calibrated value " + value);
                //decide if the value goes on the positive or negative side
                // and send value to view
                if(value >= 0){
                    getView().displayPositiveThighValue(value);
                }
                else if(value < 0){
                    getView().displayNegativeThighValue(value);
                }

            }
            else if(zeroingThigh){
                if(zeroCountThigh < 10){
                    thighZeroValue = thighZeroValue + value;
                    zeroCountThigh++;
                }
                else {
                    thighZeroValue = thighZeroValue/10.0f;
                    zeroingThigh = false;
                    thighZeroed = true;
                    Log.v(TAG, "zero value thigh = " + thighZeroValue);
                }
            }
            else {
                //do nothing because we do not want to send raw values to the view
            }
        }
        else if (sensor == shin){
            if(shinZeroed){
                //find value's difference from shinZeroValue
                value = findCalibratedValue(shinZeroValue, value);
                //decide if the value goes on the positive or negative side
                // and send value to view
                if(value >= 0){
                    getView().displayPositiveShinValue(value);
                }
                else if(value < 0){
                    getView().displayNegativeShinValue(value);
                }

            }
            else if (zeroingShin){
                if(zeroCountShin < 10) {
                    shinZeroValue = shinZeroValue + value;
                    zeroCountShin++;
                }
                else {
                    shinZeroValue = shinZeroValue/10.0f;
                    zeroingShin = false;
                    shinZeroed = true;
                }
            }
            else{
                //do nothing because we do not want to send raw values to the view
            }
        }
        else if(sensor == foot){
            if(footZeroed){
                //find value's difference from footZeroValue
                value = findCalibratedValue(footZeroValue, value);
                //decide if the value goes on the positive or negative side
                // and send value to view
                if(value >= 0){
                    getView().displayPositiveFootValue(value);
                }
                else if(value < 0){
                    getView().displayNegativeFootValue(value);
                }

            }
            else if(zeroingFoot){
                if(zeroCountFoot < 10){
                    footZeroValue = footZeroValue + value;
                    zeroCountFoot++;
                }
                else{
                    footZeroValue = footZeroValue/10.0f;
                    zeroingFoot = false;
                    footZeroed = true;
                }
            }
            else{
                //do nothing because we do not want to send raw values to the view
            }
        }
    }
    public float findCalibratedValue(float zeroValue, float rawValue){
        float calibratedValue = 0;
        if((zeroValue+90.0f) <= 180.0f & (zeroValue - 90.0f) >= -180.0f){
            calibratedValue = (rawValue + (-1.0f*zeroValue));
        }
        else if((zeroValue+90.0f) > 180.0f){
            if (rawValue < 0 ){
                calibratedValue = (180.0f - zeroValue) + (rawValue + 180.0f);
            }
            else if(rawValue > 0){
                calibratedValue = (rawValue + (-1.0f*zeroValue));
            }
        }
        else if((zeroValue-90.0f) < -180.0f){
            if(rawValue < 0 ){
                calibratedValue = (rawValue + (-1.0f*zeroValue));
            }
            if(rawValue > 0){
                calibratedValue = (-180.0f - zeroValue) + (rawValue - 180.0f);
            }
        }
        return calibratedValue;
    }

    private BroadcastReceiver modelUpdateReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String eventType = extras.getString("bleEvent");
            if(eventType.equals("sensorConnected")){
                if(extras.getString("gatt").equals("hip")){
                    getView().thighConnected();
                    getView().setLongClickActionThigh();
                }
                if(extras.getString("gatt").equals("knee")){
                    getView().shinConnected();
                    getView().setLongClickActionShin();
                }
                if(extras.getString("gatt").equals("ankle")){
                    getView().footConnected();
                    getView().setLongClickActionFoot();
                }
                if(extras.getString("gatt").equals("firefly")){
                    getView().fireflyConnected();
                    getView().setLongClickFirefly();
                }
                if (extras.getString("gatt").equals("unknown")) {
                    Log.v(TAG, "unknown gatt");
                }
                Log.v("sensorService", "connected message sent");
            }
            if(eventType.equals("sensorDisconnected")){
                if(extras.getString("gatt").equals("hip")){
                    getView().thighDisconnected();
                }
                if(extras.getString("gatt").equals("knee")){
                    getView().shinDisconnected();
                }
                if(extras.getString("gatt").equals("ankle")){
                    getView().footDisconnected();
                }
                if(extras.getString("gatt").equals("firefly")){
                    getView().fireflyDisconnected();
                }
            }
            if(eventType.equals("notification")){
                BleNotification notification = intent.getParcelableExtra("notifyObject");
                if(notification.gatt.equals("hip")){
                    charValueUpdate(thigh,notification.eulerX);
                    if (recording){
                        sensorData.add("\n1");
                        sensorData.add( Float.toString(notification.eulerX));
                        sensorData.add( Float.toString(notification.eulerY));
                        sensorData.add( Float.toString(notification.eulerZ));
                        sensorData.add( Float.toString(notification.accX));
                        sensorData.add( Float.toString(notification.accY));
                        sensorData.add( Float.toString(notification.accZ));
                        sensorData.add(Long.toString(System.currentTimeMillis()));


                    }
                }
                else if(notification.gatt.equals("knee")){
                    charValueUpdate(shin,notification.eulerX);
                    if (recording){
                        sensorData.add("\n2");
                        sensorData.add( Float.toString(notification.eulerX));
                        sensorData.add( Float.toString(notification.eulerY));
                        sensorData.add( Float.toString(notification.eulerZ));
                        sensorData.add( Float.toString(notification.accX));
                        sensorData.add( Float.toString(notification.accY));
                        sensorData.add( Float.toString(notification.accZ));
                        sensorData.add(Long.toString(System.currentTimeMillis()));
                    }
                }
                else if(notification.gatt.equals("ankle")){
                    charValueUpdate(foot,notification.eulerX);
                    if (recording){
                        sensorData.add("\n3");
                        sensorData.add( Float.toString(notification.eulerX));
                        sensorData.add( Float.toString(notification.eulerY));
                        sensorData.add( Float.toString(notification.eulerZ));
                        sensorData.add( Float.toString(notification.accX));
                        sensorData.add( Float.toString(notification.accY));
                        sensorData.add( Float.toString(notification.accZ));
                        sensorData.add(Long.toString(System.currentTimeMillis()));


                    }
                }

            }
            if(eventType.equals("scanStopped")){
                //setSensorStatus("PCM Scan Timeout");
            }
        }
    };

    public void writeFileAtStop(String string, int sensor){
        try {
            FileOutputStream outputStream = new FileOutputStream(fullPath, true);
            String data = string;
            if (sensor == 5){
                data = data.concat(sensorData.toString());
            }
            if(sensor == thigh){
               // if(string.equals("hipAcceleration = ")){
                    //data = data.concat(hipAcceleration.toString());
                //}
               // else{
                    data = data.concat(hipAngles.toString());

                //}
            }
            if(sensor == shin){
              //  if(string.equals("kneeAcceleration = ")){
                    //data = data.concat(kneeAcceleration.toString());
              //  }
               // else{
                    data = data.concat(kneeAngles.toString());

                //}
            }
            if(sensor == foot){
                //if(string.equals("ankleAcceleration = ")){
                    //data = data.concat(ankleAcceleration.toString());
               // }
               // else{
                    data = data.concat(ankleAngles.toString());

               // }
            }
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(getAppContext(),new String[]{fullPath},null,null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearDataBuffers() {
        hipAngles.clear();
        hipAcceleration.clear();
        kneeAcceleration.clear();
        kneeAngles.clear();
        ankleAcceleration.clear();
        ankleAngles.clear();
        sensorData.clear();
    }

    @Override
    public void triggerFF() {
        Log.v(TAG, "stimulate");
        if(!ffStatus){
            triggerFirefly(startStim);
            ffStatus = true;
            timerHandler.postDelayed(stopStimulate, 2000);
        }
    }

    public void writeFile(String fileName, String notes){
        recording = false;
        try {
            fullPath = path+fileName+fileType;
            //fullPathData = path + fileName + "_data" + fileType;
            FileOutputStream outputStream = new FileOutputStream(fullPath);
            string = "file created on: " + dateTime + "\n" + notes + "\n";
            outputStream.write(string.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
