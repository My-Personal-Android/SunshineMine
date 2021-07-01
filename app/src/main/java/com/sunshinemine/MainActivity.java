package com.sunshinemine;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements HttpCallBack{

    private static final String LOG_TAG = "MainActivity";

    String url = "https://api.openweathermap.org/data/2.5/onecall?";
    String lattitude = "lat=32.4404";
    String longitude = "lon=74.1203";
    String unit = "units=metric";
    String appid= "appid=8c7175339095430fdf24c1cac276d9d5";

    String my_url = url + lattitude + "&" +longitude + "&" + unit + "&" + appid;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    private ListView listView_Forecast;
    private ArrayList<String> arrayList_WeatherForecast;
    private WeatherForecast weatherForecast = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView_Forecast = findViewById(R.id.listview_forecast);
        arrayList_WeatherForecast = new ArrayList();
        arrayList_WeatherForecast.add("TODAY - Sunnay - 81 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 82 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 83 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 84 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 85 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 86 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 87 / 63");
        arrayList_WeatherForecast.add("TODAY - Sunnay - 88 / 63");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.list_item_forcast,R.id.list_item_forcast_textview,arrayList_WeatherForecast);
        listView_Forecast.setAdapter(arrayAdapter);

        String data = WeatherForecast.getPreference(this);
        WeatherForecast.v(LOG_TAG,data);
//        weatherForecast = new WeatherForecast(this,executorService,mainThreadHandler);
//        try {
//            weatherForecast.makeRequest(this,my_url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onPreExecute(String result, String caller) {

        Log.v(LOG_TAG,result + ' ' + caller);
    }

    @Override
    public void onPostExecute(String result, String caller) {
        Log.v(LOG_TAG,result + ' ' + caller);
        WeatherForecast.setPreference(this,result);
    }
}