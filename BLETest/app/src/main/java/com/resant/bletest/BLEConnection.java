package com.resant.bletest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;

public class BLEConnection extends BleManager implements BleControlManager{
    public BLEConnection(@NonNull Context context) {
        super(context);
        BleManagerCallback bmc = new BleManagerCallback(context);
        bmc.initialize();
    }

    @Override
    public boolean writeToRadio(String str) {

        return false;
    }

    private static class BleManagerCallback extends BleManager {


        public BleManagerCallback(@NonNull Context context) {
            super(context);
            c=context;
        }
        private Context c;
        @Override
        protected void initialize() {
            final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID.fromString("0000cafe-0000-1000-8000-00805f9b34fb"),BluetoothGattCharacteristic.PROPERTY_WRITE,BluetoothGattCharacteristic.PERMISSION_WRITE);

            ByteBuffer sendData = ByteBuffer.allocate(3);
            sendData.put((byte) 0);
            sendData.put((byte) 1);
            sendData.put((byte) 2);

            writeCharacteristic(characteristic,sendData.array())
                    .with((device, data) -> {
                        new AlertDialog.Builder(c)
                                .setTitle("YEAH")
                                .setMessage("получилось");
                        System.out.println("YEAH");
                    })
                    .fail((device, status) -> {
                        new AlertDialog.Builder(c)
                                .setTitle("NHAA...")
                                .setMessage("не получилось(");
                        System.out.println("");
                    })
                    .enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            System.out.println("");
            new AlertDialog.Builder(c)
                    .setTitle("dosantwork")
                    .setMessage("ну вот совсем не получилось(");
            return true;
        }

        @Override
        protected void onServicesInvalidated() {
            System.out.println("");
            new AlertDialog.Builder(c)
                    .setTitle(" dosework")
                    .setMessage("idktbg lol");
        }
    }
}

