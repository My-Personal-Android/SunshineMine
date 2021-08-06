package com.sunshinemine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sunshinemine.data.WeatherContract;
import com.sunshinemine.data.WeatherDBHelper;

import junit.framework.TestSuite;

import org.junit.Test;
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
    public void testInsertReadContentProvider() {


        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);

        WeatherDBHelper weatherDBHelper = new WeatherDBHelper(mContext,WeatherDBHelper.DATABASE_NAME,null,0);

        // Context of the app under test.
        SQLiteDatabase db = weatherDBHelper.getWritableDatabase();

        assertEquals(true, db.isOpen());

        ContentValues location_contentValues = getLocationValues();

        long row_ID ;

        Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, location_contentValues);

        Log.v("URI = ",uri.toString());
        assertNotNull(uri);

        row_ID = Long.valueOf(uri.getLastPathSegment());

        Log.v("URI = ",WeatherContract.LocationEntry.buildLocationUri(row_ID).toString());

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.buildLocationUri(row_ID),
                null,
                null,
                null,
                null
        );

        if(cursor.moveToFirst()){

            ValidateCursor(location_contentValues,cursor);

            ContentValues weatherValues = getWeatherValues(row_ID);

            Uri uri_1 = mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI,weatherValues);
            long weather_ID = ContentUris.parseId(uri_1);

            assertTrue(weather_ID!=-1);

            Cursor Weather_cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            if(Weather_cursor==null){
                fail("Weather Cursor is null");
            }
            if(Weather_cursor.moveToFirst()){ // this works only when cursor is initialised

                ValidateCursor(weatherValues,Weather_cursor);

            }else{
                fail("No Weather values returned");
            }

            Weather_cursor.close();

            Log.v("Hello", WeatherContract.WeatherEntry.buildWeatherLocation(TEST_LOCATION)+"");

             Weather_cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION,TEST_DATE),
                    null,
                    null,
                    null,
                    null);

            if(Weather_cursor==null){
                fail("Weather Cursor is null");
            }
            if(Weather_cursor.moveToFirst()){ // this works only when cursor is initialised

                ValidateCursor(weatherValues,Weather_cursor);

            }else{
                fail("No Weather values returned");
            }

            Weather_cursor.close();


            Weather_cursor = mContext.getContentResolver().query(
                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION,TEST_DATE),
                    null,
                    null,
                    null,
                    null);

            if(Weather_cursor==null){
                fail("Weather Cursor is null");
            }
            if(Weather_cursor.moveToFirst()){ // this works only when cursor is initialised

                ValidateCursor(weatherValues,Weather_cursor);

            }else{
                fail("No Weather values returned");
            }

            Weather_cursor.close();
        }else{
            fail("No Location values returned");
        }

        db.close();
        //deleteDatabaseTest();
       // testUpdateLocation();
    }


    public void deleteDatabaseTest() {
        mContext.getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,null,null);
        mContext.getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,null,null);

        Cursor cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,null,null,null,null);
        assertEquals(cursor.getCount(),0);
        cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,null,null,null,null);
        assertEquals(cursor.getCount(),0);
        cursor.close();

    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update location is functioning correctly.
     */

    
    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = getLocationValues();
        Uri locationUri = mContext.getContentResolver().
                insert(WeatherContract.LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(WeatherContract.LocationEntry._ID, locationRowId);
        updatedValues.put(WeatherContract.LocationEntry.COULUMN_CITY_NAME, "Santa's Village");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, null, null, null, null);

        int count = mContext.getContentResolver().update(
                WeatherContract.LocationEntry.CONTENT_URI, updatedValues, WeatherContract.LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(count, 1);

        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                null,   // projection
                WeatherContract.LocationEntry._ID + " = " + locationRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        Log.v("Hello",cursor.moveToFirst()+"");

        cursor.close();
    }


    static public String TEST_CITY_NAME= "North Pole";
    static public String TEST_LOCATION ="99705";
    static public String TEST_DATE = "20210801";

    static public ContentValues getWeatherValues(long row_ID){
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY,row_ID);
        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DATETEXT,TEST_DATE);
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

        String test_Name= TEST_CITY_NAME;
        String test_LocationSetting = TEST_LOCATION;
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