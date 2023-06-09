package com.resant.bletest;

import android.Manifest;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BLEConnection {
    /*
    Небольшая инструкция:
    Создать экземпляр класса, передать в него Контекст, UUID устройства, и BleControlManager
    Последний из них - интерфейс, использует функции, для установки сообщений и отловки результата получения данных.

    Работа с writeData:
    Есть два метода, которые отвечают за сохранения разных видов значений:
    addSendData_int32_t(int int32_t)
    addSendData_uint8_t(int uint8_t)
    Последовательно, используя их, добавляем значения.
    Затем, вызываем writeData, передаем UUID структуры
    После этого, BleControlManager вызовет void onDataWrite();, либо noBluetoothPermission()

    Работа с readData:
    В readData передается UUID структуры. В процессе вызывается onDataRead в BleControlManager, с объектом
    типа BluetoothData. Внутри содержится объект типа BluetoothData, который содержит в себе
    массив байтов, которые можно преобразовать самому при необходимости
    и метод getData, который преобразовывает все сам.
    В getData передается массив значений типа int.
    Каждое число обозначает тип данных, которые нужно получить. Для упрощения в классе содежатся
    константы INT32_T и UINT8_T. Значения преобразовываются в массив чисел типа int и возвращается.
    пример:
    public void onRead(BluetoothData result) {
        int [] res = result.getData(new int[]{BluetoothData.UINT8_T,BluetoothData.UINT8_T,BluetoothData.UINT8_T});
    }

     */




    private BleControlManager BCM; //   Интерфейс, для удобного использования
    private BluetoothGatt gatt; //   Связь  устройством
    private final Context context; //   Контекст приложения
    private final String UUIDname; //   UUID устройства
    private String UUIDshare; //  UUID значения
    private BluetoothGattCharacteristic characteristic; //   Характеристика устройства
    private final ArrayList<byte[]> dataAL = new ArrayList<>();//   Отправляемые данные

    //Конструктор
    @RequiresApi(api = Build.VERSION_CODES.S)
    public BLEConnection(@NonNull Context context, String UUIDname, BleControlManager BCM) {

        this.BCM = BCM; //  Сохранение интерфейса
        this.context = context; // Сохранение контекста
        this.UUIDname = UUIDname;

        //Проверка на сопряжение
        if (isBonded("LEX_DRONE_0001")) //  TODO сделать опциональным
            discover(); //  Сопряжение с устройством
        else
            initConnection(context, UUIDname); //   Подключение


    }

    //  При необходимости, можно заменить BleControlManager
    public void setBleControlManager(BleControlManager BCM) {
        this.BCM = BCM;
    }

    //   Инициализация соединения вручную
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void initConnection(String UUIDname) {
        initConnection(context, UUIDname);
    }

    //  Инициализация соединения
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void initConnection(Context context, String UUIDname) {
        //Свойства подключения - читать/писать
        int property = BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ;
        int permission = BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ;

        //  Характеристика для связи
        characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(UUIDname),
                property, permission);


        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

        //  Поиск устройств
        final BluetoothDevice[] device = new BluetoothDevice[1];
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                device[0] = result.getDevice();


            }
        };

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_SCAN);
        }
        scanner.startScan(scanCallback);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
        }

        //   Интерфейс ответа на события
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                // Если данные получены
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    byte[] recivedData = characteristic.getValue();
                    //  Они отправляются в onRead
                    BCM.onRead(new BluetoothData(recivedData));
                } else {
                    //  Иначе сообщение о неудаче
                    BCM.onReadFail();
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                //При успешном подключении
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(characteristic.getUuid());
                    if (service != null) {
                        //  Получаем значения, используя UUIDshare, получаемый в readData
                        BluetoothGattCharacteristic chara = service.getCharacteristic(UUID.fromString(UUIDshare));
                        if (chara != null) {

                            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                BCM.noBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT);
                            }
                            //  И отправляем запрос
                            gatt.readCharacteristic(chara);
                        }else{
                            BCM.onReadFail();
                        }
                    }
                }
            }



        };


        //Если устройства не обнаружены в результате скана
        if(device[0]!=null) {
            gatt = device[0].connectGatt(context, false, gattCallback);

        }
        else
        {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            //  Смотрим все устройства, с которыми было сопряжение
            for (BluetoothDevice dev : pairedDevices) {
                //  Если устройство с необходимым названием есть, то
                if (dev.getName().equals("LEX_DRONE_0001")) {// TODO сделать имя модуля опциональным

                    // сохраняем gatt
                    gatt = dev.connectGatt(context, false, gattCallback);
                    gatt.discoverServices();
                }
            }
        }

        //  Если ничего так и не найдено
        if(gatt==null)
        {
            BCM.onConnectionFailed();
            //  Отключаемся
            return;
        }
        //  Иначе сообщаем о соединении
        BCM.onConnection();
    }

    @SuppressLint("MissingPermission")
    public void discover()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //  Ищем среди всех обнаруженных устройств
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                   String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Полученаем объекта BluetoothDevice из интента
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //  Если имя девайса необходимое

                    if (device.getName()!=null&&device.getName().equals("LEX_DRONE_0001")) {//  TODO сделать имя опциональным
                        // Инициирование процесса сопряжения с устройством
                        boolean bondInitiated = device.createBond();    //  То сопрягаем

                        if (bondInitiated) {
                            Toast.makeText(context,"Bond! (try at least)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,"No Bond! (no try as well)", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }, filter);


        //  Отслеживаем подключение
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

                    // Получаем новое состояние сопряжения
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                    //  Сообщаем, если соеденено.
                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        Toast.makeText(context,"Bond WOOOOOW!", Toast.LENGTH_SHORT).show();
                        //  Начинаем подключение
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

    //  Проверка на сопряжение
    @SuppressLint("MissingPermission")
    private boolean isBonded(String name)
    {
        //  Получаем данные
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //  Получаем список сопряженных устройств
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();


        for (BluetoothDevice pairedDevice : pairedDevices) {
            if (pairedDevice.getName().equals(name)) {
                //  Если такое устройство нашлось - возвращается true
                return true;
            }
        }
        //  Иначе false
        return false;
    }

    //  Добавление отправляемых данных типа uint8_t
    public void addSendData_uint8_t(int uint8_t) {
        ByteBuffer buffer = ByteBuffer.allocate(1); //  Выделение памяти
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Установка порядка байтов
        buffer.put((byte)uint8_t);  //  Помещение числа
        byte[] data = buffer.array();   //  Превращение байтов в массив
        for(byte b: data)
        {
            dataAL.add(new byte[]{b});//  Добавление в массив
        }
    }

    //  Добавление отправляемых данных типа int32_t
    public void addSendData_int32_t(int int32_t) {
        ByteBuffer buffer = ByteBuffer.allocate(4);//  Выделение памяти
        buffer.order(ByteOrder.LITTLE_ENDIAN); //  Установка порядка байтов
        buffer.putInt(int32_t); //  Помещение числа
        byte[] data = buffer.array(); //  Превращение байтов в массив

        for(byte b: data)
        {
            dataAL.add(new byte[]{b}); //  Добавление в массив
        }
    }

    //  Добавление произвольных данных в виде байтов
    public void addData(byte[] arr) {
        for(byte b: arr)
            dataAL.add(new byte[]{b});
    }


    //  Отправление данных
    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean writeData(String UUIDshare) {

        BluetoothGattService service = gatt.getService(UUID.fromString(UUIDname)); //  Получение сервиса

        byte[] data = new byte[dataAL.size()];
        for (int i = 0; i < data.length; i++) { //  Преобразование в обычный массив байтов
            data[i] = dataAL.get(i)[0];
        }

        //  Получение характеристик
        BluetoothGattCharacteristic chara = service.getCharacteristic(UUID.fromString(UUIDshare));
        //  Задание значения характеристике
        chara.setValue(data);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
        }
        //  Отправление характеристики
        gatt.writeCharacteristic(chara);

        BCM.onDataWrite();
        return true;
    }


    //  Получение данных
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void readData(String UUIDshare) {
        //  Сохранение UUIDshare
        this.UUIDshare = UUIDshare;

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
        }
        //  соединение, отправление
        gatt.discoverServices();
    }




}

