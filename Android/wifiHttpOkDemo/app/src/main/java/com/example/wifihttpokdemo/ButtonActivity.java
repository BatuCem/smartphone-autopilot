package com.example.wifihttpokdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ButtonActivity extends AppCompatActivity {
    private SeekBar seekBarL, seekBarR;
    private TextView textL,textR;
    private int leftState,rightState;
    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);
        seekBarL=findViewById(R.id.seekBarL);
        seekBarR=findViewById(R.id.seekBarR);
        textL=findViewById(R.id.textViewL);
        textR=findViewById(R.id.textViewR);
        seekBarL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leftState=progress;
                requestToUrl(convertIntegers(leftState,rightState));
                textL.setText(Integer.toString(leftState));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rightState=progress;
                requestToUrl(convertIntegers(leftState,rightState));
                textR.setText(Integer.toString(rightState));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
    void requestToUrl(String command){      //make request from given command
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  //Get Connectivity Service
        Network network=connectivityManager.getActiveNetwork();     //Get the Active network ->needs to be non-null
        NetworkCapabilities networkCapabilities= connectivityManager.getNetworkCapabilities(network); //get Capabilities, needs to be non-null with WiFi Transport
        if(network!=null && networkCapabilities!=null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ){ //validate connection case
            executor.execute(new Runnable() {   //set execution on executor thread
                @Override
                public void run() {     //execute when running
                    wifiManager.getUrl("http://" + ipActivity.ipAddress + "/" + command);   //format command by given ip address on previous intent

                }
            });
        }
        else {
            Toast.makeText(ButtonActivity.this, "Device Not Connected!", Toast.LENGTH_SHORT).show();    //give connection error via pop-up toast
        }
    }
    public static String convertIntegers (int a, int b)
    {
        String aStr = (a >= 0 ? "+" : "-") + String.format("%03d", Math.abs(a));
        String bStr = (b >= 0 ? "+" : "-") + String.format("%03d", Math.abs(b));
        return aStr + " " + bStr;
    }

}