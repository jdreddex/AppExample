package com.runapp.jdreddex.fitnessforrunners.services;

import android.os.Process;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Handler;

import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import android.widget.Toast;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;
import com.runapp.jdreddex.fitnessforrunners.models.Point;
import com.runapp.jdreddex.fitnessforrunners.models.TrackToSaveRequest;
import com.runapp.jdreddex.fitnessforrunners.providers.SaveProvider;
import com.runapp.jdreddex.fitnessforrunners.providers.TracksProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class SynchronizeService extends Service{
    private boolean isActive;
    private static final int NOTIFICATION_ID = 2;
    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";
    public static final String TAG = "TAG";
    private String synchronizationFinished;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Handler customHandler = new Handler();

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            final Runnable generate = new Runnable() {
                @Override
                public void run() {
                    synchronizeTracks();
                }
            };
            customHandler.postDelayed(generate, 0);
            Toast.makeText(App.getInstance(), synchronizationFinished, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isActive = false;
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        isActive = false;
        synchronizationFinished = getResources().getString(R.string.synchronization_finished);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isActive) {
            isActive = true;
            Intent runIntent = new Intent(this, MainActivity.class);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getInstance())
                    .setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.app_logo))
                    .setContentIntent(PendingIntent.getActivity(
                            App.getInstance(),
                            0,
                            runIntent,
                            0
                    ))
                    .setContentTitle(getString(R.string.synchronize_service_notification_title))
                    .setContentText(getString(R.string.synchronize_service_notification_text))
                    .setAutoCancel(false)
                    .setOngoing(true);
            startForeground(NOTIFICATION_ID, builder.build());
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (!isActive) {
            isActive = true;
            stopForeground(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void synchronizeTracks(){
        Cursor saveCursor = App.getInstance().getDb().rawQuery("SELECT _id, startTime, time, distance, synchronized FROM track ORDER BY _id",null);
        if (saveCursor != null) {
            if (saveCursor.moveToFirst()) {
                int idIndex = saveCursor.getColumnIndexOrThrow("_id");
                int startTimeIndex = saveCursor.getColumnIndexOrThrow("startTime");
                int timeIndex = saveCursor.getColumnIndexOrThrow("time");
                int distanceIndex = saveCursor.getColumnIndexOrThrow("distance");
                int synchonizedIndex = saveCursor.getColumnIndexOrThrow("synchronized");
                do {
                    TrackToSaveRequest trackToSaveRequest = new TrackToSaveRequest();
                    int trackId = saveCursor.getInt(idIndex);
                    long startTime = saveCursor.getLong(startTimeIndex);
                    long time = saveCursor.getLong(timeIndex);
                    int distance = saveCursor.getInt(distanceIndex);
                    int synchronizedStr = saveCursor.getInt(synchonizedIndex);

                    trackToSaveRequest.setToken(App.getInstance().getState().getToken());
                    trackToSaveRequest.setBeginsAt(startTime);
                    trackToSaveRequest.setMyId(trackId);
                    trackToSaveRequest.setTime(time);
                    trackToSaveRequest.setDistance(distance);
                    if (synchronizedStr > 0){
                        trackToSaveRequest.setId(synchronizedStr);
                        App.getInstance().getState().addToTrackListToSend(trackToSaveRequest);
                    }else {
                        App.getInstance().getState().addToTrackListToSend(trackToSaveRequest);
                    }
                } while (saveCursor.moveToNext());
            }
            saveCursor.close();
        }

        for (int i=0; i < App.getInstance().getState().getTrackListToSend().size(); i++){
            int id = App.getInstance().getState().getTrackListToSend().get(i).getMyId();
            List<Point> points = new ArrayList<>();

            Cursor pointCursor = App.getInstance().getDb().rawQuery("SELECT lat, lng FROM track_gps WHERE trackId = ? ORDER BY _id", new String[]{
                    String.valueOf(id)
            });
            if (pointCursor != null) {
                if (pointCursor.moveToFirst()) {
                    int latIndex = pointCursor.getColumnIndexOrThrow("lat");
                    int lngIndex = pointCursor.getColumnIndexOrThrow("lng");
                    do {
                        Point point = new Point();
                        double lat = pointCursor.getDouble(latIndex);
                        double lng = pointCursor.getDouble(lngIndex);
                        point.setLat(lat);
                        point.setLng(lng);
                        points.add(point);
                    } while (pointCursor.moveToNext());
                }
                pointCursor.close();
            }
            App.getInstance().getState().getTrackListToSend().get(i).setPoints(points);
        }

        App.getInstance().getState().setCurrentTrackToSend(0);
        if(App.getInstance().getState().getTrackListToSend().size()!=0){
            TrackToSaveRequest trackToSaveRequest = App.getInstance().getState().getTrackListToSend().get(App.getInstance().getState().getCurrentTrackToSend());
            SaveProvider.saveRequest(trackToSaveRequest);
        }else {
            App.getInstance().getState().setTrackIdCounter(0);
            TracksProvider.tracksRequest(App.getInstance().getState().getToken());
        }
    }
}
