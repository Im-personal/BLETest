package com.resant.bletest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BLEConnection {

    private BleControlManager BCM;
    private BluetoothGatt gatt;
    private Context context;
    private String UUIDname;
    private BluetoothGattCharacteristic characteristic;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public BLEConnection(@NonNull Context context, String UUIDname, BleControlManager BCM) {

        this.BCM = BCM;
        this.context = context;
        discover();
        this.UUIDname = UUIDname;
        //initConnection(context, UUIDname);


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
                //BCM.onConnection();

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
            //return;
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
            //return;
        }

        BLEConnection bleConnection = this;

        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    recivedData = characteristic.getValue();
                    BCM.onRead(bleConnection);
                } else {
                    BCM.onReadFail();
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(characteristic.getUuid());
                    if (service != null) {

                        BluetoothGattCharacteristic chara = service.getCharacteristic(UUID.fromString(UUIDshare));
                        if (chara != null) {
                            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            gatt.readCharacteristic(chara);
                        }else{
                            BCM.onReadFail();
                        }
                    }
                }
            }



        };

        if(device[0]!=null) {
            gatt = device[0].connectGatt(context, false, gattCallback);

        }
        else
        {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            for (BluetoothDevice dev : pairedDevices) {
                //if (dev.getAddress().equals("Адрес устройства")) {
                Log.println(Log.DEBUG,"dataforme",dev.getName());
                Log.println(Log.DEBUG,"dataforme", Arrays.toString(pairedDevices.toArray()));

                    gatt = dev.connectGatt(context, false, gattCallback);
                    gatt.discoverServices();
                //}
            }
        }

        if(gatt==null)
        {
            BCM.onConnectionFailed();
            return;
        }
        BCM.onConnection();
    }

    @SuppressLint("MissingPermission")
    public void discover()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

// Регистрация BroadcastReceiver для получения уведомлений об обнаруженных устройствах
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                   String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Получение объекта BluetoothDevice из интента
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Проверка имени или адреса устройства
                    if(device.getName()!=null)
                    if (device.getName().equals("LEX_DRONE_0001")) {
                        // Инициирование процесса сопряжения с устройством
                        boolean bondInitiated = device.createBond();

                        if (bondInitiated) {
                            Toast.makeText(context,"Bond! (try at least)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,"No Bond! (no try as well)", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }, filter);


        // Регистрация BroadcastReceiver для получения уведомлений об изменении состояния сопряжения
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    // Получение объекта BluetoothDevice из интента
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Получение нового состояния сопряжения
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        Toast.makeText(context,"Bond WOOOOOW!", Toast.LENGTH_SHORT).show();
                        initConnection(UUIDname);
                    } else if (bondState == BluetoothDevice.BOND_NONE) {
                        Toast.makeText(context,"no bond :'(", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, filter);


// Запуск процесса обнаружения устройств
        bluetoothAdapter.startDiscovery();






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
    public boolean writeData(String UUIDshare) {

        BluetoothGattService service = gatt.getService(UUID.fromString(UUIDname));

        byte[] data = new byte[dataAL.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = dataAL.get(i)[0];
        }

        //characteristic.setValue(data);
        BluetoothGattCharacteristic chara = service.getCharacteristic(UUID.fromString(UUIDshare));
        chara.setValue(data);
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
        gatt.writeCharacteristic(chara);


        BCM.onDataWrite();
        return true;
    }

    private String UUIDshare;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void readData(String UUIDlil) {
        this.UUIDshare = UUIDlil;

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
            //return false;
        }
        gatt.discoverServices();
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

