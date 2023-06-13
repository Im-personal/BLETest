package com.resant.bletest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private BLEConnection blec;
    Context context;

    @SuppressLint("CutPasteId")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         context = this;


        et1 = findViewById(R.id.editTextText);
        et2 = findViewById(R.id.editTextText2);
        et3 = findViewById(R.id.editTextText3);
        et4 = findViewById(R.id.editTextText4);
        et5 = findViewById(R.id.editTextText5);
        deviceName = findViewById(R.id.deviceName);

        String[] options = {"0000cafe-0000-1000-8000-00805f9b34fb", "0000caff-0000-1000-8000-00805f9b34fb", "0000cb00-0000-1000-8000-00805f9b34fb"};
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // handle the selection change here
                String selectedOption = parentView.getSelectedItem().toString();

                switch (selectedOption)
                {
                    case "0000cafe-0000-1000-8000-00805f9b34fb":
                        et1.setHint("uint8_t protocol_version;");
                        et2.setHint("uint8_t hardware_type;");
                        et3.setHint("uint8_t hardware_version;");
                        et4.setVisibility(View.INVISIBLE);
                        et5.setVisibility(View.INVISIBLE);
                        break;
                    case "0000caff-0000-1000-8000-00805f9b34fb":
                        et1.setHint("uint8_t shift_enabled;");
                        et2.setHint("uint8_t config_store;");
                        et3.setHint("int32_t latitude_shift;");
                        et4.setHint("int32_t longitude_shift;");
                        et4.setVisibility(View.VISIBLE);
                        et5.setVisibility(View.INVISIBLE);

                        break;
                    case "0000cb00-0000-1000-8000-00805f9b34fb":
                        et1.setHint("uint8_t enabled;");
                        et2.setHint("uint8_t config_store;");
                        et3.setHint("int32_t latitude;");
                        et4.setHint("int32_t longitude;");
                        et5.setHint("uint32_t time;");
                        et4.setVisibility(View.VISIBLE);
                        et5.setVisibility(View.VISIBLE);

                        break;
                }

                System.out.println("Selected option: " + selectedOption);
            }
        });
        spinner.setSelection(0);

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
            switch (spinner.getSelectedItem().toString())
            {
                case "0000cafe-0000-1000-8000-00805f9b34fb":


                    blec.addSendData_uint8_t((char)Integer.parseInt(et1.getText().toString()));
                    blec.addSendData_uint8_t((char)Integer.parseInt(et2.getText().toString()));
                    blec.addSendData_uint8_t((char) Integer.parseInt(et3.getText().toString()));

                    break;
                case "0000caff-0000-1000-8000-00805f9b34fb":

                    blec.addSendData_uint8_t((char)Integer.parseInt(et1.getText().toString()));
                    blec.addSendData_uint8_t((char)Integer.parseInt(et2.getText().toString()));
                    blec.addSendData_int32_t((char) Integer.parseInt(et3.getText().toString()));
                    blec.addSendData_int32_t((char) Integer.parseInt(et4.getText().toString()));

                    break;
                case "0000cb00-0000-1000-8000-00805f9b34fb":
                    blec.addSendData_uint8_t((char)Integer.parseInt(et1.getText().toString()));
                    blec.addSendData_uint8_t((char)Integer.parseInt(et2.getText().toString()));
                    blec.addSendData_int32_t((char) Integer.parseInt(et3.getText().toString()));
                    blec.addSendData_int32_t((char) Integer.parseInt(et4.getText().toString()));
                    blec.addSendData_int32_t((char) Integer.parseInt(et5.getText().toString()));
                    break;
            }


            blec.writeData(spinner.getSelectedItem().toString());
        });

        Button read = findViewById(R.id.button2);
        read.setOnClickListener(view ->
        {
            blec.readData(spinner.getSelectedItem().toString());
        });

        findViewById(R.id.randomize).setOnClickListener(view ->
        {
            et1.setText((et1.getHint().toString().contains("32")?(""+(int)(Math.random()*100000)):(""+(int)(Math.random()*255))));
            et2.setText((et2.getHint().toString().contains("32")?(""+(int)(Math.random()*100000)):(""+(int)(Math.random()*255))));
            et3.setText((et3.getHint().toString().contains("32")?(""+(int)(Math.random()*100000)):(""+(int)(Math.random()*255))));
            et4.setText((et4.getHint().toString().contains("32")?(""+(int)(Math.random()*100000)):(""+(int)(Math.random()*255))));
            et5.setText((et5.getHint().toString().contains("32")?(""+(int)(Math.random()*100000)):(""+(int)(Math.random()*255))));
        });

        findViewById(R.id.clear).setOnClickListener(view ->
        {
            et1.setText("");
            et2.setText("");
            et3.setText("");
            et4.setText("");
            et5.setText("");
        });


    }

    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private EditText et5;
    private EditText deviceName;
    private Spinner spinner;

    private void setFields(int [] arr)
    {


        et1.post(() -> et1.setText(""+arr[0]));
        et2.post(() -> et2.setText(""+arr[1]));
        et3.post(() -> et3.setText(""+arr[2]));
        if(arr.length>=4)
            et4.post(() -> et4.setText(""+arr[3]));
        if(arr.length>=5)
            et5.post(() -> et5.setText(""+arr[4]));



    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.println(Log.DEBUG,"REQUEST", Arrays.toString(grantResults));

        if(blec!=null) blec.disconnect();
        blec = new BLEConnection(this, "0000babe-0000-1000-8000-00805f9b34fb", deviceName.getText().toString(), new BleControlManager() {
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
                    int [] res=new int[0]; ;

                    switch (spinner.getSelectedItem().toString())
                    {
                        case "0000cafe-0000-1000-8000-00805f9b34fb":
                            res = result.getData(new int[]{BluetoothData.UINT8_T,BluetoothData.UINT8_T,BluetoothData.UINT8_T});

                            break;
                        case "0000caff-0000-1000-8000-00805f9b34fb":
                            res = result.getData(new int[]{BluetoothData.UINT8_T,BluetoothData.UINT8_T,BluetoothData.INT32_T,BluetoothData.INT32_T});
                            break;
                        case "0000cb00-0000-1000-8000-00805f9b34fb":
                            res = result.getData(new int[]{BluetoothData.UINT8_T,BluetoothData.UINT8_T,BluetoothData.INT32_T,BluetoothData.INT32_T,BluetoothData.INT32_T});
                            break;
                    }


                    Log.println(Log.DEBUG,"dataforme", Arrays.toString(res));
                    setFields(res);
                }

                @Override
                public void onReadFail() {
                    runOnUiThread(()->Toast.makeText(context,"ReadFail", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onDataWrite() {
                    Toast.makeText(context,"Wrote down", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailed() {
                    Toast.makeText(context,"No Connection", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDataWriteFail() {
                    Toast.makeText(context,"Property Write No Responce", Toast.LENGTH_SHORT).show();
                }
            });
        //else
           // blec.discoverServices();
    }

}