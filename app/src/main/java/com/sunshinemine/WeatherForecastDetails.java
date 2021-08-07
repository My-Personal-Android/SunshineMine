package com.sunshinemine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.sunshinemine.data.WeatherContract;

public class WeatherForecastDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    public static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COULUMN_DATETEXT,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COULUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COULUMN_PRESSURE,
            WeatherContract.WeatherEntry.COULUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COULUMN_DEGREES,
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private  String intent_data = null;

    public TextView date_textview;
    public TextView forecast_textview;
    public TextView high_textview;
    public TextView low_textview;

    private String mLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast_details);

         LoaderManager.getInstance(this).initLoader(DETAIL_LOADER,null,this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        WeatherForecast weatherForecast = intent.getParcelableExtra("Data");
        Log.v("Helo",weatherForecast.toString());

        date_textview =findViewById(R.id.date_textview);
        forecast_textview =findViewById(R.id.forecast_textview);
        high_textview =findViewById(R.id.high_textview);
        low_textview =findViewById(R.id.low_textview);

//        date_textview.setText(WeatherForecast.getReadableDateString(weatherForecast.getDt()) + " - ");
//        forecast_textview.setText(weatherForecast.getWeatherArrayList().get(0).getMain()+" - ");
//        high_textview.setText(weatherForecast.formatHightLows(this,weatherForecast.getTemp().getMax(),weatherForecast.getTemp().getMin()).split("/")[0]+" / ");
//        low_textview.setText(weatherForecast.formatHightLows(this,weatherForecast.getTemp().getMax(),weatherForecast.getTemp().getMin()).split("/")[1]);

    }

    private Intent creatShareForecastIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,intent_data + " # SUNSHINE MINE");
        return intent;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(shareActionProvider!=null){
            shareActionProvider.setShareIntent(creatShareForecastIntent());
        }else{
            Log.v("Looo","Share Action Provide is null");
        }

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


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Intent intent = getIntent();
        WeatherForecast weatherForecast = intent.getParcelableExtra("Data");
        mLocation = "0546";
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation,String.valueOf(weatherForecast.getDt()));

        return new CursorLoader(
                this,
                weatherUri,
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if(data!=null && data.moveToFirst()){
            Log.v("Hooo",data.getString(0)+" - "+
                    data.getString(1)+" - "+
                    data.getString(2)+" - "+
                    data.getString(3)+" - "+
                    data.getString(4)+" - "+
                    data.getString(5)+" - "+
                    data.getString(6)+" - "+
                    data.getString(7)+" - "+
                    data.getString(8)+" - "+
                    data.getString(9)+" - "+
                    data.getString(10)
                    );
            date_textview.setText(WeatherForecast.getReadableDateString(Long.parseLong(data.getString(COL_WEATHER_DATE))) + " - ");
            forecast_textview.setText(data.getString(COL_WEATHER_DESC)+" - ");
            high_textview.setText(WeatherForecast.formatHightLows(this,data.getDouble(COL_WEATHER_MAX_TEMP),data.getDouble(COL_WEATHER_MIN_TEMP)).split("/")[0]+"\u00B0 / ");
            low_textview.setText(WeatherForecast.formatHightLows(this,data.getDouble(COL_WEATHER_MAX_TEMP),data.getDouble(COL_WEATHER_MIN_TEMP)).split("/")[1]+"\u00B0");

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}