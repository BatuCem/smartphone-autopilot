package com.example.wifihttpokdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ButtonActivity extends AppCompatActivity {
    private Button button;
    private boolean buttonSavedState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);
        button=findViewById(R.id.buttonToggle);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonSavedState==true)
                {
                    //TODO: turn off
                    buttonSavedState=false;
                }
                else
                {
                    //TODO:turn on
                    buttonSavedState=true;
                }

            }
        });

    }

}