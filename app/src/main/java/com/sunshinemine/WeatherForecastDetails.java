package com.sunshinemine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class WeatherForecastDetails extends AppCompatActivity {

    private TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast_details);

        test = findViewById(R.id.test);

        Intent intent = getIntent();
        WeatherForecast weatherForecast = intent.getParcelableExtra("Data");
        Log.v("Helo",weatherForecast.toString());
        test.setText(WeatherForecast.getReadableDateString(weatherForecast.getDt())+" - "+weatherForecast.getWeatherArrayList().get(0).getMain() + " - " + WeatherForecast.formatHightLows(weatherForecast.getTemp().getMax(),weatherForecast.getTemp().getMin()));
    }
}