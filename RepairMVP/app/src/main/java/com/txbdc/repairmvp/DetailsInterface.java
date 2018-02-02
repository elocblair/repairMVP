package com.txbdc.repairmvp;

import android.content.Context;

import java.util.List;

/**
 * Created by Cole on 12/13/2017.
 */

public interface DetailsInterface {
    interface ViewToPresenter{
        Context getAppContext();
        Context getActivityContext();
        void setTextApprovedDevices();
        void stoppedButtonText();
        void startedButtonText();
        void updateRecyclerView(List<String> input);
        String getValueOfApprovedDevice(int element);
        void setApprovedDeviceText(final int element, final String newDevice);
        void setApprovedPCMText(final int element, final String newDevice);
        String getValueOfApprovedPCM(int element);
        void addPCMsToApproved();
        void addDevicesToApproved();
    }

    interface PresenterToView {
        String getApprovedDevice(int position);
        void activityStopped();
        void startDetailsScan();
        void setNewApprovedDevice(final String newDevice);
    }

    interface PresenterToBleModel{
        Context getAppContext();
        Context getActivityContext();
    }

    interface ModelToPresenter {
        void detailsStopped();
        void setSearchingFromDetails(Boolean value);

    }

}
