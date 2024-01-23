package com.example.weathermap;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client = null;
    private EditText searchEditText;
    double longitude;
    double latitude;
    private LocationCallback mLocationCallback;

    private void updateWeatherByLocation() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=YOURKEY";
        new GetWeatherAsyncTask().execute(apiUrl);
    }
    private boolean locationUpdateRequested = true;

    private void handleLocationResult(LocationResult locationResult) {
        Log.d("LOCATION", "calling callback");
        if (locationResult == null) {
            Log.d("LOCATION", "locationResult is null");
            return;
        }
        for (Location location : locationResult.getLocations()) {
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                updateWeatherByLocation();

                locationUpdateRequested = false;
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();
        searchEditText = findViewById(R.id.searchbar);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                handleLocationResult(locationResult);

                if (!locationUpdateRequested) {
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .removeLocationUpdates(mLocationCallback);
                }
            }
        };

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());

        if (locationUpdateRequested) {
            new GetWeatherAsyncTask().execute();
        }
        Button searchButton = findViewById(R.id.button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString();
                if (!searchQuery.isEmpty()) {
                    String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + searchQuery + "&appid=YOURKEY";
                    new GetWeatherAsyncTask().execute(apiUrl);
                }
            }
        });

        new GetWeatherAsyncTask().execute();

        Button currentLocationButton = findViewById(R.id.currentLocation);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationUpdateRequested = true;
                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                        .requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
                new GetWeatherAsyncTask().execute();
            }
        });
    }
    String str;
    private class GetWeatherAsyncTask extends AsyncTask<String, Void, WeatherApp> {
        @Override
        protected WeatherApp doInBackground(String... urls) {
            WeatherApp obj = null;
            String url = urls.length > 0 ? urls[0] : "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=YOURKEY";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseStr = response.body().string();
                obj = new Gson().fromJson(responseStr, WeatherApp.class);
            } catch (IOException e) {
                Log.e("JSONDEMO", String.valueOf(e));
            }

            return obj;
        }

        @Override
        protected void onPostExecute(WeatherApp result) {
            if (result != null) {
                TextView cityTextView = findViewById(R.id.cityTextView);
                TextView tempTextView = findViewById(R.id.tempTextView);
                TextView pressureTextView = findViewById(R.id.pressureTextView);
                TextView humidityTextView = findViewById(R.id.humidityTextView);
                TextView descriptionTextView = findViewById(R.id.descriptionTextView);
                ImageView weatherIconImageButton = findViewById(R.id.weatherIconImageButton);

                cityTextView.setText(result.getName());
                double temperatureInCelsius = result.getMain().getTemp() - 273.15;
                tempTextView.setText(String.format("Temperature: %.1f°C", temperatureInCelsius));
                pressureTextView.setText("Pressure: " + result.getMain().getPressure() + " hPa");
                humidityTextView.setText("Humidity: " + result.getMain().getHumidity() + "%");

                if (result.getWeatherList() != null && result.getWeatherList().size() > 0) {
                    Weather weather = result.getWeatherList().get(0);
                    descriptionTextView.setText("Weather Description: " + weather.getDescription());

                    String iconUrl = "https://openweathermap.org/img/wn/" + weather.getIcon() + ".png";
                    Picasso.get().load(iconUrl).resize(300, 300).into(weatherIconImageButton);
                }
            }
        }
    }
}
