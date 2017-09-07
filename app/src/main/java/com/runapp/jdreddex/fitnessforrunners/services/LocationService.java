package com.runapp.jdreddex.fitnessforrunners.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import android.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.activities.RunActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by JDReddex on 21.07.2016.
 */
public class LocationService  extends Service implements LocationListener {
    private static final int NOTIFICATION_ID = 1;
    private static final long TIME_BETWEEN_UPDATES = 5000;
    private static final int TWO_MINUTES = 2 * 60 * 1000  ;
    private static final float UPDATE_DISTANCE_THRESHOLD_METERS = 5.0f;
    private boolean isActive;
    private float distanceFull;
    private long trackId;
    private Location previousBestLocation = null;
    private LocationManager locationManager;
    private Handler customHandler = new Handler();

    private Timer timer;
    public static final String TIME = "TIME";
    public static final String TAG = "TAG";
    public static final String ACTION_LOCATION_BROADCAST = "ACTION_LOCATION_BROADCAST";

    private int counterPos = 0;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private long startTime = 0L;
    private ArrayList<LatLng> latLngList = new  ArrayList<>();

    public float getDistanceFull() {
        return distanceFull;
    }
    public void addToDistanceFull(float distanceFull) {
        this.distanceFull = this.distanceFull + distanceFull;
    }

    public ArrayList<LatLng> getLatLngList() {
        return latLngList;
    }
    public void addToLatLngList(LatLng latLng) {
        this.latLngList.add(latLng);
    }

    public long getTrackId() {
        return trackId;
    }
    public void setTackId(long trackId) {
        this.trackId = trackId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isActive = false;
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            if (!isActive) {
                isActive = true;
                App.getInstance().getState().setIsLocationRun(true);
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        TIME_BETWEEN_UPDATES,
                        UPDATE_DISTANCE_THRESHOLD_METERS,
                        this
                );
                Intent runIntent = new Intent(this, RunActivity.class);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getInstance())
                        .setSmallIcon(R.drawable.app_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.app_logo))
                        .setContentIntent(PendingIntent.getActivity(
                                App.getInstance(),
                                0,
                                runIntent,
                                0
                        ))
                        .setContentTitle(getString(R.string.location_service_notification_title))
                        .setContentText(getString(R.string.location_service_notification_text))
                        .setAutoCancel(false)
                        .setOngoing(true);
                startForeground(NOTIFICATION_ID, builder.build());
            }

            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.getTimeInMillis();

            SQLiteStatement statement = App.getInstance().getDb().compileStatement("INSERT INTO track (startTime) VALUES (?)");
            statement.bindLong(1, calendar.getTimeInMillis());

            try {
                setTackId(statement.executeInsert());
            } finally {
                statement.close();
            }
        }catch (SecurityException se){
            Log.e(TAG,TAG);
        }

        App.getInstance().getState().setIsServiceRun(true);

        timer = new Timer();
        startTime = SystemClock.uptimeMillis();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                customHandler.postDelayed(generate, 0);
            }
        }, 100, 1);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        if (isActive) {
            isActive = false;
            try {
                locationManager.removeUpdates(this);
            }catch (SecurityException se){
                Log.e(TAG,TAG);
            }
            stopForeground(true);
        }
        if(counterPos<=0){
            try {
                if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
                    sendLocationToDB(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                }else if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) !=null){
                    sendLocationToDB(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                }else{
                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    location.setLongitude(26.62917);
                    location.setLatitude(-70.88361);
                }


            }catch (SecurityException se){
                Log.e(TAG, TAG);
            }
        }
        Toast.makeText(App.getInstance(), getString(R.string.run_finished), Toast.LENGTH_SHORT ).show();

        float[] results = new float[1];
        for (int i = 0; i<getLatLngList().size()-1; i++) {
            LatLng a = getLatLngList().get(i);
            LatLng b = getLatLngList().get(i + 1);
            Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);
            addToDistanceFull(results[0]);
        }

        SQLiteStatement statement = App.getInstance().getDb().compileStatement("UPDATE track SET distance = ?, time = ? WHERE _id = ?");
        statement.bindString(1, String.valueOf((int) getDistanceFull()));
        statement.bindLong(2, App.getInstance().getState().getTrackTimeOnFinish());
        statement.bindLong(3, getTrackId());
        try {
            statement.executeUpdateDelete();
        } finally {
            statement.close();
        }
        App.getInstance().getState().setRunDistance((int) getDistanceFull());
        App.getInstance().getState().setIsLocationRun(false);
        super.onDestroy();
    }
    @Override
    public void onLocationChanged(Location location) {
        if(isBetterLocation(location, previousBestLocation)) {
            counterPos =+1;
            previousBestLocation = location;
            sendLocationToDB(location);
        }
    }

    private void sendLocationToDB(Location location){
        SQLiteStatement statement = App.getInstance().getDb().compileStatement("INSERT INTO track_gps (trackId,lat,lng) VALUES (?,?,?)");
        statement.bindLong(1, getTrackId());
        statement.bindDouble(2, location.getLatitude());
        statement.bindDouble(3, location.getLongitude());
        addToLatLngList(new LatLng( location.getLatitude(),location.getLongitude()));
        try {
            statement.executeInsert();
        } finally {
            statement.close();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(App.getInstance(), getString(R.string.run_started), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void sendBroadcastMessage(String time) {
        if (time != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(TIME, time);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private final Runnable generate = new Runnable() {
        @Override
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            App.getInstance().getState().setTrackTimeOnFinish(updatedTime);
            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int milliseconds = (int) (updatedTime % 100);
            String timerTime = ("" + minutes + ":"
                    + String.format("%02d", seconds) + ","
                    + String.format("%02d", milliseconds));
            sendBroadcastMessage(timerTime);
        }
    };

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}



