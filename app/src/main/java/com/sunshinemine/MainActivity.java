package com.sunshinemine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sunshinemine.cast.WeatherForecastDetailsPresentation;
import com.sunshinemine.data.WeatherContract;
import com.sunshinemine.sync.SunshineSyncAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "MainActivity";

    public static final String PREFERENCE_FILE="com.sunshinemine";
    private static final String KEY="CITY";

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

    private AnimatedVectorDrawable tickToCross,crossToTick;
    private boolean isTick = true;

    private MediaRouter mMediaRouter;

    // Active Presentation, set to null if no secondary screen is enabled
    private WeatherForecastDetailsPresentation mPresentation;

    // background colors
    public int mBackgroundColor;

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
        // BEGIN_INCLUDE(onStop)
        // Dismiss the presentation when the activity is not visible.
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
        // BEGIN_INCLUDE(onStop)
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the list of background colors
        mBackgroundColor = getResources().getColor(R.color.primary);

        // BEGIN_INCLUDE(getMediaRouter)
        // Get the MediaRouter service
        mMediaRouter = MediaRouter.getInstance(this);
        // END_INCLUDE(getMediaRouter)

        LoaderManager.getInstance(this).initLoader(FORECAST_LOADER,null,this);

        supportPostponeEnterTransition();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT > 21 ) {
            tickToCross = (AnimatedVectorDrawable)getDrawable(R.drawable.avd_tick2cross);
            crossToTick = (AnimatedVectorDrawable)getDrawable(R.drawable.avd_cross2tick);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        if(null!=fab) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AnimatedVectorDrawable drawable = isTick ? tickToCross : crossToTick;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        drawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                            @Override
                            public void onAnimationStart(Drawable drawable) {
                                super.onAnimationStart(drawable);
                            }

                            @Override
                            public void onAnimationEnd(Drawable drawable) {
                                super.onAnimationEnd(drawable);
                            }
                        });
                    }
                    fab.setImageDrawable(drawable);
                    drawable.start();
                    isTick = !isTick;

                    Snackbar
                            .make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                            .setAction("Action", new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onClick(View v) {
                                   // button.setImageResource(R.drawable.avd_cancel_check);

                                    fab.animate()
                                        .translationY(-fab.getHeight())
                                        .setDuration(2000)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                               // Toast.makeText(MainActivity.this,"Hello Toast Moved",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                  //  fab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 50, 50)));

                                    TextView title = findViewById(R.id.title_app);
                                    if(title!=null) {
                                        ObjectAnimator.ofObject(title,
                                                "textColor",
                                                new ArgbEvaluator(),
                                                Color.WHITE,
                                                Color.RED)
                                                .setDuration(1100)
                                                .start();
                                        title.animate()
                                                .setInterpolator(new BounceInterpolator())
                                                .setStartDelay(500)
                                                .setDuration(900)
                                                .translationX(-450)
                                                .withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ObjectAnimator.ofObject(title,
                                                                "textColor",
                                                                new ArgbEvaluator(),
                                                                Color.RED,
                                                                Color.WHITE)
                                                                .setDuration(1100)
                                                                .start();
                                                        title.animate()
                                                                .setInterpolator(new BounceInterpolator())
                                                                .setStartDelay(500)
                                                                .setDuration(900)
                                                                .translationX(0)
                                                                .withEndAction(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ObjectAnimator.ofObject(title,
                                                                                "textColor",
                                                                                new ArgbEvaluator(),
                                                                                Color.WHITE,
                                                                                Color.RED)
                                                                                .setDuration(1100)
                                                                                .start();
                                                                        title.animate()
                                                                                .setInterpolator(new BounceInterpolator())
                                                                                .setStartDelay(500)
                                                                                .setDuration(900)
                                                                                .translationX(+450)
                                                                                .withEndAction(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ObjectAnimator.ofObject(title,
                                                                                                "textColor",
                                                                                                new ArgbEvaluator(),
                                                                                                Color.RED,
                                                                                                Color.WHITE)
                                                                                                .setDuration(1100)
                                                                                                .start();
                                                                                        title.animate()
                                                                                                .setInterpolator(new BounceInterpolator())
                                                                                                .setStartDelay(500)
                                                                                                .setDuration(900)
                                                                                                .translationX(0)
                                                                                                .withEndAction(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {

                                                                                                    }
                                                                                                })
                                                                                                .start();
                                                                                    }
                                                                                })
                                                                                .start();
                                                                    }
                                                                })
                                                                .start();
                                                    }
                                                })
                                                .start();
                                    }
                                }
                            })
                            .setAnchorView(fab)
                            .show();

                }
            });
        }

        Empty_Textview = findViewById(R.id.Empty_Textview);

        recyclerview_forecast = findViewById(R.id.recyclerview_forecast);
        recyclerview_forecast.setHasFixedSize(true);
        recyclerview_forecast.setLayoutManager(new LinearLayoutManager(this));

        weatherAdapter = new WeatherAdapter(this, new ArrayList<>(), new WeatherAdapter.WeatherAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, WeatherAdapter.WeatherAdapterViewHolder vh) {
                Log.v("Chito",date+"");
            }
        },Empty_Textview);

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

        // specify an adapter (see also next example)

        final View parallaxView = findViewById(R.id.parallax_bar);
        if (null != parallaxView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                recyclerview_forecast.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int max = parallaxView.getHeight();
                        if (dy > 0) {
                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2 ));
                        } else {
                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }
//        final AppBarLayout appBarLayout = findViewById(R.id.appbar);
//        if(null!=appBarLayout){
//            ViewCompat.setElevation(appBarLayout,0);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                recyclerview_forecast.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                    @Override
//                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                        super.onScrolled(recyclerView, dx, dy);
//                        if( 0 == recyclerview_forecast.computeHorizontalScrollOffset()){
//                            appBarLayout.setElevation(0);
//                        }else{
//                            appBarLayout.setElevation(appBarLayout.getTargetElevation());
//                        }
//                    }
//                });
//            }
//        }

        SunshineSyncAdapter.initializeSyncAdapter(this);

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

    private final MediaRouter.Callback mMediaRouterCallback =
            new MediaRouter.Callback() {

                // BEGIN_INCLUDE(SimpleCallback)

                /**
                 * A new route has been selected as active. Disable the current
                 * route and enable the new one.
                 */
                @Override
                public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
                    updatePresentation();
                }

                /**
                 * The route has been unselected.
                 */
                @Override
                public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }

                /**
                 * The route's presentation display has changed. This callback
                 * is called when the presentation has been activated, removed
                 * or its properties have changed.
                 */
                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo route) {
                    updatePresentation();
                }
                // END_INCLUDE(SimpleCallback)
            };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updatePresentation() {

        // BEGIN_INCLUDE(updatePresentationInit)
        // Get the selected route for live video
        MediaRouter.RouteInfo selectedRoute = mMediaRouter.getSelectedRoute();

        // Get its Display if a valid route has been selected
        Display selectedDisplay = selectedRoute.getPresentationDisplay();
        // END_INCLUDE(updatePresentationInit)

        // BEGIN_INCLUDE(updatePresentationDismiss)
        /*
         * Dismiss the current presentation if the display has changed or no new
         * route has been selected
         */
        if (mPresentation != null && mPresentation.getDisplay() != selectedDisplay) {
            mPresentation.dismiss();
            mPresentation = null;
        }
        // END_INCLUDE(updatePresentationDismiss)

        // BEGIN_INCLUDE(updatePresentationNew)
        /*
         * Show a new presentation if the previous one has been dismissed and a
         * route has been selected.
         */
        if (mPresentation == null && selectedDisplay != null) {

            // Initialise a new Presentation for the Display
            mPresentation = new WeatherForecastDetailsPresentation(this, selectedDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);

            // Try to show the presentation, this might fail if the display has
            // gone away in the mean time
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                // Couldn't show presentation - display was already removed
                mPresentation = null;
            }
        }
        // END_INCLUDE(updatePresentationNew)

    }

    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        mPresentation = null;
                    }
                }
            };

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
        // BEGIN_INCLUDE(MediaRouteActionProvider)
        // Configure the media router action provider
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);

        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(
                new MediaRouteSelector.Builder()
                        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                        .build());
        // BEGIN_INCLUDE(MediaRouteActionProvider)
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
                recyclerview_forecast.scheduleLayoutAnimation();
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
            weatherAdapter.swapWeatherList(arrayList);
            recyclerview_forecast.scheduleLayoutAnimation();

        }
        else{
            SunshineSyncAdapter.syncImmediately(this);
            recyclerview_forecast.scheduleLayoutAnimation();
        }
        updateEmptyView();
        supportStartPostponedEnterTransition();
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        weatherAdapter.swapWeatherList(null);
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

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // BEGIN_INCLUDE(addCallback)
        // Register a callback for all events related to live video devices
        mMediaRouter.addCallback(
                new MediaRouteSelector.Builder()
                        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                        .build(),
                mMediaRouterCallback
        );
        // END_INCLUDE(addCallback)

        // Update the displays based on the currently active routes
        updatePresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        // BEGIN_INCLUDE(onPause)
        // Stop listening for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);
        // END_INCLUDE(onPause)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != recyclerview_forecast){
            recyclerview_forecast.clearOnScrollListeners();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_status_key))){
            updateEmptyView();
        }
    }
}