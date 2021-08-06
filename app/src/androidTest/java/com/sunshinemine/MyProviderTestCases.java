//package com.sunshinemine;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.net.Uri;
//import android.test.ProviderTestCase2;
//import android.util.Log;
//
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.sunshinemine.data.WeatherContract;
//import com.sunshinemine.data.WeatherDBHelper;
//import com.sunshinemine.data.WeatherProvider;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.Map;
//import java.util.Set;
//
//@RunWith(AndroidJUnit4.class)
//public class MyProviderTestCases extends ProviderTestCase2<WeatherProvider> {
//
//    private Context mContext = ApplicationProvider.getApplicationContext();
//
//
//    public MyProviderTestCases() {
//        super(WeatherProvider.class, WeatherContract.CONTENT_AUTHORITY);
//    }
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        setContext(ApplicationProvider.getApplicationContext());
//    }
//
//    @Test
//    public void testInsertReadDb() {
//
//        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
//
//        WeatherDBHelper weatherDBHelper = new WeatherDBHelper(mContext,WeatherDBHelper.DATABASE_NAME,null,0);
//
//        // Context of the app under test.
//        SQLiteDatabase db = weatherDBHelper.getWritableDatabase();
//
//        assertEquals(true, db.isOpen());
//
//        ContentValues location_contentValues = getLocationValues();
//
//        long row_ID ;
//
//        Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, location_contentValues);
//
//        Log.v("URI = ",uri.toString());
//        assertNotNull(uri);
//
//        row_ID = Long.valueOf(uri.getLastPathSegment());
//
//        Log.v("URI = ",WeatherContract.LocationEntry.buildLocationUri(row_ID).toString());
//
//        Cursor cursor = mContext.getContentResolver().query(
//                WeatherContract.LocationEntry.buildLocationUri(row_ID),
//                null,
//                null,
//                null,
//                null
//        );
//
//        if(cursor.moveToFirst()){
//
//            ValidateCursor(location_contentValues,cursor);
//
//            ContentValues weatherValues = getWeatherValues(0);
//
//            long weather_ID ;
//            weather_ID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,weatherValues);
//
//            assertTrue(weather_ID!=-1);
//
//            Cursor Weather_cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
//                null,
//                null,
//                null,
//                null);
//
//            if(Weather_cursor==null){
//                fail("Weather Cursor is null");
//            }
//            if(Weather_cursor.moveToFirst()){ // this works only when cursor is initialised
//
//                ValidateCursor(weatherValues,Weather_cursor);
//
//            }else{
//                fail("No Weather values returned");
//            }
//
//        }else{
//            fail("No Location values returned");
//        }
//
//        db.close();
//    }
//
//
//    static public ContentValues getWeatherValues(long row_ID){
//        ContentValues weatherValues = new ContentValues();
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_LOCATION_KEY,row_ID);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DATETEXT,"201294");
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_DEGREES,1.1);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_HUMIDITY,1.2);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_PRESSURE,1.3);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,74.3);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,65.4);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,"good ");
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_WIND_SPEED,3.4);
//        weatherValues.put(WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,123);
//        return weatherValues;
//    }
//    static public ContentValues getLocationValues(){
//
//        String test_Name= " North Pole ";
//        String test_LocationSetting = "99056";
//        double test_Lattitude = 34.45;
//        double test_Longitude = 45.67;
//
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(WeatherContract.LocationEntry.COULUMN_CITY_NAME,test_Name);
//        contentValues.put(WeatherContract.LocationEntry.COULUMN_LOCATION_SETTING,test_LocationSetting);
//        contentValues.put(WeatherContract.LocationEntry.COULUMN_COORD_LAT,test_Lattitude);
//        contentValues.put(WeatherContract.LocationEntry.COULUMN_COORD_LONG,test_Longitude);
//
//        return contentValues;
//    }
//
//    // to check and valiadate data in table of database of anytype/design/columns at index
//    static public void ValidateCursor (ContentValues contentValues , Cursor cursor ){
//
//        Set<Map.Entry<String,Object>> valueSet = contentValues.valueSet();
//
//        for(Map.Entry<String,Object> entry : valueSet){
//            String columnName = entry.getKey();
//            int index= cursor.getColumnIndex(columnName);
//            assertFalse(-1==index);
//            String value = entry.getValue().toString();
//            assertEquals(value,cursor.getString(index));
//        }
//    }
//
//}
