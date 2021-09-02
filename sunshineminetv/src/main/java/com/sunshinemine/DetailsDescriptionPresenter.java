package com.sunshinemine;

import android.content.Context;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private Context context;

    public DetailsDescriptionPresenter(Context context){
        this.context = context;
    }
    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;

        if (movie != null) {
            viewHolder.getTitle().setText(movie.getTitle());
            viewHolder.getTitle().setTextColor(context.getResources().getColor(R.color.detail_accent_pane_background));
            viewHolder.getSubtitle().setText(movie.getStudio());
            viewHolder.getSubtitle().setTextColor(context.getResources().getColor(R.color.detail_accent_pane_background));
            viewHolder.getBody().setText(movie.getDescription());
            viewHolder.getBody().setTextColor(context.getResources().getColor(R.color.detail_accent_pane_background));
        }
    }
}