package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    //class to implement Settings layout in GUI and get simultaneous/set-up settings
    private String TAG="SettingsActivity";
    //declare GUI elements
    private EditText wifiIpEditText, flashThresholdEditText,objectTrackEditText, backManualIdInput, frontManualIdInput;
    private Spinner backCameraSpinner, frontCameraSpinner;
    private CheckBox proximitySensorCheckBox, backCameraAutoId, frontCameraAutoId;
    private Button initializeProgramButton, backAddIdButton, frontAddIdButton;
    private TextView backCameraIdList, frontCameraIdList;
    //declare user input values (public)
    public static String wifiIp;
    public static boolean proximitySensorEnabled;
    public static int flashThreshold;
    public static int detectionType;
    public static List<String> backCameraIds = new ArrayList<>();
    public static List<String> frontCameraIds = new ArrayList<>();
    public static boolean backCameraAutoIdEnabled, frontCameraAutoIdEnabled;
    public static int backCameraSpinnerValue, frontCameraSpinnerValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize components
        initializeComponents(); //connect declared GUI elements to layout

        // Setup Spinners
        setupSpinners();

        // Set up button listeners
        setupButtonListeners();

        // Set up immediate settings listeners
        setupImmediateListeners();
        // Set up defaults for immediate settings
        wifiIp="192.168.4.1";
        proximitySensorEnabled=false;
        flashThreshold=5;
        detectionType=0;
    }

    private void initializeComponents() {
        wifiIpEditText = findViewById(R.id.wifi_ip);
        flashThresholdEditText = findViewById(R.id.flash_threshold);
        backManualIdInput = findViewById(R.id.back_manual_id_input);
        frontManualIdInput = findViewById(R.id.front_manual_id_input);
        backCameraSpinner = findViewById(R.id.back_camera_spinner);
        frontCameraSpinner = findViewById(R.id.front_camera_spinner);
        proximitySensorCheckBox = findViewById(R.id.proximity_sensor);
        backCameraAutoId = findViewById(R.id.back_camera_auto_id);
        frontCameraAutoId = findViewById(R.id.front_camera_auto_id);
        initializeProgramButton = findViewById(R.id.initialize_program_button);
        backAddIdButton = findViewById(R.id.back_add_id_button);
        frontAddIdButton = findViewById(R.id.front_add_id_button);
        backCameraIdList = findViewById(R.id.back_camera_id_list);
        frontCameraIdList = findViewById(R.id.front_camera_id_list);
        objectTrackEditText = findViewById(R.id.object_track);
    }

    private void setupSpinners() {
        //method to setup spinners from 0 to 3 for  both side cameras
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Integer[]{0, 1, 2, 3});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backCameraSpinner.setAdapter(adapter);
        frontCameraSpinner.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        backAddIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIdToList(backManualIdInput, backCameraIds, backCameraIdList, backCameraSpinnerValue);
                Log.i(TAG, "onClick of back camera list:"+ backCameraIds.toString());
            }
        });

        frontAddIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIdToList(frontManualIdInput, frontCameraIds, frontCameraIdList, frontCameraSpinnerValue);
                Log.i(TAG, "onClick of front camera list:"+ frontCameraIds.toString());
            }
        });

        backCameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                backCameraSpinnerValue = Integer.parseInt(backCameraSpinner.getSelectedItem().toString());
                backCameraIds.clear();
                updateTextView(backCameraIdList, backCameraIds);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        frontCameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                frontCameraSpinnerValue = Integer.parseInt(frontCameraSpinner.getSelectedItem().toString());
                frontCameraIds.clear();
                updateTextView(frontCameraIdList, frontCameraIds);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initializeProgramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeProgram();
            }
        });
    }
    private void setupImmediateListeners()
    {
        wifiIpEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                wifiIp=s.toString();
                Log.i(TAG, "afterTextChanged: "+ wifiIp);
            }
        });
        proximitySensorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                proximitySensorEnabled=isChecked;
                Log.i(TAG, "onCheckedChanged: " + isChecked);
            }
        });
        flashThresholdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    flashThreshold = Integer.parseInt(s.toString()); // Update the flashThreshold variable when text changes
                    Log.i(TAG, "afterTextChanged:flash "+s);
                } catch (NumberFormatException e) {
                    flashThreshold = 0; // Invalid number or empty input
                }
            }
        });
        objectTrackEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Log.i(TAG, "afterTextChanged:object "+s);
                    detectionType = Integer.parseInt(s.toString()); // Update the flashThreshold variable when text changes
                } catch (NumberFormatException e) {
                    detectionType = 0; // Invalid number or empty input
                }
            }
        });

    }

    private void addIdToList(EditText editText, List<String> idList, TextView textView, int maxListSize) {
        String id = editText.getText().toString();
        if (!id.isEmpty()) {
            if (idList.contains(id)) {
                showToast("ID already exists in the list");
                return;
            }

            if (idList.size() >= maxListSize) {
                idList.remove(0); // Remove the oldest entry
            }
            idList.add(id);
            updateTextView(textView, idList);
            editText.setText(""); // Clear the input field
        } else {
            showToast("Please enter an ID");
        }
    }

    private void initializeProgram() {
        backCameraAutoIdEnabled = backCameraAutoId.isChecked();
        frontCameraAutoIdEnabled = frontCameraAutoId.isChecked();
        backCameraSpinnerValue = Integer.parseInt(backCameraSpinner.getSelectedItem().toString());
        frontCameraSpinnerValue = Integer.parseInt(frontCameraSpinner.getSelectedItem().toString());
        setResult(RESULT_OK);
        finish();
        // TODO: Use these settings to initialize your program
    }

    private void updateTextView(TextView textView, List<String> data) {
        textView.setText(String.join(", ",data));
    }

    private void showToast(String message) {
        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}