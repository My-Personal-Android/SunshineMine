package com.sunshinemine;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sunshinemine.data.WeatherContract;
import com.sunshinemine.data.WeatherDBHelper;

import junit.framework.TestSuite;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FullTestSuite extends TestSuite {

    private static final String LOG_TAG = "FullTestSuite";

    private Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void useAppContext() {
        // Context of the app under test.
        assertEquals("com.sunshinemine", mContext.getPackageName());
    }

    @Test
    public void testContentProvider() {

        String type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.CONTENT_URI);
        assertEquals(WeatherContract.WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherContract.WeatherEntry.buildWeatherLocationWithDate("location","date"));
        assertEquals(WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE, type);
    }

    @Test
    public void testCreatDb() {
        // Context of the app under test.
        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDBHelper(mContext,WeatherDBHelper.DATABASE_NAME,null,0).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    @Test
    public void testInsertReadDb() {


        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);

        WeatherDBHelper weatherDBHelper = new WeatherDBHelper(mContext,WeatherDBHelper.DATABASE_NAME,null,0);
        // Context of the app under test.
        SQLiteDatabase db = weatherDBHelper.getWritableDatabase();

        assertEquals(true, db.isOpen());

        ContentValues location_contentValues = getLocationValues();

        long row_ID ;
        row_ID = db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,location_contentValues);

        assertTrue(row_ID!=-1);
        Log.v(LOG_TAG,"NEW ROW ID -> "+row_ID);

//        Cursor cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.buildLocationUri(row_ID),
//        null,
//        null,
//        null,
//        null);

        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
               null
        );

        if(cursor==null){
            fail("Location Cursor is null");
        }
        if(cursor.moveToFirst()){

            ValidateCursor(location_contentValues,cursor);

            ContentValues weatherValues = getWeatherValues(row_ID);

            long weather_ID ;
            weather_ID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,weatherValues);

            assertTrue(weather_ID!=-1);
            Log.v(LOG_TAG,"NEW ROW WEATHER ID -> "+weather_ID);
//
//            Cursor Weather_cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null);

            Cursor Weather_cursor = db.query(
                    WeatherContract.WeatherEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if(Weather_cursor==null){
                fail("Weather Cursor is null");
            }
            if(Weather_cursor.moveToFirst()){ // this works only when cursor is initialised

                ValidateCursor(weatherValues,Weather_cursor);

            }else{
                fail("No Weather values returned");
            }

        }else{
            fail("No Location values returned");
        }

        db.close();
    }


    static public ContentValues getWeatherValues(long row_ID){
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY,row_ID);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DATETEXT,"201294");
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DEGREES,1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_HUMIDITY,1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_PRESSURE,1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,74.3);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,65.4);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,"good ");
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_WIND_SPEED,3.4);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,123);
        return weatherValues;
    }
    static public ContentValues getLocationValues(){

        String test_Name= " North Pole ";
        String test_LocationSetting = "99056";
        double test_Lattitude = 34.45;
        double test_Longitude = 45.67;


        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherContract.LocationEntry.COULUMN_CITY_NAME,test_Name);
        contentValues.put(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,test_LocationSetting);
        contentValues.put(WeatherContract.LocationEntry.COULUMN_COORD_LAT,test_Lattitude);
        contentValues.put(WeatherContract.LocationEntry.COULUMN_COORD_LONG,test_Longitude);

        return contentValues;
    }

    // to check and valiadate data in table of database of anytype/design/columns at index
    static public void ValidateCursor (ContentValues contentValues , Cursor cursor ){

        Set<Map.Entry<String,Object>> valueSet = contentValues.valueSet();

        for(Map.Entry<String,Object> entry : valueSet){
            String columnName = entry.getKey();
            int index= cursor.getColumnIndex(columnName);
            assertFalse(-1==index);
            String value = entry.getValue().toString();
            assertEquals(value,cursor.getString(index));
        }
    }
}