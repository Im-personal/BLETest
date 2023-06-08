package com.resant.bletest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            BLEConnection blec = new BLEConnection(this, "0000cafe-0000-1000-8000-00805f9b34fb", new BleControlManager() {
                @Override
                public void onDataRecived() {

                }

                @Override
                public void onConnection() {

                }

                @Override
                public void noBluetoothPermission(String bluetoothConnect) {

                }

                @Override
                public void onRead(BLEConnection result) {
                    result.getData(new int[]{BLEConnection.UINT8_T,BLEConnection.UINT8_T,BLEConnection.UINT8_T});
                }

                @Override
                public void onReadFail() {

                }
            });




        Button send = findViewById(R.id.button);
        send.setOnClickListener(view -> {


            EditText protocol_version = findViewById(R.id.editTextText);
            EditText hardware_type = findViewById(R.id.editTextText2);
            EditText hardware_version = findViewById(R.id.editTextText3);

            blec.addSendData_uint8_t(Integer.parseInt(protocol_version.getText().toString()));
            blec.addSendData_uint8_t(Integer.parseInt(hardware_type.getText().toString()));
            blec.addSendData_uint8_t(Integer.parseInt(hardware_type.getText().toString()));

            blec.writeData();
        });

        Button read = findViewById(R.id.button2);
        read.setOnClickListener(view ->
        {
            blec.readData();
        });

    }
}