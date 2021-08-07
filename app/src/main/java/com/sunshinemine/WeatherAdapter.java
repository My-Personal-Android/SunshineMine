package com.sunshinemine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
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

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherAdapterViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.date_textview.setText(WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+" - ");
        holder.forecast_textview.setText(mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain()+" - " );
        holder.high_textview.setText(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[0]+"\u00B0 / ");
        holder.low_textview.setText(WeatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()).split("/")[1]+"\u00B0");
        holder.linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,WeatherForecastDetails.class);
                Log.v("Helo",mWeatherForecast.get(position).toString());
                intent.putExtra("Data", (Parcelable) mWeatherForecast.get(position));
                context.startActivity(intent);
            }
        });
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
        public LinearLayout linear;
        public TextView date_textview;
        public TextView forecast_textview;
        public TextView high_textview;
        public TextView low_textview;

      //  private TextView

        public WeatherAdapterViewHolder(View itemView){
            super(itemView);
            linear =itemView.findViewById(R.id.linear);
            date_textview =itemView.findViewById(R.id.date_textview);
            forecast_textview =itemView.findViewById(R.id.forecast_textview);
            high_textview =itemView.findViewById(R.id.high_textview);
            low_textview =itemView.findViewById(R.id.low_textview);


        }
    }
}
