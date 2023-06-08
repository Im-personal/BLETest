package com.resant.bletest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class BLEConnection {

    BleControlManager BCM;
    BluetoothGatt gatt;
    Context context;
    BluetoothGattCharacteristic characteristic;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public BLEConnection(@NonNull Context context, String UUIDname, BleControlManager BCM) {

        initConnection(context, UUIDname);
        this.BCM = BCM;

    }

    public void setBleControlManager(BleControlManager BCM) {
        this.BCM = BCM;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void initConnection(String UUIDname) {
        initConnection(context, UUIDname);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void initConnection(Context context, String UUIDname) {
        int property = BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ;
        int permission = BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ;


        characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(UUIDname),
                property, permission); // Получите характеристику, которую вы хотите использовать для отправки данных


        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

        final BluetoothDevice[] device = new BluetoothDevice[1];
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                device[0] = result.getDevice();
                BCM.onConnection();

            }
        };

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_SCAN);
            return;
        }
        scanner.startScan(scanCallback);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
            return;
        }

        BLEConnection bleConnection = this;

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    recivedData = characteristic.getValue();
                    BCM.onRead(bleConnection);
                }else {
                    BCM.onReadFail();
                }

            }
        };

        gatt = device[0].connectGatt(context, false, gattCallback);

    }


    public void addSendData_uint8_t(int uint8_t) {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Устанавливаем порядок байтов (зависит от устройства)
        buffer.put((byte)uint8_t);
        byte[] data = buffer.array();
        for(byte b: data)
        {
            dataAL.add(new byte[]{b});
        }
    }

    public void addSendData_int32_t(int int32_t) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Устанавливаем порядок байтов (зависит от устройства)
        buffer.putInt(int32_t);
        byte[] data = buffer.array();

        for(byte b: data)
        {
            dataAL.add(new byte[]{b});
        }
    }

    public void addData(byte[] arr) {
        dataAL.add(arr);
    }

    private final ArrayList<byte[]> dataAL = new ArrayList<>();
    private byte[] recivedData;


    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean writeData() {

        byte[] data = new byte[dataAL.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = dataAL.get(i)[0];
        }

        characteristic.setValue(data);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
            return false;
        }
        gatt.writeCharacteristic(characteristic);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean readData() {


        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
            return false;
        }
        gatt.readCharacteristic(characteristic);
        return true;
    }

    public static final int INT32_T = 0;
    public static final int UINT8_T = 1;

    public int[] getData(int[] types)
    {

        int[] res = new int[types.length];

        ByteBuffer buffer = ByteBuffer.wrap(recivedData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(int i = 0; i<res.length;i++)
        {
            int type = types[i];
            switch (type)
            {
                case INT32_T:
                    res[i] = buffer.getInt();
                    break;

                case UINT8_T:
                    res[i] = buffer.get()&0xFF;
                    break;
            }
        }


        return res;
    }

}

