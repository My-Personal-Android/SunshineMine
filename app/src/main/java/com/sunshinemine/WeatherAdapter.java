package com.sunshinemine;

import static com.sunshinemine.Utility.convertToCamelCase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherAdapterViewHolder> {


    private Context context;
    private ArrayList<WeatherForecast> mWeatherForecast;
    private String pic_key=null;

    public WeatherAdapter(Context context, ArrayList<WeatherForecast> WeatherForecastlist) {
        this.context = context;
        this.mWeatherForecast = WeatherForecastlist;
    }
    @NonNull
    @Override
    public WeatherAdapterViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_forcast, parent, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String pic_key = prefs.getString(context.getString(R.string.pref_pics_key),context.getString(R.string.pref_pics_default));
        this.pic_key=pic_key;
        Log.v("LoooL",pic_key);
        return new WeatherAdapter.WeatherAdapterViewHolder(view);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherAdapterViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if(0 == position){

            holder.top_linear.setVisibility(View.VISIBLE);
            holder.top_linear.setContentDescription("Today "+convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()));
            holder.linear.setVisibility(View.GONE);

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
            holder.top_linear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context,WeatherForecastDetails.class);
                    intent.putExtra(WeatherForecastDetails.DATA_KEY_EXTRA, (Parcelable) mWeatherForecast.get(position));
                    context.startActivity(intent);
                }
            });
        }else{

            holder.top_linear.setVisibility(View.GONE);
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
            holder.linear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context,WeatherForecastDetails.class);
                    intent.putExtra(WeatherForecastDetails.DATA_KEY_EXTRA, (Parcelable) mWeatherForecast.get(position));
                    context.startActivity(intent);
                }
            });
        }
    }

    public void swapWeather(ArrayList<WeatherForecast> weatherForecasts){

        mWeatherForecast = weatherForecasts;
        if (mWeatherForecast != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }

    }

    @Override
    public int getItemCount() {
        if(mWeatherForecast==null) return 0;
        return mWeatherForecast.size();
    }

    class WeatherAdapterViewHolder extends RecyclerView.ViewHolder{

        public GridLayout top_linear;
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

            // Top
            top_linear= itemView.findViewById(R.id.top_grid);
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

        }
    }

}
