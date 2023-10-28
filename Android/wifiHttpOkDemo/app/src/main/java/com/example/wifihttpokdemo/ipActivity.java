package com.example.wifihttpokdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ipActivity extends AppCompatActivity {

    private Button enterIp;//define button to check ip entry
    private EditText ipInput;//define text edit space default by the given ESP32 IP:192.168.4.1
    public static String ipAddress;//externally accessible ip address taken in this context, used in other activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);   //connect to activity XML
        ipInput=(EditText) findViewById(R.id.ipInput);  //connect text field in layout and variable
        enterIp=(Button) findViewById(R.id.enterButton);//connect button in layout and definition
        enterIp.setOnClickListener(new View.OnClickListener() { //Set listener on button
            @Override
            public void onClick(View v) {   //do on each click
                ipAddress=ipInput.getText().toString(); //get string from text field
                Intent intent=new Intent(ipActivity.this,ButtonActivity.class); //switch to next activity
                startActivity(intent);
            }
        });

    }
}