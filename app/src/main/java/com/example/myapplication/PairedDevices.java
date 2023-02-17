package com.example.myapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Objects;
import java.util.Set;


public class PairedDevices extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private TextView pairText;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paired_devices);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Button pairButton = findViewById(R.id.pairButton);
        pairText = (TextView) findViewById(R.id.device_names);

        pairButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(PairedDevices.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= 31) {
                    ActivityCompat.requestPermissions(PairedDevices.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                    return;
                }
            }
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (Build.VERSION.SDK_INT >= 31) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            } else {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceAddy = device.getAddress();
                    pairText.append(deviceName + "   |   " + deviceAddy + "\n");
                }
                pairText.setMovementMethod(LinkMovementMethod.getInstance());
                pairText.setText(makeAsClickableLines(pairText.getText()), TextView.BufferType.SPANNABLE);
            } else {
                Toast.makeText(getApplicationContext(), "No Paired Devices", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private CharSequence makeAsClickableLines (CharSequence charSequence){
        if (charSequence.length() == 0)
            return charSequence;

        String text = charSequence.toString().trim();

        if (text.contains("\n") && text.lastIndexOf("\n") == text.length() - 1) {
            text = text.substring(0, text.length() - 2);
        }
        text = text.replaceAll("\\n+", "\n");
        if (text.contains("\n") && text.indexOf("\n") == 0) {
            text = text.substring(1, text.length() - 1);
        }

        String[] lines = text.split("\n");

        SpannableStringBuilder ssb = new SpannableStringBuilder();

        int currentStartGlobal = 0;

        for (int i = 0; i < lines.length; i++) {

            final String currentLine = lines[i];
            final int currentStart = currentStartGlobal;
            final int currentLength = currentLine.length();
            final String currentText = text;

            SpannableString sb = new SpannableString(currentLine);

            sb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Log.d(TAG, "onClick: " + currentLine + " " + currentStart + ":" + (currentStart + currentLength));

                    String s1 = currentText.substring(0, currentStart);

                    int startWithLength = currentStart + currentLength + 1;
                    startWithLength = Math.min(startWithLength, currentText.length());

                    String s2 = currentText.substring(startWithLength);

                    Log.d(TAG, "onClick: " + s1 + " " + s2);

                    ((TextView) widget).setText(makeAsClickableLines(s1 + s2));
                }
            }, 0, currentLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ssb.append(sb).append(i + 1 > lines.length ? "" : "\n");

            currentStartGlobal += currentLength;
        }

        return ssb;
    }

}
