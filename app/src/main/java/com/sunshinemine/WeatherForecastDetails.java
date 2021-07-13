package com.sunshinemine;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class WeatherForecastDetails extends AppCompatActivity {

    private TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        test = findViewById(R.id.test);

        Intent intent = getIntent();
        WeatherForecast weatherForecast = intent.getParcelableExtra("Data");
        Log.v("Helo",weatherForecast.toString());
        test.setText(WeatherForecast.getReadableDateString(weatherForecast.getDt())+" - "+weatherForecast.getWeatherArrayList().get(0).getMain() + " - " + weatherForecast.formatHightLows(this,weatherForecast.getTemp().getMax(),weatherForecast.getTemp().getMin()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);
        return true;
    }

    // function to the button on press
    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}