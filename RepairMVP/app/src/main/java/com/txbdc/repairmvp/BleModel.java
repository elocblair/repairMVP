package com.txbdc.repairmvp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

/**
 * Created by Cole on 11/7/2017.
 */

public class BleModel extends Service implements DetailsInterface.ModelToPresenter {
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private IBinder bleBinder = new BleBinder();
    Intent intent;
    private int sensorToConnect = 0;
    String TAG = "bleModel";
    private BluetoothDevice thighDevice, shinDevice, footDevice =  null;
    private BluetoothGatt thighGatt, shinGatt, footGatt, fireflyGatt = null;
    private final int connected = 2;
    private final int connecting = 1;
    private final int disconnected = 0;
    public BluetoothGattCharacteristic FIREFLY_CHARACTERISTIC2;
    boolean fireflyFound = false;
    private boolean scanning = false;
    private final int thigh = 1;
    private final int shin = 2;
    private final int foot = 3;
    String[] approvedDevices = new String[8];
    public boolean searchingFromDetails = false;
    public String[] deviceIDs = new String[30];
    public String[] pcmIDs = new String[30];
    public int[] deviceRSSIs = new int[30];
    public int shockclockCount = 0;
    public int pcmCount = 0;
    SharedPreferences sharedPreferences;

    private BluetoothGattCharacteristic NRF_CHARACTERISTIC;

    public void triggerFF() {
        boolean b = fireflyGatt.writeCharacteristic(FIREFLY_CHARACTERISTIC2);
        Log.i(TAG, "firefly write status = " + b);
    }

    public class BleBinder extends Binder {
        BleModel getService() {
            return BleModel.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return bleBinder;
    }

    public void initializeBle(){
        adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();
        intent =  new Intent(TAG);

    }

    @Override
    public void onCreate() {
        sharedPreferences = this.getSharedPreferences("savedDevices", Context.MODE_PRIVATE);
        approvedDevices[0] = sharedPreferences.getString("device1", "000000");
        approvedDevices[1] = sharedPreferences.getString("device2", "000000");
        approvedDevices[2] = sharedPreferences.getString("device3", "000000");
        approvedDevices[3] = sharedPreferences.getString("device4", "000000");
        approvedDevices[4] = sharedPreferences.getString("device5", "000000");
        approvedDevices[5] = sharedPreferences.getString("device6", "000000");
        approvedDevices[6] = sharedPreferences.getString("device7", "000000");
        approvedDevices[7] = sharedPreferences.getString("device8", "000000");

    }
    public void detailsStopped() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("device1", approvedDevices[0]);
        editor.putString("device2", approvedDevices[1]);
        editor.putString("device3", approvedDevices[2]);
        editor.putString("device4", approvedDevices[3]);
        editor.putString("device5", approvedDevices[4]);
        editor.putString("device6", approvedDevices[5]);
        editor.putString("device7", approvedDevices[6]);
        editor.putString("device8", approvedDevices[7]);
        editor.commit();
    }

    @Override
    public void setSearchingFromDetails(Boolean value) {
        this.searchingFromDetails = value;
    }

    public int changeSensorToConnect(int sensor) {
        scanner.flushPendingScanResults(mScanCallback);
        int previousSensor = sensorToConnect;
        sensorToConnect = sensor;
        return previousSensor;
    }

    public void stopScan(){
        sensorToConnect = 0;
        scanner.stopScan(mScanCallback);
        scanning = false;
    }

    public int checkSensorStatus(int sensor) {
        if(sensor == thigh){
            if (thighGatt != null) {
                return connected;
            }
            else return 0;
        }
        else if(sensor == shin){
            if (shinGatt != null) {
                return connected;
            }
            else return 0;
        }
        else if(sensor == foot){
            if(footGatt != null){
                return connected;
            }
            else return 0;
        }
        else if(sensor == 4){
            if(fireflyGatt != null){
                return connected;
            }
            else return 0;
        }
        else return 0;
    }

    public boolean checkIfScanning(){
        return scanning;
    }

    public void searchForBleDevice(int sensor){
        Log.v("cole", "searched");
        sensorToConnect = sensor;
        scanning = true;
        scanner.startScan(mScanCallback);
    }

    public void searchForBleDevice(){
        sensorToConnect = 0;
        scanning = true;
        scanner.startScan(mScanCallback);
    }

    public void disconnectSensor(int sensor){
        if(sensor == thigh){
            thighGatt.disconnect();
            thighGatt.close();
            thighGatt = null;
        }
        else if(sensor == shin){
            shinGatt.disconnect();
            shinGatt.close();
            shinGatt = null;
        }
        else if(sensor == foot){
            footGatt.disconnect();
            footGatt.close();
            footGatt = null;
        }
        else if(sensor == 4){
            fireflyGatt.disconnect();
            fireflyGatt.close();
            fireflyGatt = null;
            fireflyFound = false;
        }
    }

    public ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");

            processResult(result);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: " + results.size() + " results");
            for (ScanResult result : results) {
                processResult(result);
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "LE Scan Failed: " + errorCode);
        }

        private void processResult(ScanResult device) {
            Log.i(TAG, "New LE Device: " + device.getDevice().getName() + " @ " + device.getRssi() + " Address " + device.getDevice().getAddress());
            String deviceName;
            deviceName = device.getDevice().getName();
            if(deviceName != null){
                if(searchingFromDetails){
                    if (deviceName.equals("JohnCougarMellenc")) {
                        boolean newDevice = true;
                        for (int i = 0; i < shockclockCount; i++) {
                            if (device.getDevice().getAddress().equals(deviceIDs[i])) {
                                newDevice = false;
                            }
                        }
                        if (newDevice) {
                            deviceIDs[shockclockCount] = device.getDevice().getAddress();
                            deviceRSSIs[shockclockCount] = device.getRssi();
                            shockclockCount++;
                        }
                    }
                    if (deviceName.contains("FireflyPCM")) {
                        boolean newPCM = true;
                        for (int i = 0; i < pcmCount; i++) {
                            if (device.getDevice().getName().equals(pcmIDs[i])) {
                                newPCM = false;
                            }
                        }
                        if (newPCM) {
                            pcmIDs[pcmCount] = device.getDevice().getName();
                            deviceRSSIs[pcmCount] = device.getRssi();
                            pcmCount++;
                        }
                    }
                    if (deviceName.equals("JohnCougarMellenc")) {
                        boolean newDevice = true;
                        for (int i = 0; i < shockclockCount; i++) {
                            if (device.getDevice().getAddress().equals(deviceIDs[i])) {
                                newDevice = false;
                            }
                        }
                        if (newDevice) {
                            deviceIDs[shockclockCount] = device.getDevice().getAddress();
                            deviceRSSIs[shockclockCount] = device.getRssi();
                            shockclockCount++;
                        }
                    }
                    if (deviceName.contains("FireflyPCM")) {
                        boolean newPCM = true;
                        for (int i = 0; i < pcmCount; i++) {
                            if (device.getDevice().getName().equals(pcmIDs[i])) {
                                newPCM = false;
                            }
                        }
                        if (newPCM) {
                            pcmIDs[pcmCount] = device.getDevice().getName();
                            deviceRSSIs[pcmCount] = device.getRssi();
                            pcmCount++;
                        }
                    }
                }
                else{
                    if(deviceName.equals("JohnCougarMellenc")){
                        Log.v(TAG, "sensor " + sensorToConnect);
                        for (int i = 0; i < 5; i++) {
                            if (approvedDevices[i].contains(device.getDevice().getAddress().toString())) {
                                if (sensorToConnect == 1){
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    thighDevice = device.getDevice();
                                    thighGatt = thighDevice.connectGatt(getApplicationContext(),false, bleGattCallback);
                                }
                                else if (sensorToConnect == 2){
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    shinDevice = device.getDevice();
                                    shinGatt = shinDevice.connectGatt(getApplicationContext(),false, bleGattCallback);
                                }
                                else if(sensorToConnect == 3){
                                    scanner.stopScan(mScanCallback);
                                    scanning = false;
                                    footDevice = device.getDevice();
                                    footGatt = footDevice.connectGatt(getApplicationContext(), false, bleGattCallback);
                                }
                            }
                        }
                    }
                    if(sensorToConnect == 4) {
                        if (deviceName.contains("FireflyPCM")) {
                            for (int i = 5; i < 8; i++) {
                                if (approvedDevices[i].contains(device.getDevice().getName().toString())) {
                                        BluetoothDevice sensor = device.getDevice();
                                        scanner.stopScan(mScanCallback);
                                        scanning = false;
                                        fireflyGatt = sensor.connectGatt(getApplicationContext(), false, bleGattCallback);
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    public final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
           if (gatt == thighGatt | gatt == shinGatt | gatt == footGatt) {
                byte[] temp = characteristic.getValue();
                int MSB = temp[1] << 8;
                int LSB = temp[0] & 0x000000FF;
                int val = MSB | LSB;
                float gyroZ = val * 0.0625f;
                MSB = temp[3] << 8;
                LSB = temp[2] & 0x000000FF;
                val = MSB | LSB;
                float gyroY = val * 0.0625f;
                MSB = temp[5] << 8;
                LSB = temp[4] & 0x000000FF;
                val = MSB | LSB;
                float gyroX = val * 0.0625f;

               MSB = temp[7] << 8;
               LSB = temp[6] & 0x000000FF;
               val = MSB | LSB;
               float accZ = val * 0.001f;
               MSB = temp[9] << 8;
               LSB = temp[8] & 0x000000FF;
               val = MSB | LSB;
               float accY = val * 0.001f;
               MSB = temp[11] << 8;
               LSB = temp[10] & 0x000000FF;
               val = MSB | LSB;
               float accX = val * 0.001f;

               String accData = "acceleration values: x = " + accX + ", y = " + accY + ", z = " + accZ;
               //Log.v(TAG, accData);
                //Log.v(TAG, "value " + gyroX);
               String bleEvent = "notification";
               intent.putExtra("bleEvent", bleEvent);
                if(gatt == thighGatt){
                    BleNotification notification = new BleNotification(gyroX, gyroY, gyroZ, accX, accY, accZ, "hip");
                    intent.putExtra("notifyObject",notification);
                }
                if(gatt == shinGatt){
                    BleNotification notification = new BleNotification(gyroX, gyroY, gyroZ, accX, accY, accZ, "knee");
                    intent.putExtra("notifyObject",notification);
                }
                if(gatt == footGatt){
                    BleNotification notification = new BleNotification(gyroX, gyroY, gyroZ, accX, accY, accZ, "ankle");
                    intent.putExtra("notifyObject",notification);
                }
                sendBroadcast(intent);
            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
           if (newState == disconnected) {
               String bleEvent = "sensorDisconnected";
                if (gatt.equals(thighGatt)) {
                    thighGatt = null;
                    intent.putExtra("bleEvent", bleEvent);
                    intent.putExtra("gatt", "hip");
                } else if (gatt.equals(shinGatt)) {
                    shinGatt = null;
                    intent.putExtra("bleEvent", bleEvent);
                    intent.putExtra("gatt", "knee");
                } else if (gatt.equals(footGatt)) {
                    footGatt = null;
                    intent.putExtra("bleEvent", bleEvent);
                    intent.putExtra("gatt", "ankle");
                }
                else if (gatt.equals(fireflyGatt)){
                   footGatt = null;
                   intent.putExtra("bleEvent", bleEvent);
                   intent.putExtra("gatt", "firefly");
               }
                sendBroadcast(intent);
            } else if (newState == connecting) {
            } else if (newState == connected) {
               Log.v(TAG, "device connected");
                gatt.discoverServices();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.v(TAG, "charRead");
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
           Log.v(TAG, "services discovered");
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (int i = 0; i < characteristics.size(); i++) {
                    if (characteristics.get(i).getUuid().toString().equals("0000beef-1212-efde-1523-785fef13d123")) {
                        NRF_CHARACTERISTIC = service.getCharacteristic(UUID.fromString("0000beef-1212-efde-1523-785fef13d123"));
                        gatt.setCharacteristicNotification(NRF_CHARACTERISTIC, true);
                        UUID dUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                        BluetoothGattDescriptor notifyDescriptor = NRF_CHARACTERISTIC.getDescriptor(dUUID);
                        notifyDescriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                        boolean b = gatt.writeDescriptor(notifyDescriptor);
                        Log.v(TAG, String.valueOf(b));
                        String bleEvent = "sensorConnected";
                        intent.putExtra("bleEvent", bleEvent);
                        intent.putExtra("gatt", "undetermined");
                        if (gatt == thighGatt) {
                            intent.putExtra("gatt", "hip");
                        }
                        if (gatt == shinGatt) {
                            intent.putExtra("gatt", "knee");
                        }
                        if (gatt == footGatt) {
                            intent.putExtra("gatt", "ankle");
                        }
                        sendBroadcast(intent);
                    }
                    if (characteristics.get(i).getUuid().toString().equals("0000fff2-0000-1000-8000-00805f9b34fb")) {
                        FIREFLY_CHARACTERISTIC2 = characteristics.get(i);
                        FIREFLY_CHARACTERISTIC2.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        fireflyFound = true;
                        Log.v(TAG, "pcm connected");
                        String bleEvent = "sensorConnected";
                        intent.putExtra("bleEvent", bleEvent);
                        intent.putExtra("gatt", "firefly");
                        sendBroadcast(intent);
                    }
                }
            }
        }
    };
}
