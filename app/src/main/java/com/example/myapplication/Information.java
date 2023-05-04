package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;


public class Information extends AppCompatActivity {
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    private TextView Temperature, solarPanel, Output, shutOff, Irradiance, Battery;
    private final Handler myHandler = new Handler();
    private InputStream inputStream;
    private String sunriseTime;
    private String sunriseTime2;
    private String sunsetTime;
    private SimpleDateFormat sdf;
    private TimeZone utcTZ;
    private TimeZone centralTZ;
    private Calendar sunriseCal;
    private Calendar sunsetCal;
    private Calendar currentCal;


    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_screen);
        Objects.requireNonNull(getSupportActionBar()).hide();
        btSocket = null;
        solarPanel = findViewById(R.id.solarPanel);
        Output = findViewById(R.id.output);
        Temperature = findViewById(R.id.tempText);
        shutOff = findViewById(R.id.sunTime);
        Irradiance = findViewById(R.id.irradiance);
        Battery = findViewById(R.id.batteryText);

        // Set up time zone and date format
        utcTZ = TimeZone.getTimeZone("UTC");
        centralTZ = TimeZone.getTimeZone("US/Central");
        sdf = new SimpleDateFormat("hh:mm:ss a");

        // Set up calendar objects
        sunriseCal = Calendar.getInstance();
        sunsetCal = Calendar.getInstance();
        currentCal = Calendar.getInstance();

        // Get sunrise and sunset times from API
        getSunriseSunsetData();

        Irradiance.setText("812.4");
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
                    solarPanel.setText("");
                    solarPanel.append("Current: " + sText[2] + "\n");
                    solarPanel.append("Voltage: " + sText[3] + "\n");
                    solarPanel.append("Power: " + sText[4]);

                    //send voltage data
                    Output.setText("");
                    Output.append("Current: " + sText[5] + "\n");
                    Output.append("Voltage: " + sText[6] + "\n");
                    Output.append("Power: " + sText[7]);

                    //send battery voltage data
                    Battery.setText("");
                    Battery.append("Current: " + sText[8] + "\n");
                    Battery.append("Voltage: " + sText[9] + "\n");
                    Battery.append("Power: " + sText[10]);
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

    private void getSunriseSunsetData() {
        String url = "https://api.sunrise-sunset.org/json?lat=25.901747&lng=-97.497482&date=today";
        @SuppressLint("SetTextI18n") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse sunrise and sunset times from API response
                        JSONObject results = response.getJSONObject("results");
                        sunriseTime = results.getString("sunrise");
                        sunsetTime = results.getString("sunset");

                        TimeZone centralTZ = TimeZone.getTimeZone("America/Chicago");
                        int rawOffset = centralTZ.getRawOffset();
                        int dstSavings = 3600000; // 1 hour
                        centralTZ.setRawOffset(rawOffset + dstSavings);

                        // Convert sunrise and sunset times to central time
                        sdf.setTimeZone(utcTZ);
                        sunriseCal.setTime(Objects.requireNonNull(sdf.parse(sunriseTime)));
                        sunsetCal.setTime(Objects.requireNonNull(sdf.parse(sunsetTime)));
                        sdf.setTimeZone(centralTZ);
                        sunriseCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
                        sunriseCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
                        sunriseCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
                        sunsetCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
                        sunsetCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
                        sunsetCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
                        String sunriseTime = sdf.format(sunriseCal.getTime());
                        String sunsetTime = sdf.format(sunsetCal.getTime());

                        // Determine whether it's before or after sunrise/sunset
                        boolean isBeforeSunrise = currentCal.before(sunriseCal);
                        boolean isBeforeSunset = currentCal.before(sunsetCal);
                        if (isBeforeSunrise) {
                            // If it's before sunrise, display the sunrise time
                            shutOff.setText(sunriseTime);
                        } else if (isBeforeSunset) {
                            // If it's after sunrise but before sunset, display the sunset time
                            shutOff.setText(sunsetTime);
                        } else {
                            // If it's after sunset, display sunrise time for the next day
                            String nextURL = "https://api.sunrise-sunset.org/json?lat=25.901747&lng=-97.497482&date=tomorrow";
                            @SuppressLint("SetTextI18n") JsonObjectRequest newJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                    answer -> {
                                        try {
                                            // Parse sunrise and sunset times from API answer
                                            JSONObject results2 = answer.getJSONObject("results");
                                            sunriseTime2 = results2.getString("sunrise");
                                            // Convert sunrise and sunset times to central time
                                            sdf.setTimeZone(utcTZ);
                                            sunriseCal.setTime(Objects.requireNonNull(sdf.parse(sunriseTime2)));
                                            sdf.setTimeZone(centralTZ);
                                            String sunriseTime2 = sdf.format(sunriseCal.getTime());
                                            shutOff.setText(sunriseTime2);

                                        } catch (JSONException e) {
                                            Toast.makeText(getApplicationContext(), "Error retrieving data 2.", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }, error -> {
                                Toast.makeText(getApplicationContext(), "Error retrieving data 2!", Toast.LENGTH_SHORT).show();
                                error.printStackTrace();
                            });
                            RequestQueue requestQueue = Volley.newRequestQueue(this);
                            requestQueue.add(newJsonObjectRequest);
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error retrieving data.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(), "Unable to determine current time.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(getApplicationContext(), "Error retrieving data!", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}



