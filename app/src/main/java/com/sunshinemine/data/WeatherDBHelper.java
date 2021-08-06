package com.sunshinemine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class WeatherDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME= "weather.db";

    public WeatherDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_LOCATION_TABLE =
                "CREATE TABLE " + WeatherContract.LocationEntry.TABLE_NAME + " ( "+
                        WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY, " +
                        WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING+ " TEXT UNIQUE NOT NULL, "+
                        WeatherContract.LocationEntry.COULUMN_CITY_NAME+ " TEXT NOT NULL, "+
                        WeatherContract.LocationEntry.COULUMN_COORD_LAT+ " REAL NOT NULL, "+
                        WeatherContract.LocationEntry.COULUMN_COORD_LONG+ " REAL NOT NULL "+
                        " );";

        final String SQL_CREATE_WEATHER_TABLE =
                "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_NAME + " ( "+
                        WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY + " INTEGER NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_DATETEXT + " TEXT NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_SHORT_DESC + " TEXT NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_WEATHER_ID + " INTEGER NOT NULL, "+

                        WeatherContract.WeatherEntry.COULUMN_MIN_TEMP + " REAL NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_MAX_TEMP+ " REAL NOT NULL, "+

                        WeatherContract.WeatherEntry.COULUMN_HUMIDITY + " REAL NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_PRESSURE + " REAL NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_WIND_SPEED + " REAL NOT NULL, "+
                        WeatherContract.WeatherEntry.COULUMN_DEGREES + " REAL NOT NULL, "+

                        " FOREIGN KEY (" + WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY +") REFERENCES "+
                        WeatherContract.LocationEntry.TABLE_NAME + " ("+ WeatherContract.LocationEntry._ID+"), "+

                        " UNIQUE ("+ WeatherContract.WeatherEntry.COULUMN_DATETEXT+", "+
                        WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY+") ON CONFLICT REPLACE"+
                        " );";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_WEATHER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ WeatherContract.LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
