package com.txbdc.repairmvp;

/**
 * Created by Cole on 12/13/2017.
 */
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.content.ServiceConnection;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailsPresenter implements DetailsInterface.PresenterToBleModel, DetailsInterface.PresenterToView {
    private WeakReference<DetailsInterface.ViewToPresenter> mView;

    BleModel bleModel;
    Boolean isBound = true;

    String TAG = "details presenter";

    android.os.Handler timerHandler = new android.os.Handler();

    public DetailsPresenter(DetailsInterface.ViewToPresenter view) {
        mView = new WeakReference<>(view);
        Intent bleIntent = new Intent(getActivityContext(), BleModel.class);
        //getActivityContext().startService(bleIntent);
        getActivityContext().bindService(bleIntent, mServiceConnection, getActivityContext().BIND_AUTO_CREATE);
    }

    /**
     * Return the View reference.
     * Throw an exception if the View is unavailable.
     */
    private DetailsInterface.ViewToPresenter getView() throws NullPointerException{
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

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleModel.BleBinder binder = (BleModel.BleBinder) service;
            bleModel = binder.getService();
            isBound = true;
            getView().addDevicesToApproved();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleModel = null;
            isBound = false;
        }
    };

    @Override
    public String getApprovedDevice(int position) {
        return bleModel.approvedDevices[position];

    }

    @Override
    public void activityStopped() {
        bleModel.detailsStopped();
        bleModel.stopScan();
        if (mServiceConnection != null) {
            //bleModel.unbindService(mServiceConnection);
        }
    }

    @Override
    public void startDetailsScan() {
        if (isBound) {
            if (bleModel.checkIfScanning()) {
                bleModel.stopScan();
                bleModel.setSearchingFromDetails(false);

            }
            if (!bleModel.checkIfScanning()) {
                Log.v("details", "starting scan");
                getView().startedButtonText();
                timerHandler.postDelayed(scanStop, 3000);
                bleModel.setSearchingFromDetails(true);
                bleModel.searchForBleDevice();
                timerHandler.postDelayed(scanStop, 5000);
                for (int i = 0; i > bleModel.shockclockCount; i++) {
                    bleModel.deviceIDs[i] = null;
                }
                bleModel.shockclockCount = 0;
            }
        }
    }

    Runnable scanStop = new Runnable() {
        @Override
        public void run() {
            if (bleModel.checkIfScanning()) {
                getView().stoppedButtonText();
                bleModel.stopScan();
                Log.v(TAG, "scan stopped");
            }
            bleModel.searchingFromDetails = false;
            List<String> input = new ArrayList<>();
            for (int i = 0; i < bleModel.shockclockCount; i++) {
                input.add("Sensor " + bleModel.deviceIDs[i]);
            }// define an adapter
            for (int i = 0; i < bleModel.pcmCount; i++) {
                input.add(bleModel.pcmIDs[i]);
            }
            getView().updateRecyclerView(input);
        }
    };


    @Override
    public void setNewApprovedDevice(final String newDevice) {
        if(newDevice.contains("PCM")){
           if (getView().getValueOfApprovedPCM(0).equals("1")) {
                getView().setApprovedPCMText(0, newDevice);
            }
            else if (getView().getValueOfApprovedPCM(1).equals("2")) {
                getView().setApprovedPCMText(1, newDevice);
            }
            else if (getView().getValueOfApprovedPCM(2).equals("3")) {
                getView().setApprovedPCMText(2, newDevice);
            }
            else {
                String dev2 = getView().getValueOfApprovedPCM(0);
                String dev3 = getView().getValueOfApprovedPCM(1);
                getView().setApprovedPCMText(1, dev2);
                getView().setApprovedPCMText(2, dev3);
               getView().setApprovedPCMText(0, newDevice);
            }
            bleModel.approvedDevices[5] = getView().getValueOfApprovedPCM(0);
            bleModel.approvedDevices[6] = getView().getValueOfApprovedPCM(1);
            bleModel.approvedDevices[7] = getView().getValueOfApprovedPCM(2);
        }
        else{
            if (getView().getValueOfApprovedDevice(0).equals("1")) {
                getView().setApprovedDeviceText(0, newDevice);
            }
            else if (getView().getValueOfApprovedDevice(1).equals("2")) {
                getView().setApprovedDeviceText(1, newDevice);
            }
            else if (getView().getValueOfApprovedDevice(2).equals("3")) {
                getView().setApprovedDeviceText(2, newDevice);
            }
            else if (getView().getValueOfApprovedDevice(3).equals("4")) {
                getView().setApprovedDeviceText(3, newDevice);
            }
            else if (getView().getValueOfApprovedDevice(4).equals("5")) {
                getView().setApprovedDeviceText(4, newDevice);
            }
            else {
                String dev2 = getView().getValueOfApprovedDevice(0);
                String dev3 = getView().getValueOfApprovedDevice(1);
                String dev4 = getView().getValueOfApprovedDevice(2);
                String dev5 = getView().getValueOfApprovedDevice(3);
                getView().setApprovedDeviceText(1, dev2);
                getView().setApprovedDeviceText(2, dev3);
                getView().setApprovedDeviceText(3, dev4);
                getView().setApprovedDeviceText(4, dev5);
                getView().setApprovedDeviceText(0, newDevice);
            }
            bleModel.approvedDevices[0] = getView().getValueOfApprovedDevice(0);
            bleModel.approvedDevices[1] = getView().getValueOfApprovedDevice(1);
            bleModel.approvedDevices[2] = getView().getValueOfApprovedDevice(2);
            bleModel.approvedDevices[3] = getView().getValueOfApprovedDevice(3);
            bleModel.approvedDevices[4] = getView().getValueOfApprovedDevice(4);
        }

    }

}
