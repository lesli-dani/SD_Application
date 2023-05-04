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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
        sdf = new SimpleDateFormat("hh:mm:ss a");

        // Set up calendar objects
        sunriseCal = Calendar.getInstance();
        sunsetCal = Calendar.getInstance();
        currentCal = Calendar.getInstance();

        // Get sunrise and sunset times from API
        getSunriseSunsetData();

        //Get irradiance from API
        getIrradiance();

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
                if (btSocket.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Connection Successful!", Toast.LENGTH_SHORT).show();
                    try {
                        inputStream = btSocket.getInputStream();
                        inputStream.skip(inputStream.available());
                        myHandler.postDelayed(readBT, 1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
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
                    while (byteChar != '\n') {
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

        if (btSocket != null) {
            if (btSocket.isConnected()) {
                try {
                    btSocket.close();
                    if (!btSocket.isConnected()) {
                        Toast.makeText(getApplicationContext(), "Bluetooth was Disconnected!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Bluetooth Could Not Disconnect!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Bluetooth Could Not Disconnect!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
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
                            @SuppressLint("SetTextI18n") JsonObjectRequest newJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, nextURL, null,
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
                        Toast.makeText(getApplicationContext(), "Error retrieving Sunrise data.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(), "Unable to determine current time.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(getApplicationContext(), "Error retrieving Sunrise data!", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void getIrradiance() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.solcast.com.au/world_radiation/estimated_actuals?latitude=25.893907&longitude=-97.48691&output_parameters=ghi&format=json&api_key=Azvpit04Ws4BuY5jI8_4l-3KcGPJ-BGi&timezone=America%2FChicago";

        // Make a GET request to the API
        @SuppressLint("SetTextI18n") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String responseStr = response.toString();
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONArray estimatedActuals = jsonObject.getJSONArray("estimated_actuals");
                        for (int i = 0; i < estimatedActuals.length(); i++) {
                            JSONObject obj = estimatedActuals.getJSONObject(i);
                            String periodEnd = obj.getString("period_end");
                            double ghi = obj.getDouble("ghi");
                            if (isWithinCurrentDayAndInterval(periodEnd)) {
                                Irradiance.setText(ghi + " W/m^2");
                                return;
                            }
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error retrieving ghi irradiance data!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(), "Error retrieving ghi data!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Error retrieving Irradiance data!", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static boolean isWithinCurrentDayAndInterval(String dateTimeStr) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dateTime = sdf.parse(dateTimeStr);

        TimeZone timeZone = TimeZone.getTimeZone("America/Chicago");
        Calendar calendar = Calendar.getInstance(timeZone);
        Date currentDate = calendar.getTime();

        // Check if the date is within the current day
        calendar.setTime(currentDate);
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        assert dateTime != null;
        calendar.setTime(dateTime);
        int dateDay = calendar.get(Calendar.DAY_OF_YEAR);
        if (currentDay != dateDay) {
            return false;
        }

        // Check if the time is within the current 30-minute interval
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentIntervalStartMinute = currentMinute - currentMinute % 30;
        calendar.set(Calendar.MINUTE, currentIntervalStartMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date intervalStartDate = calendar.getTime();
        Date intervalEndDate = new Date(intervalStartDate.getTime() + 30 * 60 * 1000);

        return (currentDate.after(intervalStartDate) || currentDate.equals(intervalStartDate)) && currentDate.before(intervalEndDate);
    }


}



