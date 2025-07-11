package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsActivity extends AppCompatActivity {
    //class to implement Settings layout in GUI and get simultaneous/set-up settings
    private String TAG="SettingsActivity";


    //declare GUI elements
    private EditText wifiIpEditText, flashThresholdEditText, backManualIdInput, frontManualIdInput;
    private Spinner backCameraSpinner, frontCameraSpinner, objectTrackSpinner, operationModeSpinner,driveModeSpinner;
    private CheckBox proximitySensorCheckBox, backCameraAutoId, frontCameraAutoId;
    private Button initializeProgramButton, backAddIdButton, frontAddIdButton;
    private TextView backCameraIdList, frontCameraIdList;
    private SeekBar pBar, iBar, dBar;
    //declare user input values (public)
    public static String wifiIp = "191.168.4.1";
    public static boolean proximitySensorEnabled = false;
    public static int flashThreshold = 5;
    public static List<String> backCameraIds = new ArrayList<>();
    public static List<String> frontCameraIds = new ArrayList<>();
    public static boolean backCameraAutoIdEnabled = true, frontCameraAutoIdEnabled = true;
    public static int backCameraSpinnerValue, frontCameraSpinnerValue;
    public static int detectionType=0;
    public static int operationMode=0;
    public static int driveMode = 0;
    private TextView pTextView, iTextView, dTextView;
    public static Double P=1.0,I=0.0,D=0.0;

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

        //Set up seek bar listeners
        setupSeekBarListeners();

        // Set up immediate settings listeners
        setupImmediateListeners();
    }

    private void initializeComponents() {
        pTextView = findViewById(R.id.pTextView);
        iTextView = findViewById(R.id.iTextView);
        dTextView = findViewById(R.id.dTextView);
        wifiIpEditText = findViewById(R.id.wifi_ip);
        wifiIpEditText.setText(wifiIp);
        flashThresholdEditText = findViewById(R.id.flash_threshold);
        flashThresholdEditText.setText(Integer.toString(flashThreshold));
        backManualIdInput = findViewById(R.id.back_manual_id_input);
        frontManualIdInput = findViewById(R.id.front_manual_id_input);
        backCameraSpinner = findViewById(R.id.back_camera_spinner);
        frontCameraSpinner = findViewById(R.id.front_camera_spinner);
        proximitySensorCheckBox = findViewById(R.id.proximity_sensor);
        proximitySensorCheckBox.setChecked(proximitySensorEnabled);
        backCameraAutoId = findViewById(R.id.back_camera_auto_id);
        backCameraAutoId.setChecked(backCameraAutoIdEnabled);
        frontCameraAutoId = findViewById(R.id.front_camera_auto_id);
        frontCameraAutoId.setChecked(frontCameraAutoIdEnabled);
        initializeProgramButton = findViewById(R.id.initialize_program_button);
        backAddIdButton = findViewById(R.id.back_add_id_button);
        frontAddIdButton = findViewById(R.id.front_add_id_button);
        backCameraIdList = findViewById(R.id.back_camera_id_list);
        frontCameraIdList = findViewById(R.id.front_camera_id_list);
        objectTrackSpinner = findViewById(R.id.object_track);
        operationModeSpinner = findViewById(R.id.operation_mode);
        driveModeSpinner = findViewById(R.id.drive_mode);
        pBar = findViewById(R.id.pBar);
        iBar = findViewById(R.id.iBar);
        dBar = findViewById(R.id.dBar);
        pTextView.setText("P: "+P);
        dTextView.setText("D: "+D);
        iTextView.setText("I: "+I);
        pBar.setProgress(P.intValue()*200);
        iBar.setProgress(I.intValue()*200);
        dBar.setProgress(D.intValue()*200);

    }

    private void setupSpinners() {
        //method to setup spinners from 0 to 3 for  both side cameras
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Integer[]{0, 1, 2, 3});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backCameraSpinner.setAdapter(adapter);
        backCameraSpinner.setSelection(backCameraSpinnerValue);
        frontCameraSpinner.setAdapter(adapter);
        frontCameraSpinner.setSelection(frontCameraSpinnerValue);
        ArrayAdapter<String> adapterString = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DetectionTensorflow.labels);
        adapterString.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        objectTrackSpinner.setAdapter(adapterString);
        objectTrackSpinner.setSelection(detectionType);
        ArrayAdapter<String> adapterMode = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Tracking","GPS Based Solver","Free Roam","Remote Control"});
        operationModeSpinner.setAdapter(adapterMode);
        operationModeSpinner.setSelection(operationMode);
        ArrayAdapter<String> adapterDrive = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"4WD","Steering Drive"});
        driveModeSpinner.setAdapter(adapterDrive);
        driveModeSpinner.setSelection(driveMode);

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

        objectTrackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                detectionType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        operationModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                operationMode=position;
                Log.i(TAG, "onItemSelected: "+position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        driveModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                driveMode=position;
                Log.i(TAG, "onItemSelected: "+position);
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


    }
    private void setupSeekBarListeners()
    {
        pBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {;
                P = progress/200.0;
                pTextView.setText("P: "+P);
                Log.i(TAG, "onProgressChanged: P"+ P);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        iBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                I = progress/200.0;;
                iTextView.setText("I: "+I);
                Log.i(TAG, "onProgressChanged: I"+ progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        dBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                D = progress/200.0;
                dTextView.setText("D: "+D);
                Log.i(TAG, "onProgressChanged: D"+ D);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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