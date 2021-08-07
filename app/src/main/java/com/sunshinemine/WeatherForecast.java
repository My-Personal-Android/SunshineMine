package com.sunshinemine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.concurrent.Executor;

public class WeatherForecast implements Parcelable {


    private Context mContext;
    private Executor executor;
    private Handler resultHandler;
    private HttpCallBack httpCallBack;

    private static final String LOG_TAG = "WeatherForecast";

    private static final String PREFERENCE_FILE_WEATHERFORECAST = "com.sunshine";
    private static final String KEY_WEATHERFORECAST = "WeatherForecast";

    private long dt;
    private long sunrise;
    private long sunset;
    private long moonrise;
    private long moonset;
    private Temp temp;
    private Feels_Like feels_like;
    private long pressure;
    private long humidity;
    private double wind_speed;
    private  long wind_deg;
    private double wind_gust;
    private List<weather> weatherArrayList = new ArrayList<>();
    private long clouds;
    private double pop;
    private double uvi;

    public void setWeatherArrayList(List<weather> weatherArrayList) {
        this.weatherArrayList = weatherArrayList;
    }

    protected WeatherForecast(Parcel in) {
        dt = in.readLong();
        sunrise = in.readLong();
        sunset = in.readLong();
        moonrise = in.readLong();
        moonset = in.readLong();
        pressure = in.readLong();
        humidity = in.readLong();
        wind_speed = in.readDouble();
        wind_deg = in.readLong();
        wind_gust = in.readDouble();
        clouds = in.readLong();
        pop = in.readDouble();
        uvi = in.readDouble();
        temp=in.readParcelable(Temp.class.getClassLoader());
        feels_like=in.readParcelable(Feels_Like.class.getClassLoader());
        in.readTypedList(weatherArrayList, WeatherForecast.weather.CREATOR);
    }

    public static final Creator<WeatherForecast> CREATOR = new Creator<WeatherForecast>() {
        @Override
        public WeatherForecast createFromParcel(Parcel in) {
            return new WeatherForecast(in);
        }

        @Override
        public WeatherForecast[] newArray(int size) {
            return new WeatherForecast[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dt);
        dest.writeLong(sunrise);
        dest.writeLong(sunset);
        dest.writeLong(moonrise);
        dest.writeLong(moonset);
        dest.writeLong(pressure);
        dest.writeLong(humidity);
        dest.writeDouble(wind_speed);
        dest.writeLong(wind_deg);
        dest.writeDouble(wind_gust);
        dest.writeLong(clouds);
        dest.writeDouble(pop);
        dest.writeDouble(uvi);
        dest.writeParcelable(temp,flags);
        dest.writeParcelable(feels_like,flags);
        dest.writeTypedList(weatherArrayList);
    }

    public static double getMaxTemperatureForDay(String jsonObj, int dayIndex) throws JSONException {
        JSONObject weather = new JSONObject(jsonObj);
        JSONArray days = weather.getJSONArray("daily");
        JSONObject dayInfo = days.getJSONObject(dayIndex);
        JSONObject temperatureInfo = dayInfo.getJSONObject("temp");
        return temperatureInfo.getDouble("max");
    }

    public static String getReadableDateString(long time){
        Date date = new Date(time *1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMM d");
        return simpleDateFormat.format(date).toString();
    }

    public String formatHightLows (Context context,double high , double low){

        Resources res = context.getResources(); //assuming in an activity for example, otherwise you can provide a context.
        String[] units = res.getStringArray(R.array.units_values);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.pref_units_key),context.getString(R.string.pref_units_default));
        Log.v("Looo",unit);


        if(unit.equals(units[0])){ // metric
            // do nothing as already in metric
        }
        else if(unit.equals(units[1])){ // imperial
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;

        }else{
            // not found
            Log.v("Loo","Not Found Unit");
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highwLowStr = roundedHigh + "/" +roundedLow;
        return highwLowStr;
    }

    public static boolean setPreference(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_WEATHERFORECAST, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_WEATHERFORECAST, value);
        return editor.commit();
    }

    public static ArrayList<WeatherForecast> getWeatherDataFromJson(String jsonObj) throws JSONException {

        ArrayList<WeatherForecast> weatherForecastArrayList = null ;

        if(jsonObj!=null){
            weatherForecastArrayList = new ArrayList<>();

            JSONObject jsonObject =  new JSONObject(jsonObj);
            JSONArray jsonArray = jsonObject.getJSONArray("daily");

            for(int i=0;i<jsonArray.length();i++){
                JSONObject day_weather  = jsonArray.getJSONObject(i);
                WeatherForecast weatherForecast = new WeatherForecast(
                      day_weather.getLong("dt"),
                      day_weather.getLong("sunrise"),
                      day_weather.getLong("sunset"),
                      day_weather.getLong("moonrise"),
                      day_weather.getLong("moonset"),
                        fetchTempFromJson(day_weather.getJSONObject("temp")),
                        fetchFeelsLikeFromJson(day_weather.getJSONObject("feels_like")),
                        day_weather.getLong("pressure"),
                        day_weather.getLong("humidity"),
                        day_weather.getDouble("wind_speed"),
                        day_weather.getLong("wind_deg"),
                        day_weather.getDouble("wind_gust"),
                        fetchWeatherArrayListFrom(day_weather.getJSONArray("weather")),
                        day_weather.getLong("clouds"),
                        day_weather.getDouble("pop"),
                        day_weather.getDouble("uvi")
                );
                weatherForecastArrayList.add(weatherForecast);
            }
        }
        return  weatherForecastArrayList;
    }

    public static ArrayList<weather> fetchWeatherArrayListFrom(JSONArray jsonArray) throws JSONException {

        ArrayList<weather> weathers = new ArrayList<>();

        for(int i=0;i<jsonArray.length();i++){

            JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
            weather weather = new weather(
                    jsonObject.getInt("id"),
                    jsonObject.getString("main"),
                    jsonObject.getString("description"),
                    jsonObject.getString("icon")
            );
            weathers.add(weather);
        }
        return weathers;
    }
    public static Feels_Like fetchFeelsLikeFromJson(JSONObject jsonObject) throws JSONException {
        Feels_Like feels_like = new Feels_Like(
                jsonObject.getDouble("day"),
                jsonObject.getDouble("night"),
                jsonObject.getDouble("eve"),
                jsonObject.getDouble("morn")
        );
        return feels_like;
    }

    public static Temp fetchTempFromJson(JSONObject jsonObject) throws JSONException {
        Temp temp = new Temp(
               jsonObject.getDouble("day"),
                jsonObject.getDouble("min"),
                jsonObject.getDouble("max"),
                jsonObject.getDouble("night"),
                jsonObject.getDouble("eve"),
                jsonObject.getDouble("morn")
        );
        return temp;
    }
    public static String getPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_WEATHERFORECAST, Context.MODE_PRIVATE);
        return settings.getString(KEY_WEATHERFORECAST, "defaultValue");
    }

    public static void ClearSharedPrefences(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_WEATHERFORECAST, Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }

    public WeatherForecast() {
    }

    public WeatherForecast(Context context, Executor executor, Handler handler ){
        this.mContext=context;
        this.executor = executor;
        this.resultHandler = handler;
    }

    public static void v(String TAG, String message) {
        int maxLogSize = 2000;
        for(int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            android.util.Log.v(TAG, message.substring(start, end));
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void makeRequest(HttpCallBack httpCallBack,String url) throws IOException {
        Log.v(LOG_TAG,"httpCallRequest makeRequest");
        this.httpCallBack = httpCallBack;
        notifyFirst();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.v(LOG_TAG,"httpCallRequest doInBackground / executor ");
                String result = null;
                try {
                    result = makeSynchronousRequest(url);
                    notifyResult(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void notifyFirst() {
        Log.v(LOG_TAG,"httpCallRequest onPreExecute Done");
        try {
            httpCallBack.onPreExecute("onPreExecute Done",LOG_TAG);
        }catch (Exception e){
            e.printStackTrace();
            Log.v(LOG_TAG,e+"");
        }
    }

    private void notifyResult(String data) {
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                //httpCallBack.onPostExecute(result, LOG_TAG);
                Log.v(LOG_TAG,"httpCall onPostExecute Done");
                httpCallBack.onPostExecute(data, LOG_TAG);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected String makeSynchronousRequest(String url) throws IOException {
        Log.v(LOG_TAG,"httpCallRequest makeSynchronousRequest");
        String jsonResponse = "";

        URL mUrl = null;
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {

            Log.v(LOG_TAG,"httpCallRequest Post Data = "+url);

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setReadTimeout(500000 /* milliseconds 8.3 min */);
            urlConnection.setConnectTimeout(550000 /* milliseconds 8.8 min */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");


            urlConnection.connect();
            //Log.v(LOG_TAG, "httpCallRequest -> Overall Message " + urlConnection.getResponseMessage()+" \t Overall Code"+urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == 200) {
                Log.v(LOG_TAG, "httpCallRequest -> Good request " + urlConnection.getResponseMessage());
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } else if (urlConnection.getResponseCode() == 400) {

                Log.v(LOG_TAG, "httpCallRequest -> Bad request " + urlConnection.getResponseMessage());

            }else if(urlConnection.getResponseCode()==401) {

                Log.v(LOG_TAG, "httpCallRequest -> Bad request " + urlConnection.getResponseMessage());

            } else {

                Log.v(LOG_TAG, "Not Fetched -> " + urlConnection.getResponseMessage());
                Log.v(LOG_TAG, urlConnection.getResponseCode() + "");

            }

        } catch (IOException e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public WeatherForecast(long dt, long sunrise, long sunset, long moonrise, long moonset, WeatherForecast.Temp temp, WeatherForecast.Feels_Like feels_like, long pressure, long humidity, double wind_speed, long wind_deg, double wind_gust, ArrayList<weather> weatherArrayList, long clouds, double pop, double uvi) {
        this.dt = dt;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.moonrise = moonrise;
        this.moonset = moonset;
        this.temp = temp;
        this.feels_like = feels_like;
        this.pressure = pressure;
        this.humidity = humidity;
        this.wind_speed = wind_speed;
        this.wind_deg = wind_deg;
        this.wind_gust = wind_gust;
        this.weatherArrayList = weatherArrayList;
        this.clouds = clouds;
        this.pop = pop;
        this.uvi = uvi;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public long getMoonrise() {
        return moonrise;
    }

    public void setMoonrise(long moonrise) {
        this.moonrise = moonrise;
    }

    public long getMoonset() {
        return moonset;
    }

    public void setMoonset(long moonset) {
        this.moonset = moonset;
    }

    public WeatherForecast.Temp getTemp() {
        return temp;
    }

    public void setTemp(WeatherForecast.Temp temp) {
        this.temp = temp;
    }

    public WeatherForecast.Feels_Like getFeels_like() {
        return feels_like;
    }

    public void setFeels_like(WeatherForecast.Feels_Like feels_like) {
        this.feels_like = feels_like;
    }

    public long getPressure() {
        return pressure;
    }

    public void setPressure(long pressure) {
        this.pressure = pressure;
    }

    public long getHumidity() {
        return humidity;
    }

    public void setHumidity(long humidity) {
        this.humidity = humidity;
    }

    public double getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(double wind_speed) {
        this.wind_speed = wind_speed;
    }

    public long getWind_deg() {
        return wind_deg;
    }

    public void setWind_deg(long wind_deg) {
        this.wind_deg = wind_deg;
    }

    public double getWind_gust() {
        return wind_gust;
    }

    public void setWind_gust(double wind_gust) {
        this.wind_gust = wind_gust;
    }

    public List<weather> getWeatherArrayList() {
        return weatherArrayList;
    }

    public void setWeatherArrayList(ArrayList<weather> weatherArrayList) {
        this.weatherArrayList = weatherArrayList;
    }

    public long getClouds() {
        return clouds;
    }

    public void setClouds(long clouds) {
        this.clouds = clouds;
    }

    public double getPop() {
        return pop;
    }

    public void setPop(double pop) {
        this.pop = pop;
    }

    public double getUvi() {
        return uvi;
    }

    public void setUvi(double uvi) {
        this.uvi = uvi;
    }

    @Override
    public String toString() {
        return "WeatherForecast{" +
                "dt=" + dt +
                ", sunrise=" + sunrise +
                ", sunset=" + sunset +
                ", moonrise=" + moonrise +
                ", moonset=" + moonset +
                ", temp=" + temp +
                ", feels_like=" + feels_like +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                ", wind_speed=" + wind_speed +
                ", wind_deg=" + wind_deg +
                ", wind_gust=" + wind_gust +
                ", weatherArrayList=" + weatherArrayList +
                ", clouds=" + clouds +
                ", pop=" + pop +
                ", uvi=" + uvi +
                '}';
    }



    public static class Temp implements Parcelable{
        private double day;
        private double min;
        private double max;
        private double night;
        private double eve;
        private double morn;

        public Temp() {
        }

        public Temp(double day, double min, double max, double night, double eve, double morn) {
            this.day = day;
            this.min = min;
            this.max = max;
            this.night = night;
            this.eve = eve;
            this.morn = morn;
        }

        protected Temp(Parcel in) {
            day = in.readDouble();
            min = in.readDouble();
            max = in.readDouble();
            night = in.readDouble();
            eve = in.readDouble();
            morn = in.readDouble();
        }

        public static final Creator<Temp> CREATOR = new Creator<Temp>() {
            @Override
            public Temp createFromParcel(Parcel in) {
                return new Temp(in);
            }

            @Override
            public Temp[] newArray(int size) {
                return new Temp[size];
            }
        };

        public double getDay() {
            return day;
        }

        public void setDay(double day) {
            this.day = day;
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getNight() {
            return night;
        }

        public void setNight(double night) {
            this.night = night;
        }

        public double getEve() {
            return eve;
        }

        public void setEve(double eve) {
            this.eve = eve;
        }

        public double getMorn() {
            return morn;
        }

        public void setMorn(double morn) {
            this.morn = morn;
        }

        @Override
        public String toString() {
            return "temp{" +
                    "day=" + day +
                    ", min=" + min +
                    ", max=" + max +
                    ", night=" + night +
                    ", eve=" + eve +
                    ", morn=" + morn +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(day);
            dest.writeDouble(min);
            dest.writeDouble(max);
            dest.writeDouble(night);
            dest.writeDouble(eve);
            dest.writeDouble(morn);
        }
    }
    public static class Feels_Like implements Parcelable{
        private double day;
        private double night;
        private double eve;
        private double morn;

        public Feels_Like() {
        }

        public Feels_Like(double day, double night, double eve, double morn) {
            this.day = day;
            this.night = night;
            this.eve = eve;
            this.morn = morn;
        }

        protected Feels_Like(Parcel in) {
            day = in.readDouble();
            night = in.readDouble();
            eve = in.readDouble();
            morn = in.readDouble();
        }

        public static final Creator<Feels_Like> CREATOR = new Creator<Feels_Like>() {
            @Override
            public Feels_Like createFromParcel(Parcel in) {
                return new Feels_Like(in);
            }

            @Override
            public Feels_Like[] newArray(int size) {
                return new Feels_Like[size];
            }
        };

        public double getDay() {
            return day;
        }

        public void setDay(double day) {
            this.day = day;
        }

        public double getNight() {
            return night;
        }

        public void setNight(double night) {
            this.night = night;
        }

        public double getEve() {
            return eve;
        }

        public void setEve(double eve) {
            this.eve = eve;
        }

        public double getMorn() {
            return morn;
        }

        public void setMorn(double morn) {
            this.morn = morn;
        }

        @Override
        public String toString() {
            return "feels_like{" +
                    "day=" + day +
                    ", night=" + night +
                    ", eve=" + eve +
                    ", morn=" + morn +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(day);
            dest.writeDouble(night);
            dest.writeDouble(eve);
            dest.writeDouble(morn);
        }
    }
    public static class weather implements Parcelable{
        private int id;
        private String main;
        private String description;
        private String icon;

        public weather() {
        }

        public weather(int id, String main, String description, String icon) {
            this.id = id;
            this.main = main;
            this.description = description;
            this.icon = icon;
        }

        protected weather(Parcel in) {
            id = in.readInt();
            main = in.readString();
            description = in.readString();
            icon = in.readString();
        }

        public static final Creator<weather> CREATOR = new Creator<weather>() {
            @Override
            public weather createFromParcel(Parcel in) {
                return new weather(in);
            }

            @Override
            public weather[] newArray(int size) {
                return new weather[size];
            }
        };

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        @Override
        public String toString() {
            return "weather{" +
                    "id=" + id +
                    ", main='" + main + '\'' +
                    ", description='" + description + '\'' +
                    ", icon='" + icon + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(main);
            dest.writeString(description);
            dest.writeString(icon);
        }
    }
}
