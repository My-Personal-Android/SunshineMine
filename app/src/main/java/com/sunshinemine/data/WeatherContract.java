package com.sunshinemine.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

import androidx.core.net.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.Settings.System.DATE_FORMAT;

public class WeatherContract {

    public static final String CONTENT_AUTHORITY = "com.sunshinemine"; // similar to as a domain name of website to differentiate between different apps

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY); // base content uri

    public static final String PATH_WEATHER = "weather";

    public static final String PATH_LOCATION = "location";


    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }


    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";
        public static final String COULUMN_LOCATION_KEY = "location_id";
        public static final String COULUMN_DATETEXT = "date";
        public static final String COULUMN_WEATHER_ID = "weather_id";
        public static final String COULUMN_SHORT_DESC = "short_desc";
        public static final String COULUMN_MAX_TEMP= "max";
        public static final String COULUMN_MIN_TEMP= "min";
        public static final String COULUMN_HUMIDITY = "humidity";
        public static final String COULUMN_PRESSURE = "pressure";
        public static final String COULUMN_WIND_SPEED = "wind";
        public static final String COULUMN_DEGREES = "degrees";

        public static String getDbDateString(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            return sdf.format(date);
        }

        public static Date getDateFromDb(String dateText) {
            SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                return dbDateFormat.parse(dateText);
            } catch (ParseException | java.text.ParseException e ) {
                e.printStackTrace();
                return null;
            }
        }

        //
        public static Uri buildWeatherUri (long id){
             return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        public static Uri buildWeatherLocation (String locationSetting){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate (String locationSetting,String startDate){
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COULUMN_DATETEXT,startDate).build();
        }

        public static Uri buildWeatherLocationWithDate (String locationSetting,String date){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        public static String getLocationSettingFromUri (Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri (Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri (Uri uri){
            return uri.getQueryParameter(COULUMN_DATETEXT);
        }
    }

    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";

        public static final String COULUMN_LOCATION_SETTING = "location_setting";
        public static final String COULUMN_CITY_NAME = "city_name";
        public static final String COULUMN_COORD_LAT = "coord_lat";
        public static final String COULUMN_COORD_LONG = "coord_long";

        public static Uri buildLocationUri (long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }
}
