package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class Information extends AppCompatActivity {
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    private TextView Temperature, Voltage, Current;
    private final Handler myHandler = new Handler();
    private InputStream inputStream;
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_screen);
        Objects.requireNonNull(getSupportActionBar()).hide();
        btSocket = null;
        Current = (TextView) findViewById(R.id.currentText);
        Voltage = (TextView) findViewById(R.id.voltageText);
        Temperature = (TextView) findViewById(R.id.tempText);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void connectToArduino(View view) {

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Information.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            return;
        }

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("98:DA:60:02:61:08");

        int counter = 0;
        do {
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);

                btSocket.connect();
                if(btSocket.isConnected())
                {
                    Toast.makeText(getApplicationContext(), "Connection Successful!", Toast.LENGTH_SHORT).show();
                    try {
                        inputStream = btSocket.getInputStream();
                        inputStream.skip(inputStream.available());
                        myHandler.postDelayed(readBT, 1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Bluetooth Could Not Connect!", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Bluetooth Is Already Connected!", Toast.LENGTH_SHORT).show();
            }
            counter++;
        } while (!Objects.requireNonNull(btSocket).isConnected() && counter < 3);

    }

    private final Runnable readBT = new Runnable() {

        @SuppressLint("DefaultLocale")
        public void run() {

            try {
                if (inputStream.available() > 3) {
                    StringBuilder S = new StringBuilder();
                    byte b;
                    char byteChar = 'z';
                    while(byteChar != '\n') {
                        b = (byte) inputStream.read();
                        byteChar = (char) b;
                        S.append(byteChar);
                    }

                    //allText is gonna be the characters up to the new line
                    String allText = S.toString();
                    String[] sText = allText.split(",");

                    //send temperature data
                    Temperature.setText("");
                    Temperature.append(sText[0] + " \u2103");
                    Temperature.append(System.getProperty("line.separator"));
                    Temperature.append(sText[1] + " \u2109");

                    //send current data
                    Current.setText("");
                    Current.append(sText[2]);

                    //send voltage data
                    Voltage.setText("");
                    Voltage.append(sText[3]);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (btSocket.isConnected())
                myHandler.postDelayed(this, 100);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void disconnectFromArduino(View view) {

        if(btSocket != null)
        {
            if(btSocket.isConnected()){
                try {
                    btSocket.close();
                    if(!btSocket.isConnected())
                    {
                        Toast.makeText(getApplicationContext(), "Bluetooth was Disconnected!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Bluetooth Could Not Disconnect!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Bluetooth Could Not Disconnect!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        else
        {
            Toast.makeText(getApplicationContext(), "No Connected BT Device To Disconnect From!", Toast.LENGTH_SHORT).show();
        }
    }
}