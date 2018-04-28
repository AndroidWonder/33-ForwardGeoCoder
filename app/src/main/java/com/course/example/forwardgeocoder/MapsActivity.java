/*This application is a forward GeoCoder example. It will take a location,
find its latitude and longitude, and plot this with a Google Maps marker.
It uses the Google Geocoder API and Google Maps.
Click the marker for it's title text.

The user needs a Geocoder Google Cloud project which will give
an API permission key.

*/
package com.course.example.forwardgeocoder;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button button;
    private EditText text;
    private String location;
    private LatLng finalPosition;
    private static final float zoom = 14.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        text = (EditText) findViewById(R.id.field);
        button = (Button) findViewById(R.id.go_button);

        /*Clicking the button will create a background thread to use the GeoCoder API
        using http client services. It waits for the thread to finish, then positions
        the map to the latitude longitude returned by the API.
        */
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //get location for query string
                location = text.getText().toString();
                text.setText("");

                //start the thread
                Thread t = new Thread(background);
                t.start();

                //wait for thread to finish
                try {
                    t.join();
                } catch(InterruptedException e) {}

                //position map and add marker
           //     mMap.clear();
                mMap.addMarker(new MarkerOptions().position(finalPosition).title(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(finalPosition, zoom));
            }
        });

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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    //thread connects to Google GeoCoder Api, gets response code, JSON search results,
    //places data into Log
    Runnable background = new Runnable() {
        public void run(){

            StringBuilder builder = new StringBuilder();

            //place location in query string
            String Url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + location + "&key=AIzaSyCJsvtCg4Nma9eCunBVBTAjZHjD06sKqhQ";  //forward

            InputStream is = null;

            try {
                URL url = new URL(Url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.e("JSON", "The response is: " + response);
                //if response code not 200, end thread
                if (response != 200) return;
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            }	catch(IOException e) {}
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch(IOException e) {}
                }
            }

            //convert StringBuilder to String
            String readJSONFeed = builder.toString();
            Log.e("JSON", readJSONFeed);

            //decode JSON
            try {
                JSONObject obj = new JSONObject(readJSONFeed);

                //gget "results" array
                JSONArray jsonArray = obj.getJSONArray("results");
                Log.i("JSON",
                        "Number of results " + jsonArray.length());

                //get first array element
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Log.i("JSON", jsonObject.toString());

                //get "geometry" object
                JSONObject geometry = jsonObject.getJSONObject("geometry");

                //get "location" object
                JSONObject foo = geometry.getJSONObject("location");
                Log.i("JSON", foo.toString());

                //get latitude and longitude
                String lat = foo.getString("lat");
                String lng = foo.getString("lng");

                Log.i("JSON", lat + "   " +lng);

                //create LatLng object for map
                finalPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));


            } catch (JSONException e) {e.getMessage();
                e.printStackTrace();
            }
        }

    };





}
