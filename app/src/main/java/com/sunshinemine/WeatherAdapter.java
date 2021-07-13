package com.sunshinemine;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = inflater.inflate(R.layout.item_weather, parent, false);
        return new WeatherAdapter.WeatherAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherAdapterViewHolder holder, int position) {
        WeatherForecast weatherForecast = new WeatherForecast();
        holder.text.setText(WeatherForecast.getReadableDateString(mWeatherForecast.get(position).getDt())+" - "+mWeatherForecast.get(position).getWeatherArrayList().get(0).getMain() + " - " + weatherForecast.formatHightLows(context,mWeatherForecast.get(position).getTemp().getMax(),mWeatherForecast.get(position).getTemp().getMin()));
        holder.text.setOnClickListener(new View.OnClickListener() {
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
        public TextView text;

        public WeatherAdapterViewHolder(View itemView){
            super(itemView);
            text =itemView.findViewById(R.id.text);
        }
    }
}
