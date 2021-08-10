package com.sunshinemine;

import static com.sunshinemine.Utility.convertToCamelCase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherAdapterViewHolder> {


    private Context context;
    private ArrayList<WeatherForecast> mWeatherForecast;

    public WeatherAdapter(Context context, ArrayList<WeatherForecast> WeatherForecastlist) {
        this.context = context;
        this.mWeatherForecast = WeatherForecastlist;
    }
    @NonNull
    @Override
    public WeatherAdapterViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_forcast, parent, false);
        return new WeatherAdapter.WeatherAdapterViewHolder(view);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherAdapterViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if(0 == position){
            holder.top_linear.setVisibility(View.VISIBLE);
            holder.linear.setVisibility(View.GONE);

            holder.list_item_date_textview.setText("Today- "+ WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+"");
            holder.list_item_forecast_textview.setText(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain())+"");
            holder.list_item_high_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0])));
            holder.list_item_low_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1])));

            holder.list_item_icon.setImageResource(Utility.getArtResourceForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()));
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
            if(position == 1){
                holder.date_textview.setText("Tomorrow ");

            }else{
                holder.date_textview.setText(WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+"");

            }
            holder.forecast_textview.setText(convertToCamelCase(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()+"" ));
            holder.high_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0])));
            holder.low_textview.setText(context.getString(R.string.format_temperature,Float.parseFloat(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1])));
            holder.image.setImageResource(Utility.getArtResourceForWeatherCondition(mWeatherForecast.get(position).getWeatherArrayList().get(0).getId()));
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

        public LinearLayout top_linear;
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
            top_linear= itemView.findViewById(R.id.top_linear);
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
