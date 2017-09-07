package com.runapp.jdreddex.fitnessforrunners.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by JDReddex on 11.07.2016.
 */
public class NotificationAdapter extends RecyclerViewCursorAdapter<NotificationAdapter.NotificationViewHolder> {
    private final Context mContext;
    private Cursor cursor;
    private NotificationViewHolder.ClickListener clickListener;

    public NotificationAdapter(Context context,Cursor cursor, NotificationViewHolder.ClickListener clickListener ) {
        super(null);
        this.clickListener = clickListener;
        mContext = context;
        this.cursor = cursor;
        swapCursor(cursor);
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_list_item, parent, false);
        return new NotificationViewHolder(view,clickListener);
    }

    @Override
    protected void onBindViewHolder(NotificationViewHolder holder, Cursor cursor, int position){

        long notificationId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("time"));

        Locale locale = App.getInstance().getResources().getConfiguration().locale;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat sdfTime = new SimpleDateFormat("dd/MM/yy", locale);
        String dateStr = sdfTime.format(time);

        SimpleDateFormat sdfStartTime = new SimpleDateFormat("HH:mm",locale);
        String timeStr = sdfStartTime.format(time);

        holder.timeTv.setText(timeStr);
        holder.dateTv.setText(dateStr);
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        TextView dateTv;
        TextView noteTv;
        TextView timeTv;
        View selectedOverlay;

        private ClickListener listener;
        NotificationViewHolder(View view,ClickListener listener){
            super(view);
            this.listener = listener;
            dateTv = (TextView) view.findViewById(R.id.notification_date);
            noteTv = (TextView) view.findViewById(R.id.notification_note);
            timeTv = (TextView) view.findViewById(R.id.notification_time);
            selectedOverlay = (View) view.findViewById(R.id.selected_overlay);
            view.setOnLongClickListener(this);
        }
        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                listener.onLongClickNotification(getAdapterPosition());
            }
            return true;
        }

        public interface ClickListener {
            void onLongClickNotification(int position);
        }
    }
}
