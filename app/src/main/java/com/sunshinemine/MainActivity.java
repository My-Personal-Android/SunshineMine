package com.sunshinemine;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements HttpCallBack{

    private static final String LOG_TAG = "MainActivity";

    String url = "https://api.openweathermap.org/data/2.5/onecall?";
    String lattitude = "lat=32.5742";
    String longitude = "lon=73.4828";
    String unit = "units=metric";
    String appid= "appid=8c7175339095430fdf24c1cac276d9d5";

    String my_url = url + lattitude + "&" +longitude + "&" + unit + "&" + appid;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    private ArrayList<String> arrayList_WeatherForecast;
    private WeatherForecast weatherForecast = null;

    private RecyclerView recyclerview_forecast;
    private WeatherAdapter weatherAdapter;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);


        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);
        recyclerview_forecast.setHasFixedSize(true);
        recyclerview_forecast.setLayoutManager(new LinearLayoutManager(this));

        weatherAdapter = new WeatherAdapter(this, new ArrayList<>());
        recyclerview_forecast.setAdapter(weatherAdapter);
        recyclerview_forecast.setItemAnimator(new DefaultItemAnimator());

        String data = WeatherForecast.getPreference(this);
        WeatherForecast.v(LOG_TAG,data);

        try {
            weatherAdapter.swapWeather(WeatherForecast.getWeatherDataFromJson(WeatherForecast.getPreference(this)));

            ArrayList<WeatherForecast> weatherForecastArrayList = WeatherForecast.getWeatherDataFromJson(data);
            Log.v(LOG_TAG,WeatherForecast.getReadableDateString(weatherForecastArrayList.get(7).getDt())+" - "+weatherForecastArrayList.get(7).getWeatherArrayList().get(0).getMain() + " - " + WeatherForecast.formatHightLows(weatherForecastArrayList.get(7).getTemp().getMax(),weatherForecastArrayList.get(7).getTemp().getMin()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        weatherForecast = new WeatherForecast(this,executorService,mainThreadHandler);
//        try {
//            weatherForecast.makeRequest(this,my_url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecastmenu, menu);
        return true;
    }
    //Android Activity Lifecycle Method
   // Called when a panel's menu is opened by the user.
    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        MenuItem mnuLogOut = menu.findItem(R.id.action_refresh);

        //set the menu options depending on login status

        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {

            case R.id.action_refresh:
            {
                weatherForecast = new WeatherForecast(this,executorService,mainThreadHandler);
                try {
                    weatherForecast.makeRequest(this,my_url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onPreExecute(String result, String caller) {

        Log.v(LOG_TAG,result + ' ' + caller);
    }

    @Override
    public void onPostExecute(String result, String caller) {

      //  Log.v(LOG_TAG,result + ' ' + caller);
        WeatherForecast.v(LOG_TAG,result);
        WeatherForecast.setPreference(this,result);

        try {
            weatherAdapter.swapWeather(WeatherForecast.getWeatherDataFromJson(WeatherForecast.getPreference(this)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}