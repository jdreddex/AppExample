package com.runapp.jdreddex.fitnessforrunners.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by JDReddex on 11.07.2016.
 */
public class TrackAdapter extends RecyclerViewCursorAdapter<TrackAdapter.TrackViewHolder> {
    private final Context mContext;
    private Cursor cursor;
    private TrackViewHolder.ClickListener clickListener;

    public TrackAdapter(Context context,Cursor cursor,TrackViewHolder.ClickListener clickListener ) {
        super(null);
        this.clickListener = clickListener;
        mContext = context;
        this.cursor = cursor;
        swapCursor(cursor);
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_list_item, parent, false);
        return new TrackViewHolder(view, clickListener);
    }

    @Override
    protected void onBindViewHolder(TrackViewHolder holder, Cursor cursor, int  position){

        int distanceIndex = cursor.getInt(cursor.getColumnIndexOrThrow("distance"));
        long postIdIndex = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow("startTime"));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("time"));
        int favorite = cursor.getInt(cursor.getColumnIndexOrThrow("favorite"));

        String distance = (String.format("%.3f", distanceIndex/1000f));

        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        int milliseconds = (int) (time % 100);
        String trackTime = ("" + minutes + ":"
                + String.format("%02d", seconds) + ","
                + String.format("%02d", milliseconds));

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(startTime);
        calendar1.setTimeZone(TimeZone.getDefault());

        String dateStr = calendar1.get(Calendar.DAY_OF_MONTH)+"/"+(calendar1.get(Calendar.MONTH)+1)+"/"+ calendar1.get(Calendar.YEAR);
        String startTimeStr = calendar1.get(Calendar.HOUR_OF_DAY)+":"+calendar1.get(Calendar.MINUTE);

        holder.distance.setText(distance + " " + App.getInstance().getString(R.string.kilometers));
        holder.trackTime.setText(trackTime);
        holder.startTime.setText(startTimeStr);
        holder.trackDate.setText(dateStr);
        if (favorite == 1){
            holder.favorite.setChecked(true);
        }else if(favorite == 0){
            holder.favorite.setChecked(false);
        }
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private android.support.v7.widget.AppCompatTextView distance;
        private android.support.v7.widget.AppCompatTextView trackTime;
        private android.support.v7.widget.AppCompatTextView startTime;
        private android.support.v7.widget.AppCompatTextView trackDate;
        private android.support.v7.widget.AppCompatCheckBox favorite;
        private ClickListener listener;

        TrackViewHolder(View view, ClickListener listener){
            super(view);
            distance = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.distance);
            trackTime = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.track_time);
            startTime = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.start_time);
            trackDate = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.track_date);
            favorite = (android.support.v7.widget.AppCompatCheckBox) view.findViewById(R.id.checkbox_favorite);
            this.listener = listener;
            favorite.setOnClickListener(this);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View view)
        {
            if(view.getClass() == favorite.getClass()){
                if(favorite.isChecked()) {
                    listener.onCheckedChange(getAdapterPosition(), 1);
                }else {
                    listener.onCheckedChange(getAdapterPosition(), 0);
                }
            }else{
                if (listener != null) {
                    listener.onItemClicked(getAdapterPosition());
                }
            }
        }

        public interface ClickListener {
            void onItemClicked(int position);
            void onCheckedChange(int position, int fav);
        }
    }
}
