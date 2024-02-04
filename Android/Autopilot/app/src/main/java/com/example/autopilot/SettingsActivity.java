package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private String TAG="SettingsActivity";

    private EditText wifiIpEditText, flashThresholdEditText, backManualIdInput, frontManualIdInput;
    private Spinner backCameraSpinner, frontCameraSpinner;
    private CheckBox proximitySensorCheckBox, backCameraAutoId, frontCameraAutoId;
    private Button initializeProgramButton, backAddIdButton, frontAddIdButton;
    private ListView backCameraIdList, frontCameraIdList;

    private String wifiIp;
    private boolean proximitySensorEnabled;
    private int flashThreshold;
    private List<String> backCameraIds = new ArrayList<>();
    private List<String> frontCameraIds = new ArrayList<>();
    private boolean backCameraAutoIdEnabled, frontCameraAutoIdEnabled;
    private int backCameraSpinnerValue, frontCameraSpinnerValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize components
        initializeComponents();

        // Setup Spinners
        setupSpinners();

        // Set up button listeners
        setupButtonListeners();
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
    }

    private void setupSpinners() {
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
                updateListView(backCameraIdList, backCameraIds);
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
                updateListView(frontCameraIdList, frontCameraIds);
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

    private void addIdToList(EditText editText, List<String> idList, ListView listView, int maxListSize) {
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
            updateListView(listView, idList);
            editText.setText(""); // Clear the input field
        } else {
            showToast("Please enter an ID");
        }
    }

    private void initializeProgram() {
        wifiIp = wifiIpEditText.getText().toString();
        proximitySensorEnabled = proximitySensorCheckBox.isChecked();
        flashThreshold = Integer.parseInt(flashThresholdEditText.getText().toString());
        backCameraAutoIdEnabled = backCameraAutoId.isChecked();
        frontCameraAutoIdEnabled = frontCameraAutoId.isChecked();
        backCameraSpinnerValue = Integer.parseInt(backCameraSpinner.getSelectedItem().toString());
        frontCameraSpinnerValue = Integer.parseInt(frontCameraSpinner.getSelectedItem().toString());

        // TODO: Use these settings to initialize your program
    }

    private void updateListView(ListView listView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
    }

    private void showToast(String message) {
        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}