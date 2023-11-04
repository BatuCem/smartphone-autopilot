package com.example.wifihttpokdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
//Defines Activity where command tx will commence
public class ButtonActivity extends AppCompatActivity implements View.OnClickListener {
    private Button[] buttons =new Button[5];              //Define button array

    Executor executor= Executors.newSingleThreadExecutor(); //get executor to do background tasks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);   //connect layout with XML
        buttons[0]=findViewById(R.id.forwardButton);     //connect forward button in layout
        buttons[1]=findViewById(R.id.backwardButton);   //connect backward move button in layout
        buttons[2]=findViewById(R.id.rightButton);      //connect turn right button in layout
        buttons[3]=findViewById(R.id.leftButton);       //connect turn left button in layout
        buttons[4]=findViewById(R.id.stopButton);       //connect stop button in layout

        for(int i=0;i<buttons.length;i++)
        {
            buttons[i].setOnClickListener(this);
        }
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

    @Override
    public void onClick(View v) {
        requestToUrl("S");
        final int viewId = v.getId();
        if(viewId ==R.id.forwardButton)
        {
            requestToUrl("F");
            Toast.makeText(this, "moving forward", Toast.LENGTH_SHORT).show();
        }
        else if (viewId==R.id.backwardButton)
        {
            requestToUrl("B");
            Toast.makeText(this, "moving backward", Toast.LENGTH_SHORT).show();
        }
        else if (viewId==R.id.rightButton)
        {
            requestToUrl("R");
            Toast.makeText(this, "turning right", Toast.LENGTH_SHORT).show();
        }
        else if (viewId==R.id.leftButton)
        {
            requestToUrl("L");
            Toast.makeText(this, "turning left", Toast.LENGTH_SHORT).show();
        }
        else {
            //Do nothing for now, just report stop
            Toast.makeText(this, "stopping", Toast.LENGTH_SHORT).show();
        }

    }
}