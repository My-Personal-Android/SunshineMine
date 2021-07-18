package com.sunshinemine.data;

import android.provider.BaseColumns;

public class WeatherContract {

    public static final class WeatherEntry implements BaseColumns {
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
    }

    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COULUMN_LOCATION_SETTING = "location_setting";
        public static final String COULUMN_CITY_NAME = "city_name";
        public static final String COULUMN_COORD_LAT = "coord_lat";
        public static final String COULUMN_COORD_LONG = "coord_long";
    }
}
