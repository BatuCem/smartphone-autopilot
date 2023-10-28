package com.example.wifihttpokdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
//Defines Activity where command tx will commence
public class ButtonActivity extends AppCompatActivity {
    private Button button;              //Define button
    private boolean buttonSavedState;   //Save current state of the LED

    Executor executor= Executors.newSingleThreadExecutor(); //get executor to do background tasks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);   //connect layout with XML
        button=findViewById(R.id.buttonToggle);     //connect button in layout
        button.setOnClickListener(new View.OnClickListener() { //set listener on button
            @Override
            public void onClick(View v) {   //do when button is clicked
                if(buttonSavedState==true)  //if LED was on
                {
                    requestToUrl("L");      //Turn off LED by sending ".../L"
                    buttonSavedState=false;          //save turned off state
                }
                else    //else consider led was off
                {
                    requestToUrl("H");      //Turn on LED by sending ".../H"
                    buttonSavedState=true;           //save turned on state
                }

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

}