package com.resant.bletest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private BLEConnection blec;
    Context context;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         context = this;



        Button connect = findViewById(R.id.button3);
        connect.setOnClickListener(v -> {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.BLUETOOTH_CONNECT},
                        1);

            });


        Button send = findViewById(R.id.button);
        send.setOnClickListener(view -> {


            EditText protocol_version = findViewById(R.id.editTextText);
            EditText hardware_type = findViewById(R.id.editTextText2);
            EditText hardware_version = findViewById(R.id.editTextText3);

            blec.addSendData_uint8_t(Integer.parseInt(protocol_version.getText().toString()));
            blec.addSendData_uint8_t(Integer.parseInt(hardware_type.getText().toString()));
            blec.addSendData_uint8_t(Integer.parseInt(hardware_version.getText().toString()));

            blec.writeData("0000cafe-0000-1000-8000-00805f9b34fb");
        });

        Button read = findViewById(R.id.button2);
        read.setOnClickListener(view ->
        {
            blec.readData("0000cafe-0000-1000-8000-00805f9b34fb");
        });

    }

    private void setFields(int [] arr)
    {
        EditText et1 = findViewById(R.id.editTextText);
        EditText et2 = findViewById(R.id.editTextText2);
        EditText et3 = findViewById(R.id.editTextText3);
        et1.setText(arr[0]);
        et2.setText(arr[1]);
        et3.setText(arr[2]);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.println(Log.DEBUG,"REQUEST", Arrays.toString(grantResults));

        blec = new BLEConnection(this, "0000babe-0000-1000-8000-00805f9b34fb", new BleControlManager() {
            @Override
            public void onDataRecived() {
                runOnUiThread(()->Toast.makeText(context,"Data Recived", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onConnection() {
                runOnUiThread(()->Toast.makeText(context,"Connected!", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void noBluetoothPermission(String bluetoothConnect) {
                //runOnUiThread(()->Toast.makeText(context,"No Permission", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onRead(BluetoothData result) {
                int [] res = result.getData(new int[]{BluetoothData.UINT8_T,BluetoothData.UINT8_T,BluetoothData.UINT8_T});
                setFields(res);
            }

            @Override
            public void onReadFail() {
                runOnUiThread(()->Toast.makeText(context,"ReadFail", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDataWrite() {

            }

            @Override
            public void onConnectionFailed() {
                Toast.makeText(context,"No Connection", Toast.LENGTH_SHORT).show();
            }
        });

    }

}