package com.cathf.biketrace.biketrace;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by cathf on 2017/08/07.
 */

public class SensorSelectorActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String selected_sensor = (String) parent.getItemAtPosition(pos);
        Toast.makeText(parent.getContext(), "This is my selected sensor: " + selected_sensor, Toast.LENGTH_SHORT).show();

        // set the last_selected_name value to the selected_sensor
        SharedPreferences settings = parent.getContext().getSharedPreferences(String.valueOf(R.string.BiketracePrefsFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(String.valueOf(R.string.last_selected_name), selected_sensor);
        // Commit the edits!
        editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}