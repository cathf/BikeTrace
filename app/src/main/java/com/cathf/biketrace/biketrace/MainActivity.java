package com.cathf.biketrace.biketrace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public String last_selected_name;
    public String last_selected_date_time_start;
    public String last_selected_date_time_end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the value of last_selected_name to whatever was saved to shared preferences last_selected_name.
        // Set to null if there was no last selected name
        SharedPreferences settings = getSharedPreferences(String.valueOf(R.string.BiketracePrefsFile), Context.MODE_PRIVATE);
        last_selected_name = settings.getString(String.valueOf(R.string.last_selected_name), "");
        last_selected_date_time_start = settings.getString(String.valueOf(R.string.last_selected_date_time_start), "");
        last_selected_date_time_end = settings.getString(String.valueOf(R.string.last_selected_date_time_end), "");


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        // set up BikeTrace Buttons
        setupSensorSelector();
        setupTestTextDate();
        setupTrackButton();
        setupFindBikeButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        // on pause of activity, always write last state values to the preferences file
        savePreferences(last_selected_name, last_selected_date_time_start, last_selected_date_time_end);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTrackButton()
    {
        // Get a reference to the Press Me Button
        final Button track_button = (Button) findViewById(R.id.track_on_off_button);

        // Set an OnClickListener on this Button
        // Called each time the user clicks the Button
        track_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(track_button.getText().equals("Track: OFF"))
                {
                    track_button.setText(R.string.track_on);
                    //TODO - get current date and time that button was pressed
                    String button_press_time = getCurrentDateTime();

                    EditText test_date_start = (EditText) findViewById(R.id.date_time_test_text_date_start);
                    EditText test_date_end = (EditText) findViewById(R.id.date_time_test_text_date_end);
                    // get the most recently saved preferences and set them before changing anything
                    getPreferences();
                    last_selected_date_time_start = test_date_start.getText().toString();
                    last_selected_date_time_end = test_date_end.getText().toString();
                    savePreferences(last_selected_name, last_selected_date_time_start, last_selected_date_time_end);
                    Log.i("INFO: Tracking ON", "");
                }
                else if(track_button.getText().equals("Track: ON"))
                {

                    track_button.setText(R.string.track_off);
                    // get the most recently saved preferences and set them before changing anything
                    getPreferences();
                    last_selected_date_time_start = "";
                    last_selected_date_time_end = "";
                    savePreferences(last_selected_name, last_selected_date_time_start, last_selected_date_time_end);
                    Log.i("INFO: Tracking OFF", "");
                }

                boolean x = startTrackActivity();

            }
        });
    }

    private boolean startTrackActivity()
    {
        //Toast.makeText(getApplicationContext(), "Send Stop/Start Signal to sensor", Toast.LENGTH_SHORT).show();
        Log.i("INFO: Starting up", " startTrackActivity()");
        //TODO: Send sms to sensor to start/stop tracking. Currently just return true.

        return true;

    }

    private void setupFindBikeButton()
    {
        // Get a reference to the Press Me Button
        final Button find_bike_button = (Button) findViewById(R.id.find_bike_button);
        final Button track_button = (Button) findViewById(R.id.track_on_off_button);

        // Set an OnClickListener on this Button
        // Called each time the user clicks the Button
        find_bike_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(last_selected_date_time_start != "" & track_button.getText().equals("Track: ON")) {
                    Log.i("INFO: Starting up", "startFindBikeActivity()");
                    startFindBikeActivity();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please set tracking to ON before searching for bike data", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void startFindBikeActivity()
    {
        Log.i("INFO: ", "Find My Bike Button Pressed");
        Intent FindBikeInput = new Intent(MainActivity.this, StartFindBikeActivity2.class);
        startActivity(FindBikeInput);
    }

    private void setupSensorSelector(){
        Spinner sensor_spinner = (Spinner) findViewById(R.id.sensor_selector);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensor_list, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sensor_spinner.setAdapter(adapter);

        // get the most recently saved preferences and set them before changing anything
        getPreferences();

        // only set the deafult of the last_selected_name if there was a name selected previously.
        if(last_selected_name != "")
        {
            Log.i("last_selected_name:",last_selected_name);
            sensor_spinner.setSelection(adapter.getPosition(last_selected_name));
        }

        sensor_spinner.setOnItemSelectedListener(new SensorSelectorActivity());
    }

    private void setupTestTextDate(){
        EditText test_date_start = (EditText) findViewById(R.id.date_time_test_text_date_start);
        EditText test_date_end = (EditText) findViewById(R.id.date_time_test_text_date_end);

        if(last_selected_date_time_start == "" | last_selected_date_time_start == null)
        {
            last_selected_date_time_start = test_date_start.getText().toString();
        }
        else
        {
            test_date_start.setText(last_selected_date_time_start);
        }

        if(last_selected_date_time_end == "" | last_selected_date_time_end == null)
        {
            last_selected_date_time_end = test_date_end.getText().toString();
        }
        else
        {
            test_date_end.setText(last_selected_date_time_end);
        }
    }

    public void savePreferences (String lsn, String lsdts, String lsdte)
    {
        // Save the preferences of last_selected_name and last_selected_date_time_start so that they can be referenced correctly by other activities
        Log.i("INFO: ", "Saving last_selected_name and last_selected_date_time_start as " + lsn + ", " + lsdts);
        SharedPreferences settings = getSharedPreferences(String.valueOf(R.string.BiketracePrefsFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(String.valueOf(R.string.last_selected_date_time_start), lsdts);
        editor.putString(String.valueOf(R.string.last_selected_date_time_end), lsdte);
        editor.putString(String.valueOf(R.string.last_selected_name), lsn);
        editor.commit();
    }

    public void getPreferences ()
    {
        // Save the preferences of last_selected_name and last_selected_date_time_start so that they can be referenced correctly by other activities
        Log.i("INFO: ", "Old values for last_selected_name and last_selected_date_time_start as " + last_selected_name + ", " + last_selected_date_time_start);
        SharedPreferences settings = getSharedPreferences(String.valueOf(R.string.BiketracePrefsFile), Context.MODE_PRIVATE);
        last_selected_date_time_start = settings.getString(String.valueOf(R.string.last_selected_date_time_start), "");
        last_selected_date_time_end = settings.getString(String.valueOf(R.string.last_selected_date_time_end), "");
        last_selected_name = settings.getString(String.valueOf(R.string.last_selected_name), "");
        Log.i("INFO: ", "New values for last_selected_name and last_selected_date_time_start as " + last_selected_name + ", " + last_selected_date_time_start);
    }

    private String getCurrentDateTime()
    {
        // return the current date and time of the user formatted nicely for the user
        //TODO: add some sort of timezone indicator
        Calendar c = Calendar.getInstance();
        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatted_date = df.format(c.getTime());
        return formatted_date;
    }

}