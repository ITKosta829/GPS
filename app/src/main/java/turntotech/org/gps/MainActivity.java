package turntotech.org.gps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {
    Button btnGetMyLoc, btnGetMyAdd, btnDistanceToTTT;
    TextView labelLatitude, labelLongitude, showLatitude, showLongitude;
    Tracker gpsTracker;

    protected static final String HTTP = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
    protected static final String API_KEY = "&key=AIzaSyDvSzZs2vIJzot6RrRfPwlBWStLLTrkijY";
    String URL, LAT, LON;

    final double TTT_LAT = 40.741461;
    final double TTT_LON = -73.990036;

    double my_LAT, my_LON;

    public static HashMap<String, String> address_components;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("TurnToTech", "Project Name - GPS");
        btnGetMyLoc = (Button) findViewById(R.id.btn_get_my_location);
        btnGetMyAdd = (Button) findViewById(R.id.btn_get_my_closest_location);
        btnDistanceToTTT = (Button) findViewById(R.id.btn_distance_to_TTT);
        btnGetMyAdd.setEnabled(false);
        btnDistanceToTTT.setEnabled(false);

        labelLatitude = (TextView) findViewById(R.id.TV_my_Lat);
        labelLongitude = (TextView) findViewById(R.id.TV_my_Lon);
        showLatitude = (TextView) findViewById(R.id.TV_Show_My_Lat);
        showLongitude = (TextView) findViewById(R.id.TV_Show_My_Lon);

        // Event
        btnGetMyLoc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gpsTracker = new Tracker(MainActivity.this);

                // check if GPS enabled
                if (gpsTracker.canGetLocation()) {

                    my_LAT = gpsTracker.getLatitude();
                    my_LON = gpsTracker.getLongitude();
                    LAT = Double.toString(my_LAT);
                    LON = Double.toString(my_LON);

                    //Enable all field
                    labelLongitude.setVisibility(View.VISIBLE);
                    labelLatitude.setVisibility(View.VISIBLE);
                    showLatitude.setVisibility(View.VISIBLE);
                    showLongitude.setVisibility(View.VISIBLE);

                    //Set text
                    showLatitude.setText(LAT);
                    showLongitude.setText(LON);

                    btnGetMyAdd.setEnabled(true);
                    btnDistanceToTTT.setEnabled(true);

                    URL = HTTP + LAT + "," + LON + API_KEY;

                    new getClosestAddress().execute(URL);

                    //Toast.makeText(getApplicationContext(), "*....GPS....*", Toast.LENGTH_LONG).show();
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gpsTracker.showSettingsAlert();
                }

            }
        });

        btnGetMyAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayAddress displayAddress = new DisplayAddress();
                displayAddress.show(getFragmentManager(),"display");
            }
        });

        btnDistanceToTTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distanceToTurnToTech();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    public Double calcDistance(Double Start_LAT, Double Start_LON, Double End_LAT, Double End_LON) {

        final int R = 6371; // Radius of the earth
        Double latDistance = toRad(End_LAT - Start_LAT);
        Double lonDistance = toRad(End_LON - Start_LON);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(Start_LAT)) * Math.cos(toRad(End_LAT)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double distance = R * c;

        distance = round(distance, 4);

        return distance;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    public void distanceToTurnToTech() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Distance to Turn To Tech");

        // Setting Dialog Message
        alertDialog.setMessage("Your Distance to TTT is: \n" +
                calcDistance(my_LAT,my_LON,TTT_LAT, TTT_LON).toString() + " km");

        // On pressing Settings button
        alertDialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private class getClosestAddress extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            StringBuilder sb = new StringBuilder();

            HttpURLConnection urlConnection = null;
            try {
                java.net.URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
                urlConnection.connect();

                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(), "utf-8"));
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    in.close();

                    //System.out.println("" + sb.toString());
                    return sb.toString();

                } else {
                    System.out.println(urlConnection.getResponseMessage());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                JSONObject json = new JSONObject(result);
                JSONArray results = json.getJSONArray("results");
                JSONObject components = results.getJSONObject(0);
                JSONArray address = components.getJSONArray("address_components");

                address_components = new HashMap<>();

                for (int i = 0; i < address.length(); i++) {

                    JSONObject values = address.getJSONObject(i);
                    String key = values.getJSONArray("types").get(0).toString();
                    String value = values.getString("long_name");
                    address_components.put(key,value);
                    android.util.Log.d("Dean", key + " " + value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
