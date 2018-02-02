package com.txbdc.repairmvp;

/**
 * Created by Cole on 11/17/2017.
 */


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neuronifier on 9/6/2017.
 */

public class BleNotification implements Parcelable {
    float eulerX, eulerY, eulerZ, accX, accY, accZ;
    String gatt;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(eulerX);
        dest.writeFloat(eulerY);
        dest.writeFloat(eulerZ);
        dest.writeFloat(accX);
        dest.writeFloat(accY);
        dest.writeFloat(accZ);
        dest.writeString(gatt);
    }
    public static final Parcelable.Creator<BleNotification> CREATOR
            = new Parcelable.Creator<BleNotification>() {
        public BleNotification createFromParcel(Parcel in) {
            return new BleNotification(in);
        }

        public BleNotification[] newArray(int size) {
            return new BleNotification[size];
        }
    };
    private BleNotification(Parcel in) {
        eulerX = in.readFloat();
        eulerY = in.readFloat();
        eulerZ = in.readFloat();
        accX = in.readFloat();
        accY = in.readFloat();
        accZ = in.readFloat();
        gatt = in.readString();
    }
    public BleNotification(float eulerX, String gatt){
        this.eulerX = eulerX;
        this.gatt = gatt;
    }
    public BleNotification(float eulerX, float eulerY, float eulerZ, float accX, float accY, float accZ, String gatt){
        this.eulerX = eulerX;
        this.eulerY = eulerY;
        this.eulerZ =  eulerZ;
        this.accX =  accX;
        this.accY = accY;
        this.accZ = accZ;
        this.gatt = gatt;
    }
}