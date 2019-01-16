package com.example.bruno.cambus;

import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    RequestQueue requestQueue; // This is our requests queue to process our HTTP requests.
    Double lat = 0.0;
    Double longi = 0.0;
    String nomeOnibus = null;
    String linhaOnibus = null;
    Integer count_down = 0;
    Integer count_up = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void iterateOverJsonArray(JSONArray ja) {

        for (int i = 0; i < ja.length(); i++) {
            try {
                if (ja.getJSONObject(i) instanceof JSONObject) {
                    try {
                        iterateOverJsonItems(ja.getJSONObject(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Testte" + ja);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void iterateOverJsonItems( JSONObject jo ) {
        Iterator<String> keys = jo.keys();
        Object value = null;
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            value = null;
            try {
                value = jo.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.print(key + ": ");
            if ( value instanceof JSONArray ) {
                System.out.print("[");
                if ( ((JSONArray) value).length() > 0 ) {
                    System.out.print("\n");
                    iterateOverJsonArray( (JSONArray)value );
                    System.out.println("]");
                }
                else {
                    System.out.print("]\n");
                }
            }
            else if ( value instanceof JSONObject ) {
                System.out.println("{");
                iterateOverJsonItems( (JSONObject)value );
                System.out.println("}");
            }
            else {
                System.out.println(value);
                if(key.equals("gps"))
                {
                    lat = -23.608692899;
                    longi = -46.695984782;
                }

                if(key.equals("lat"))
                {
                    String lat1 = value.toString();

                    if(lat1.startsWith("-"))
                        lat = Integer.valueOf(lat1.substring(1)).doubleValue();
                }

                if(key.equals("lon"))
                {
                    String longi1 = value.toString();

                    if(longi1.startsWith("-"))
                        longi = Integer.valueOf(longi1.substring(1)).doubleValue();
                }

                if(key.equals("Name"))
                {
                    nomeOnibus = value.toString();
                }

                if(key.equals("Line"))
                {
                    linhaOnibus = value.toString();
                }

                if(key.equals("count_up"))
                {
                    count_up = Integer.parseInt(value.toString());
                }

                if(key.equals("count_down"))
                {
                    count_down = Integer.parseInt(value.toString());
                }
            }
        }
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

        HttpURLConnection urlConnection = null;
        try {
            URL url        = new URL("https://dweet.io:443/get/latest/dweet/for/Dobrowok");
            urlConnection  = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            String input = result.toString();
            JSONObject jo  = new JSONObject(input);
            JSONArray bugs = jo.getJSONArray("with");
            iterateOverJsonArray(bugs);
        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

        // Add a marker in Sydney and move the camera
        System.out.println(longi);
        System.out.println(lat);
        LatLng sydney = new LatLng(lat, longi);
        int passageiros = count_up.intValue() - count_down.intValue();
        mMap.addMarker(new MarkerOptions().position(sydney).title(nomeOnibus + " " + linhaOnibus + " contem: " + passageiros + " passageiros." ));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
