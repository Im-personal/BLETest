package com.resant.bletest;


interface BleControlManager {

    void onDataRecived();
    void onConnection();
    void noBluetoothPermission(String bluetoothConnect);
    void onRead(BLEConnection result);
    void onReadFail();

}