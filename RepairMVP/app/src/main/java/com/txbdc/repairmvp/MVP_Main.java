package com.txbdc.repairmvp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Cole on 11/7/2017.
 */

public interface MVP_Main {
    // Required View methods available to the presenter
    // the passive layer, responsible only for showing data
    // and receiving user interactions
    interface RequiredViewOps{
        Context getAppContext();
            Context getActivityContext();
        void notifyViewOfUpdatesFromRemoteService(Bundle bundle);
        void searchingForThigh();
        void searchingForShin();
        void searchingForFoot();
        void searchingForFF();
        void notSearchingForThigh();
        void notSearchingForShin();
        void notSearchingForFoot();
        void notSearchingForThighText();
        void notSearchingForShinText();
        void notSearchingForFootText();
        void sensorConnected(final SensorView sensor);
        void thighConnected();
        void shinConnected();
        void footConnected();
        void disconnected(final SensorView sensor);
        void thighDisconnected();
        void shinDisconnected();
        void footDisconnected();
        void setLongClickActionThigh();
        void setLongClickActionShin();
        void setLongClickActionFoot();
        void setLongClickFirefly();
        void fireflyConnected();
        void fireflyDisconnected();
        void displayPositiveThighValue(final float value);
        void displayNegativeThighValue(final float value);
        void displayPositiveShinValue(final float value);
        void displayNegativeShinValue(final float value);
        void displayPositiveFootValue(final float value);
        void displayNegativeFootValue(final float value);
        void searchingForDevice(SensorView view);
        void startRecording();
        void stopRecording();
        void setTextDefault();
        void showKeyboard(View v);
    }

    // Operations offered to the view to communicate with the presenter
    // Processes user interactions and sends data requests to the model
    interface ProvidedPresenterOps{
        void mainActivityStopped();
        void clickSensorButton(int sensor);
        boolean getAudioRecordingStatus();
        int checkSensorStatus(int sensor);
        void clickSensorButton(SensorView view, int sensor);
        void clickRecord();
        void clickPCM();
        void disconnectFF();
        void sendViewToPresenterTest(SensorView sensor);
        void zeroSensor(int sensor);
        void writeFile(String filename, String notes);
        void writeFileAtStop(String descriptor, int sensor);
        void clearDataBuffers();
        void triggerFF();
    }

    // Required Presenter methods available to the model
    interface RequiredPresenterOps{
        Context getAppContext();
            Context getActivityContext();
    }

    // Operations offered to the Model to communicate with the Presenter
    // handles all data business logic
    interface ProvidedModelOps{
        int changeSensorToConnect(int sensor);
        int checkSensorStatus(int sensor);
        boolean checkIfScanning();
        void searchForBleDevice(int sensor);
        void triggerFF();
    }
}
