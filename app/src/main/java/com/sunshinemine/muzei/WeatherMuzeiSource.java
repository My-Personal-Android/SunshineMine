package com.sunshinemine.muzei;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.sunshinemine.MainActivity;
import com.sunshinemine.Utility;
import com.sunshinemine.data.WeatherContract;
import com.sunshinemine.sync.SunshineSyncAdapter;

public class WeatherMuzeiSource extends MuzeiArtSource {
    private static final String[] FORECAST_COLUMNS = new String[]{
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;

    public WeatherMuzeiSource() {
        super("WeatherMuzeiSource");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        boolean dataUpdated = intent != null &&
                SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction());
        Log.v("Kaloooo","update handle");
        Log.v("Kaloooo",""+dataUpdated + " - "+ isEnabled());
        if (dataUpdated && isEnabled()) {
            Log.v("Kaloooo","updating");
            onUpdate(UPDATE_REASON_OTHER);
        }
    }

    @Override
    protected void onUpdate(int reason) {
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location,String.valueOf(System.currentTimeMillis()));
        Cursor cursor = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COULUMN_DATETEXT + " ASC");
        if (cursor.moveToFirst()) {
            Log.v("Kaloooo","move to first");
            int weatherId = cursor.getInt(INDEX_WEATHER_ID);
            String desc = cursor.getString(INDEX_SHORT_DESC);

            String imageUrl = Utility.getImageUrlForWeatherCondition(weatherId);
            // Only publish a new wallpaper if we have a valid image
            if (imageUrl != null) {
                Log.v("Kaloooo","got image uir");
                publishArtwork(new Artwork.Builder()
                        .imageUri(Uri.parse(imageUrl))
                        .title(desc)
                        .byline(location)
                        .viewIntent(new Intent(this, MainActivity.class))
                        .build());
            }
        }
        cursor.close();
    }
}