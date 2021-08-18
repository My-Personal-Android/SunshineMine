package com.sunshinemine;

import static androidx.core.content.ContextCompat.startActivity;
import static com.sunshinemine.Utility.convertToCamelCase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherAdapterViewHolder> {


    private Context context;
    private ArrayList<WeatherForecast> mWeatherForecast;
    private String pic_key=null;

    private View mEmptyView;
    private WeatherAdapterOnClickHandler mClickHandler;

    public static interface WeatherAdapterOnClickHandler {
        void onClick(Long date, WeatherAdapterViewHolder vh);
    }

    public WeatherAdapter(Context context, ArrayList<WeatherForecast> WeatherForecastlist,WeatherAdapterOnClickHandler mClickHandler,View view) {
        this.context = context;
        this.mWeatherForecast = WeatherForecastlist;
        this.mEmptyView = view;
        this.mClickHandler = mClickHandler;
    }
    @NonNull
    @Override
    public WeatherAdapterViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_forecast, parent, false);
        view.setFocusable(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String pic_key = prefs.getString(context.getString(R.string.pref_pics_key),context.getString(R.string.pref_pics_default));
        this.pic_key=pic_key;

        return new WeatherAdapter.WeatherAdapterViewHolder(view);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherAdapterViewHolder holder, @SuppressLint("RecyclerView") int position) {


        if(0 == position){

            holder.top_relative.setVisibility(View.VISIBLE);
            holder.top_relative.setContentDescription("Today "+convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()));
            holder.linear.setVisibility(View.GONE);

            holder.selected_city_textview.setText(Utility.convertToCamelCase(MainActivity.getPreference(context).toLowerCase()));

            holder.list_item_date_textview.setText("Today - "+ WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+"");
            holder.list_item_date_textview.setContentDescription("Today- "+ WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+"");
            holder.list_item_forecast_textview.setText(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain())+"");
            holder.list_item_forecast_textview.setContentDescription(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain())+"");
            holder.list_item_high_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0])));
            holder.list_item_high_textview.setContentDescription(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0])));
            holder.list_item_low_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1])));
            holder.list_item_low_textview.setContentDescription(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1])));

            if(pic_key.equals("image")){
                Glide.with(context)
                        .load(Utility.getImageUrlForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .error(Utility.getArtResourceForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .circleCrop()
                        .into(holder.list_item_icon);
            }else {
                Glide.with(context)
                        .load(Utility.getArtUrlForWeatherCondition(context, mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .error(Utility.getArtResourceForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .into(holder.list_item_icon);
            }

            holder.list_item_icon.setContentDescription("Image for Weather : "+convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()));
        }
        else if(position <= mWeatherForecast.size()){

            holder.top_relative.setVisibility(View.GONE);
            holder.linear.setVisibility(View.VISIBLE);
            holder.linear.setContentDescription(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()));
            if(position == 1){
                holder.date_textview.setText("Tomorrow ");
                holder.date_textview.setContentDescription("Tomorrow");
            }else{
                holder.date_textview.setText(WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+"");
                holder.date_textview.setContentDescription(WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+"");
            }
            holder.forecast_textview.setText(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()+"" ));
            holder.forecast_textview.setContentDescription(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()+"" ));
            holder.high_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0])));
            holder.high_textview.setContentDescription(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0])));
            holder.low_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1])));
            holder.low_textview.setContentDescription(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1])));

            if(pic_key.equals("image")){
                Glide.with(context)
                        .load(Utility.getImageUrlForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .error(Utility.getArtResourceForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .circleCrop()
                        .into(holder.image);
            }else {
                Glide.with(context)
                        .load(Utility.getArtUrlForWeatherCondition(context, mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .error(Utility.getArtResourceForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()))
                        .into(holder.image);
            }

            holder.image.setContentDescription("Image for Weather : "+convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()));
        }
    }

    public void swapWeatherList(ArrayList<WeatherForecast> weatherForecasts){

        mWeatherForecast = weatherForecasts;
        if (mWeatherForecast != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        if(mWeatherForecast==null)
            return 0;
        return mWeatherForecast.size();
    }

    class WeatherAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView selected_city_textview;

        public CardView top_relative;
        public TextView list_item_date_textview;
        public TextView list_item_high_textview;
        public TextView list_item_low_textview;
        public ImageView list_item_icon;
        public TextView list_item_forecast_textview;


        public LinearLayout linear;
        public TextView date_textview;
        public TextView forecast_textview;
        public TextView high_textview;
        public TextView low_textview;
        public ImageView image;

      //  private TextView

        public WeatherAdapterViewHolder(View itemView){
            super(itemView);

            // Selected city textview
            selected_city_textview = itemView.findViewById(R.id.selected_city_textview);

            // Top
            top_relative = itemView.findViewById(R.id.top_relative);
            list_item_date_textview =itemView.findViewById(R.id.list_item_date_textview);
            list_item_high_textview =itemView.findViewById(R.id.list_item_high_textview);
            list_item_low_textview =itemView.findViewById(R.id.list_item_low_textview);
            list_item_icon =itemView.findViewById(R.id.list_item_icon);
            list_item_forecast_textview =itemView.findViewById(R.id.list_item_forecast_textview);


            // Below
            linear =itemView.findViewById(R.id.linear);
            date_textview =itemView.findViewById(R.id.date_textview);
            forecast_textview =itemView.findViewById(R.id.forecast_textview);
            high_textview =itemView.findViewById(R.id.high_textview);
            low_textview =itemView.findViewById(R.id.low_textview);
            image = itemView.findViewById(R.id.image);

            itemView.setOnClickListener(this::onClick);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {

            Log.v("Logo key,",mWeatherForecast.get(getAdapterPosition())+"");
            mClickHandler.onClick(new Long(getAdapterPosition()),this);
            Intent intent = new Intent(context, WeatherForecastDetailsActivity.class);
            intent.putExtra(WeatherForecastDetailsActivity.DATA_KEY_EXTRA, (Parcelable) mWeatherForecast.get(getAdapterPosition()));
            // For Transition Effect
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                    v,context.getString(R.string.detail_icon_transition_name));
            context.startActivity(intent, activityOptions.toBundle());
        }
    }

}
