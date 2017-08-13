package com.cathf.biketrace.biketrace;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Double.parseDouble;

public class StartFindBikeActivity2 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> productsList;
    ArrayList<LatLng> markerPoints;

    private static String url_all_products = "http://ec2-34-250-175-136.eu-west-1.compute.amazonaws.com/BikeTrace/get_bike_data_mysql.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BIKEDATA = "sensor_data";
    private static final String TAG_DATAID = "data_id";
    private static final String TAG_NAME = "sensor_name";
    private static final String TAG_GPSLOCATION = "gps_location";
    private static final String TAG_DATETIMETZ = "date_time_tz";
    private static final String TAG_DATETIME = "date_time";

    // products JSONArray
    JSONArray bike_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_find_bike2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getApplicationContext(), "Generating Map...", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        productsList = new ArrayList<HashMap<String, String>>();
        markerPoints = new ArrayList<LatLng>();

        new LoadAllBikes().execute();

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllBikes extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(StartFindBikeActivity2.this);
            pDialog.setMessage("Loading bike map points. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            SharedPreferences settings = getSharedPreferences(String.valueOf(R.string.BiketracePrefsFile), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            //TODO: remove cath as the default
            String last_selected_name = settings.getString(String.valueOf(R.string.last_selected_name), "cath");
            //TODO: remove this default - set a year in advance for testing
            String last_selected_date_time = settings.getString(String.valueOf(R.string.last_selected_date_time), "2018-08-12 01:00:00");

            Log.i("INFO: ", "Information retrieved for " + last_selected_name + ", " + last_selected_date_time);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //TODO: set params name and date from the app variables
            //TODO: make app not crash if it doesn't find any data points
            params.add(new BasicNameValuePair("s", last_selected_name));
            params.add(new BasicNameValuePair("d", last_selected_date_time));


            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
            //JSONObject json = jParser.makeHttpRequest(url_all_products, "GET");

            // Check your log cat for JSON reponse
            Log.i("INFO: ", "All Bikes json string " + json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    bike_data = json.getJSONArray(TAG_BIKEDATA);

                    // looping through All Products
                    for (int i = 0; i < bike_data.length(); i++) {
                        JSONObject c = bike_data.getJSONObject(i);

                        // Storing each json item in variable
                        String data_id = c.getString(TAG_DATAID);
                        String sensor_name = c.getString(TAG_NAME);
                        String gps_location = c.getString(TAG_GPSLOCATION);
                        String date_time_tz = c.getString(TAG_DATETIMETZ);
                        String date_time = c.getString(TAG_DATETIME);

                        if (!gps_location.isEmpty()) {
                            String[] lat_long_string = gps_location.split(",");

                            if(lat_long_string.length == 2) {

                                try
                                {
                                    Double gps_lat = parseDouble(lat_long_string[0]);
                                    Double gps_long = parseDouble(lat_long_string[1]);
                                    LatLng gps_markers = new LatLng(gps_lat, gps_long);
                                    markerPoints.add(gps_markers);
                                }
                                catch (NumberFormatException e)
                                {
                                    Log.i("Error: ", "Could not convert lat_long_sting to GPS Data " + lat_long_string + ". This point will not be displayed on the map.");
                                }
                            }
                            else {
                                Log.i("INFO: ", "lat_long_string variable was not the correct length");
                            }
                        }
                    }
                }
                else {
                    //markerPoints.add(new LatLng(parseDouble("0.00"), parseDouble("0.00")));
                    Log.i("Error: ", "No data was found for this item, setting default location to lat = 0.00 and long = 0.00");
                    //Toast.makeText(getApplicationContext(), "No data was found for this item, setting default location to lat = 0 and long = 0.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();

            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into map
                     * */
                    if(markerPoints.size() > 0)
                    {
                        Iterator<LatLng> iterator = markerPoints.iterator();
                        int count = 1;

                        while (iterator.hasNext()) {
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(iterator.next());
                            mMap.addMarker(markerOptions.title("Marker " + String.valueOf(count)));
                            count = count+1;
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoints.get(0)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPoints.get(0), 15));
                    }
                    else
                    {
                        // set the default marker on mMap for position 0, 0 and don't zoom if there are no points available.
                        LatLng default_lat_lng = new LatLng(0.0, 0.0);
                        //mMap.addMarker(new MarkerOptions().position(default_lat_lng));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(default_lat_lng));
                        Toast.makeText(getApplicationContext(), "No points to display", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

}
