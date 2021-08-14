package com.sunshinemine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.sunshinemine.data.WeatherContract;
import com.sunshinemine.sync.SunshineSyncAdapter;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "MainActivity";

    public static final String PREFERENCE_FILE="com.sunshinemine";
    private static final String KEY="CITY";

    private TextView selected_city_textview;
    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME+"."+ WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COULUMN_DATETEXT,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID
    };
    public static final int COL_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_ID = 6;

    private RecyclerView recyclerview_forecast;
    private WeatherAdapter weatherAdapter;
    private TextView Empty_Textview;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = new Bundle();
            bundle.putString(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,intent.getExtras().getString(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC));
            bundle.putString(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,intent.getExtras().getString(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING));
            showDialogtoAlert(bundle);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER,null,this);

        Empty_Textview = findViewById(R.id.Empty_Textview);
        selected_city_textview = findViewById(R.id.selected_city_textview);
        selected_city_textview.setText(MainActivity.getPreference(this));

        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);

        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);
        recyclerview_forecast.setHasFixedSize(true);
        recyclerview_forecast.setLayoutManager(new LinearLayoutManager(this));

        weatherAdapter = new WeatherAdapter(this, new ArrayList<>());
        weatherAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            void checkEmpty() {
                Empty_Textview.setVisibility(weatherAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                updateEmptyView();
            }
        });
        recyclerview_forecast.setAdapter(weatherAdapter);
        recyclerview_forecast.setItemAnimator(new DefaultItemAnimator());

        SunshineSyncAdapter.initializeSyncAdapter(this);

        SunshineSyncAdapter.syncImmediately(this);

        //FC6FXVHBVKT3HABJ

        Bundle bundle= getIntent().getExtras();
        // when app is not running and notification come
        if(bundle!=null){
           showDialogtoAlert(bundle);
        }

        FirebaseMessaging.getInstance ().getToken ()
                .addOnCompleteListener ( task -> {
                    if (!task.isSuccessful ()) {
                        //Could not get FirebaseMessagingToken
                        return;
                    }
                    if (null != task.getResult ()) {
                        //Got FirebaseMessagingToken
                        String firebaseMessagingToken = Objects.requireNonNull ( task.getResult () );
                        //Use firebaseMessagingToken further
                        Log.v("Token Firebase = ",firebaseMessagingToken);
                    }
                } );


    }

    public void showDialogtoAlert(Bundle bundle){
        new AlertDialog.Builder(this)
                .setTitle(" Weather Alert")
                .setMessage("Heads up : " +Utility.convertToCamelCase(bundle.getString(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC))+ " in " + Utility.convertToCamelCase(bundle.getString(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING)) + " ..! ")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }})
                .show();
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
                SunshineSyncAdapter.syncImmediately(this);
                return true;
            }
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_map:
                openPreferredLocationOnMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void openPreferredLocationOnMap(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location = preferences.getString(this.getString(R.string.pref_city_key),this.getString(R.string.pref_city_default));

        Uri gmmIntentUri = Uri.parse("geo:"+location.split("/")[0].split(",")[0] + "," + location.split("/")[0].split(",")[1]);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String location_city = preferences.getString(getString(R.string.pref_city_key),getString(R.string.pref_city_default));
        MainActivity.setPreference(this,location_city.split("/")[1]);

        // Karachi, Pakistan time is 10:00 hours ahead Chicago, United States
        long ten_Hours_in_miliseconds = 36000000;
        String startDate = String.valueOf(new Date(Calendar.getInstance().getTimeInMillis() - ten_Hours_in_miliseconds).getTime());
        Log.v("Mera",startDate);

        String sortOrder = WeatherContract.WeatherEntry.COULUMN_DATETEXT + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                MainActivity.getPreference(this), startDate);

        CursorLoader cursorLoader =  new CursorLoader(this,
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.v("Mera",data.getCount()+"");
        if(data!=null && data.moveToFirst()) {
            Log.v("Mera",data.getCount()+"");
            ArrayList<WeatherForecast> arrayList =new ArrayList<>();
            do {
                Log.v("Hola", data.getInt(COL_WEATHER_ID) + " - " + data.getString(COL_WEATHER_DATE) + " - " + data.getString(COL_WEATHER_DESC) + " - " +
                        data.getString(COL_WEATHER_MAX_TEMP) + " - " + data.getString(COL_WEATHER_MIN_TEMP) + " - " + data.getString(COL_LOCATION_SETTING));

                WeatherForecast weatherForecast = new WeatherForecast();
                weatherForecast.setDt(Long.parseLong(data.getString(COL_WEATHER_DATE)));

                ArrayList<WeatherForecast.weather> weathers = new ArrayList<>();
                WeatherForecast.weather weather = new WeatherForecast.weather();
                weather.setMain(data.getString(COL_WEATHER_DESC));
                weather.setId(data.getInt(COL_WEATHER_ID));
                weathers.add(weather);
                weatherForecast.setWeatherArrayList(weathers);

                WeatherForecast.Temp temp = new WeatherForecast.Temp();
                temp.setMax(Double.parseDouble(data.getString(COL_WEATHER_MAX_TEMP)));
                temp.setMin(Double.parseDouble(data.getString(COL_WEATHER_MIN_TEMP)));
                weatherForecast.setTemp(temp);

                arrayList.add(weatherForecast);
            }
            while (data.moveToNext());
            weatherAdapter.swapWeather(arrayList);
        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        weatherAdapter.swapWeather(null);
    }

    public static boolean setPreference(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY, value);
        return editor.commit();
    }

    public static String getPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        return settings.getString(KEY, "MANDIBAHAUDDIN");
    }

    private void updateEmptyView(){
        if(weatherAdapter.getItemCount() == 0 ){
            if(null != Empty_Textview){
                int message = R.string.empty_forecast_list;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(this);
                switch (location){
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    default:
                        if(!Utility.isNetworkAvailable(this)){
                            message = R.string.empty_forecast_list_no_network;
                        };
                }
                Empty_Textview.setText(message);
            }
        }
    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_status_key))){
            updateEmptyView();
        }
    }
}