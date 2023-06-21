package com.resant.bletest;


interface BleControlManager {

    void onDataRecived();
    void onConnection();
    void noBluetoothPermission(String bluetoothConnect);
    void onRead(BluetoothData result);
    void onReadFail();

    void onDataWrite();

    void onConnectionFailed();

    void onDataWriteFail();

    void onDeviceNotFound();
}