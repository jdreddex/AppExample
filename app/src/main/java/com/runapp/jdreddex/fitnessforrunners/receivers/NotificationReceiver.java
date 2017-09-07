package com.runapp.jdreddex.fitnessforrunners.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;
import com.runapp.jdreddex.fitnessforrunners.activities.RunActivity;

/**
 * Created by JDReddex on 14.07.2016.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final int NOTIFY_ID = 101;
    public static String NOTIFICATION_ID = "NOTIFICATION_ID";

    public static INotificationReceiverListener notificationReceiverListener;

    public interface INotificationReceiverListener{
        void notificationReceiverUpdateList();
    }
    public static void setListener(INotificationReceiverListener listener) {
        notificationReceiverListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        long _id = intent.getLongExtra(NOTIFICATION_ID, -1);
        SQLiteStatement statement = App.getInstance().getDb().compileStatement("DELETE FROM reminder WHERE _id = ?");
        statement.bindLong(1, _id);
        try {
            statement.executeUpdateDelete();
        }finally {
            statement.close();
        }
        notificationReceiverListener.notificationReceiverUpdateList();

        App.getInstance().getState().setRunFragmentScreen(1);
        Intent runIntent = new Intent(context, RunActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, runIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getInstance());
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo))
                .setContentTitle(App.getInstance().getString(R.string.notification_show_title))
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentText((App.getInstance().getString(R.string.notification_show_text)));
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getInstance());

        if(!App.getInstance().getState().isLocationRun()){
            notificationManager.notify(NOTIFY_ID, notification);
        }
    }
}
