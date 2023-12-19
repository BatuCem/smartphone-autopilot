package com.example.wifihttpokdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ipActivity extends AppCompatActivity {

    private Button enterIp;
    private EditText ipInput;
    public static String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        ipInput=(EditText) findViewById(R.id.ipInput);
        enterIp=(Button) findViewById(R.id.enterButton);
        enterIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress=ipInput.getText().toString();
                Intent intent=new Intent(ipActivity.this,ButtonActivity.class);
                startActivity(intent);
            }
        });

    }
}