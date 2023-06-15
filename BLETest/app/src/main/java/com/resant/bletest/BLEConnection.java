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
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

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
    private BluetoothDevice device; // Переменная для хранения найденного устройства
    private boolean isReading=false;



    private String deviceName = "LEX_DRONE_0001";


    //Конструктор
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public BLEConnection(@NonNull Context context, String UUIDname, BleControlManager BCM) {

        this.BCM = BCM; //  Сохранение интерфейса
        this.context = context; // Сохранение контекста
        this.UUIDname = UUIDname;

        //Проверка на сопряжение
        if(!isTriedToConnect)
            initConnection(context, UUIDname); //   Подключение
        else
            gatt.discoverServices();

    }


    //Конструктор с именем
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public BLEConnection(@NonNull Context context, String UUIDname,String deviceName, BleControlManager BCM) {

        this.deviceName = deviceName;
        this.BCM = BCM; //  Сохранение интерфейса
        this.context = context; // Сохранение контекста
        this.UUIDname = UUIDname;



            initConnection(context, UUIDname); //   Подключение


    }

    @SuppressLint("MissingPermission")
    public void discoverServices()
    {
        isReading=false;
        gatt.discoverServices();
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


    boolean isTriedToConnect = false;

    //  Инициализация соединения
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void initConnection(Context context, String UUIDname) {
        isReading=false;
        //Свойства подключения - читать/писать
        int property = BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ;
        int permission = BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ;

        //  Характеристика для связи
        characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(UUIDname),
                property, permission);


        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();



        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
        }

        //   Интерфейс ответа на события
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {



            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                // handle characteristic changed notifications here
                Log.d("dataforme","yeah kinda");
                // do something with the value
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.println(Log.DEBUG,"dataforme", "first step");
                // Если данные получены
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.println(Log.DEBUG,"dataforme", "secodnd step");
                    byte[] recivedData = characteristic.getValue();
                    Log.println(Log.DEBUG,"dataforme", recivedData[0]+";"+recivedData[1]+";"+recivedData[2]+";");
                    //  Они отправляются в onRead
                    BCM.onRead(new BluetoothData(recivedData));
                } else {
                    //  Иначе сообщение о неудаче
                    BCM.onReadFail();
                }

            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Устройство подключено
                    Log.d("dataforme", "Device connected");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Устройство отключено
                    Log.d("dataforme", "Device disconnected");
                }
            }


            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                //При успешном подключении
                if (status == BluetoothGatt.GATT_SUCCESS) {


                    if(isReading) {
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
                            } else {
                                BCM.onReadFail();
                            }
                        }
                    }else {
                        BCM.onConnection();
                    }
                }else{
                    BCM.onConnectionFailed();

                }
            }



        };




        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner(); // Сканнер ближайших устройств


        ScanCallback scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice foundDevice = result.getDevice();
                if (foundDevice.getName()!=null&&foundDevice.getName().equals(deviceName)) {//Если имя устройства совпадает с необходимым
                    device = foundDevice;
                    gatt = device.connectGatt(context, false, gattCallback);// Подключение к нему, сохранение его данных
                    sleep(300);//Небольшое ожидание - необходимо для корректной работы discoverServices()
                    isTriedToConnect=true;
                    gatt.discoverServices();

                    scanner.stopScan(this); // Останавливаем сканирование
                }


            }
        };

        scanner.startScan(scanCallback);

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
    public void addSendData_uint8_t(char uint8_t) {
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


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //  Отправление данных
    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean writeData(String UUIDshare) {
        //isReading=false;
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
        if(gatt.writeCharacteristic(chara))
        {
            BCM.onDataWrite();
            dataAL.clear();
            return true;
        }
        else
        {
            BCM.onDataWriteFail();
            dataAL.clear();
            return false;
        }
    }


    //  Получение данных
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void readData(String UUIDshare) {
        isReading=true;
        //  Сохранение UUIDshare
        this.UUIDshare = UUIDshare;

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            BCM.noBluetoothPermission(android.Manifest.permission.BLUETOOTH_CONNECT);
        }
        //  соединение, отправление
        gatt.discoverServices();
    }


    @SuppressLint("MissingPermission")
    public void disconnect()
    {
        if(gatt!=null)
            gatt.disconnect();
    }


}

