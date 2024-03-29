//package com.sunshinemine.extracustomthings;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.MenuItemCompat;
//import androidx.mediarouter.app.MediaRouteActionProvider;
//import androidx.mediarouter.media.MediaControlIntent;
//import androidx.mediarouter.media.MediaRouteSelector;
//import androidx.mediarouter.media.MediaRouter;
//
//import android.content.DialogInterface;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.Display;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.sunshinemine.R;
//
//@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
//public class CastActivity extends AppCompatActivity {
//
//    private MediaRouter mMediaRouter;
//
//    // Active Presentation, set to null if no secondary screen is enabled
//    private SamplePresentation mPresentation;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_cast);
//        mTextStatus = findViewById(R.id.textStatus);
//
//        // get the list of background colors
//        mColors = getResources().getIntArray(R.array.androidcolors);
//
//        // Enable clicks on the 'change color' button
//        mButton = findViewById(R.id.button1);
//        mButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                showNextColor();
//            }
//        });
//
//        // BEGIN_INCLUDE(getMediaRouter)
//        // Get the MediaRouter service
//        mMediaRouter = MediaRouter.getInstance(this);
//        // END_INCLUDE(getMediaRouter)
//    }
//
//    /**
//     * Implementing a {@link android.media.MediaRouter.Callback} to update the displayed
//     * {@link android.app.Presentation} when a route is selected, unselected or the
//     * presentation display has changed. The provided stub implementation
//     * {@link android.media.MediaRouter.SimpleCallback} is extended and only
//     * {@link android.media.MediaRouter.SimpleCallback#onRouteSelected(android.media.MediaRouter, int, android.media.MediaRouter.RouteInfo)}
//     * ,
//     * {@link android.media.MediaRouter.SimpleCallback#onRouteUnselected(android.media.MediaRouter, int, android.media.MediaRouter.RouteInfo)}
//     * and
//     * {@link android.media.MediaRouter.SimpleCallback#onRoutePresentationDisplayChanged(android.media.MediaRouter, android.media.MediaRouter.RouteInfo)}
//     * are overridden to update the displayed {@link android.app.Presentation} in
//     * {@link #updatePresentation()}. These callbacks enable or disable the
//     * second screen presentation based on the routing provided by the
//     * {@link android.media.MediaRouter} for {@link android.media.MediaRouter#ROUTE_TYPE_LIVE_VIDEO}
//     * streams. @
//     */
//    private final MediaRouter.Callback mMediaRouterCallback =
//            new MediaRouter.Callback() {
//
//                // BEGIN_INCLUDE(SimpleCallback)
//
//                /**
//                 * A new route has been selected as active. Disable the current
//                 * route and enable the new one.
//                 */
//                @Override
//                public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
//                    updatePresentation();
//                }
//
//                /**
//                 * The route has been unselected.
//                 */
//                @Override
//                public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
//                    updatePresentation();
//                }
//
//                /**
//                 * The route's presentation display has changed. This callback
//                 * is called when the presentation has been activated, removed
//                 * or its properties have changed.
//                 */
//                @Override
//                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo route) {
//                    updatePresentation();
//                }
//                // END_INCLUDE(SimpleCallback)
//            };
//
//    /**
//     * Updates the displayed presentation to enable a secondary screen if it has
//     * been selected in the {@link android.media.MediaRouter} for the
//     * {@link android.media.MediaRouter#ROUTE_TYPE_LIVE_VIDEO} type. If no screen has been
//     * selected by the {@link android.media.MediaRouter}, the current screen is disabled.
//     * Otherwise a new {@link SamplePresentation} is initialized and shown on
//     * the secondary screen.
//     */
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
//    private void updatePresentation() {
//
//        // BEGIN_INCLUDE(updatePresentationInit)
//        // Get the selected route for live video
//        MediaRouter.RouteInfo selectedRoute = mMediaRouter.getSelectedRoute();
//
//        // Get its Display if a valid route has been selected
//        Display selectedDisplay = selectedRoute.getPresentationDisplay();
//        // END_INCLUDE(updatePresentationInit)
//
//        // BEGIN_INCLUDE(updatePresentationDismiss)
//        /*
//         * Dismiss the current presentation if the display has changed or no new
//         * route has been selected
//         */
//        if (mPresentation != null && mPresentation.getDisplay() != selectedDisplay) {
//            mPresentation.dismiss();
//            mPresentation = null;
//            mButton.setEnabled(false);
//            mTextStatus.setText(R.string.secondary_notconnected);
//        }
//        // END_INCLUDE(updatePresentationDismiss)
//
//        // BEGIN_INCLUDE(updatePresentationNew)
//        /*
//         * Show a new presentation if the previous one has been dismissed and a
//         * route has been selected.
//         */
//        if (mPresentation == null && selectedDisplay != null) {
//
//            // Initialise a new Presentation for the Display
//            mPresentation = new SamplePresentation(this, selectedDisplay);
//            mPresentation.setOnDismissListener(mOnDismissListener);
//
//            // Try to show the presentation, this might fail if the display has
//            // gone away in the mean time
//            try {
//                mPresentation.show();
//                mTextStatus.setText(getResources().getString(R.string.secondary_connected,
//                        selectedRoute.getName()));
//                mButton.setEnabled(true);
//                showNextColor();
//            } catch (WindowManager.InvalidDisplayException ex) {
//                // Couldn't show presentation - display was already removed
//                mPresentation = null;
//            }
//        }
//        // END_INCLUDE(updatePresentationNew)
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // BEGIN_INCLUDE(addCallback)
//        // Register a callback for all events related to live video devices
//        mMediaRouter.addCallback(
//                new MediaRouteSelector.Builder()
//                        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
//                        .build(),
//                mMediaRouterCallback
//        );
//        // END_INCLUDE(addCallback)
//
//        // Show the 'Not connected' status message
//        mButton.setEnabled(false);
//        mTextStatus.setText(R.string.secondary_notconnected);
//
//        // Update the displays based on the currently active routes
//        updatePresentation();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // BEGIN_INCLUDE(onPause)
//        // Stop listening for changes to media routes.
//        mMediaRouter.removeCallback(mMediaRouterCallback);
//        // END_INCLUDE(onPause)
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        // BEGIN_INCLUDE(onStop)
//        // Dismiss the presentation when the activity is not visible.
//        if (mPresentation != null) {
//            mPresentation.dismiss();
//            mPresentation = null;
//        }
//        // BEGIN_INCLUDE(onStop)
//    }
//
//    /**
//     * Inflates the ActionBar or options menu. The menu file defines an item for
//     * the {@link android.app.MediaRouteActionProvider}, which is registered here for all
//     * live video devices using {@link android.media.MediaRouter#ROUTE_TYPE_LIVE_VIDEO}.
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//
//        getMenuInflater().inflate(R.menu.forecastmenu, menu);
//
//        // BEGIN_INCLUDE(MediaRouteActionProvider)
//        // Configure the media router action provider
//        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
//
//        MediaRouteActionProvider mediaRouteActionProvider =
//                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
//        mediaRouteActionProvider.setRouteSelector(
//                new MediaRouteSelector.Builder()
//                        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
//                        .build());
//        // BEGIN_INCLUDE(MediaRouteActionProvider)
//
//        return true;
//    }
//
//    /**
//     * Listens for dismissal of the {@link SamplePresentation} and removes its
//     * reference.
//     */
//    private final DialogInterface.OnDismissListener mOnDismissListener =
//            new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    if (dialog == mPresentation) {
//                        mPresentation = null;
//                    }
//                }
//            };
//
//    // Views used to display status information on the primary screen
//    private TextView mTextStatus;
//    private Button mButton;
//
//    // selected color index
//    private int mColor = 0;
//
//    // background colors
//    public int[] mColors;
//
//    /**
//     * Displays the next color on the secondary screen if it is activate.
//     */
//    private void showNextColor() {
//        if (mPresentation != null) {
//            // a second screen is active and initialized, show the next color
//            mPresentation.setColor(mColors[mColor]);
//            mColor = (mColor + 1) % mColors.length;
//        }
//    }
//}
