package com.runapp.jdreddex.fitnessforrunners.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by JDReddex on 25.07.2016.
 */
public class TrackFavoriteAdapter extends RecyclerViewCursorAdapter<TrackFavoriteAdapter.TrackFavoriteViewHolder> {
        private final Context mContext;
        private Cursor cursor;
        private TrackFavoriteViewHolder.ClickListener clickListener;

        public TrackFavoriteAdapter(Context context,Cursor cursor,TrackFavoriteViewHolder.ClickListener clickListener ) {
            super(null);
            this.clickListener = clickListener;
            mContext = context;
            this.cursor = cursor;
            swapCursor(cursor);
        }

        @Override
        public TrackFavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.track_favorite_list_item, parent, false);
            return new TrackFavoriteViewHolder(view, clickListener);
        }

        @Override
        protected void onBindViewHolder(TrackFavoriteViewHolder holder, Cursor cursor, int  position){

            int distanceIndex = cursor.getInt(cursor.getColumnIndexOrThrow("distance"));
            long postIdIndex = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            long startTime = cursor.getLong(cursor.getColumnIndexOrThrow("startTime"));
            long time = (cursor.getLong(cursor.getColumnIndexOrThrow("time")));

            String distance = (String.format("%.3f", distanceIndex/1000f));

            int seconds = (int) (time / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int milliseconds = (int) (time % 100);
            String trackTime = ("" + minutes + ":"
                    + String.format("%02d", seconds) + ","
                    + String.format("%02d", milliseconds));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startTime);
            calendar.setTimeZone(TimeZone.getDefault());

            String dateStr = calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+ calendar.get(Calendar.YEAR);
            String startTimeStr = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);

            holder.distance.setText(distance + " " + App.getInstance().getString(R.string.kilometers));
            holder.trackTime.setText(trackTime);
            holder.startTime.setText(startTimeStr);
            holder.trackDate.setText(dateStr);
        }


    public static class TrackFavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        android.support.v7.widget.AppCompatTextView distance;
        android.support.v7.widget.AppCompatTextView trackTime;
        android.support.v7.widget.AppCompatTextView startTime;
        android.support.v7.widget.AppCompatTextView trackDate;
        private ClickListener listener;
        TrackFavoriteViewHolder(View view, ClickListener listener){
            super(view);
            distance = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.distance);
            trackTime = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.track_time);
            startTime = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.start_time);
            trackDate = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.track_date);
            this.listener = listener;
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View view)
        {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition());
            }
        }
        public interface ClickListener {
            void onItemClicked(int position);
        }
    }
}
