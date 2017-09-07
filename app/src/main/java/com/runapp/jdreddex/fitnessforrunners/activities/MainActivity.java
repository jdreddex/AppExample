package com.runapp.jdreddex.fitnessforrunners.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.receivers.NotificationReceiver;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.fragments.AddNotificationDialogFragment;
import com.runapp.jdreddex.fitnessforrunners.fragments.NotificationListFragment;

import com.runapp.jdreddex.fitnessforrunners.fragments.TrackFragment;
import com.runapp.jdreddex.fitnessforrunners.fragments.TrackListFavoriteFragment;
import com.runapp.jdreddex.fitnessforrunners.fragments.TrackListFragment;
import com.runapp.jdreddex.fitnessforrunners.services.SynchronizeService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JDReddex on 08.07.2016.
 */
public class MainActivity extends AppCompatActivity
        implements AddNotificationDialogFragment.AddNotificationDialogListener,
        NotificationListFragment.INotificationListListener,
        TrackListFragment.ITrackListListener,
        TrackListFavoriteFragment.ITrackFavoriteListListener{

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView navList;
    private long time;
    Bundle bundle = new Bundle();

    private static final String ATTRIBUTE_NAME_TEXT = "ATTRIBUTE_NAME_TEXT";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    public static final String TAG_EXIT = "TAG_EXIT";

    private String[] FRAGMENT_TAGS = {TrackListFragment.TAG_MAIN,TrackListFavoriteFragment.TAG_FAVORITE,NotificationListFragment.TAG_NOTIFICATION,TAG_EXIT} ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navList = (ListView)findViewById(R.id.nav_list);
        setSupportActionBar(toolbar);

        final String[] texts = getResources().getStringArray(R.array.pages);
        final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>(texts.length);
        Map<String, String> m;
        for (int i = 0; i < texts.length; i++) {
            m = new HashMap<String, String>();
            m.put(ATTRIBUTE_NAME_TEXT, texts[i]);
            m.put(FRAGMENT_TAG, FRAGMENT_TAGS[i]);
            data.add(m);
        }
        String[] from = {ATTRIBUTE_NAME_TEXT};
        int[] to = {android.R.id.text1};
        SimpleAdapter adapter = new SimpleAdapter(this, data,R.layout.menu_list_item, from, to);
        navList.setAdapter(adapter);
        navList.setItemChecked(0, true);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            TrackListFragment details = TrackListFragment.newInstance();
             getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            showFragment(details,null,true,TrackListFragment.TAG_MAIN);
        }

        navList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3){
                AppCompatTextView textView = (AppCompatTextView) v.findViewById(android.R.id.text1);
                textView.setTag(textView.getText());
                String tag = data.get(position).get(FRAGMENT_TAG);
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.viewer);

                if (tag.equals(TrackListFragment.TAG_MAIN)){
                    TrackListFragment trackListFragment = TrackListFragment.newInstance();

                    if (!(fragment != null && tag.equals(fragment.getTag()))) {
                        showFragment(trackListFragment, null, true, TrackListFragment.TAG_MAIN);
                    }
                    navList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    hideDrawer();
                }else if(tag.equals(NotificationListFragment.TAG_NOTIFICATION)){
                    NotificationListFragment notificationListFragment = NotificationListFragment.newInstance();
                    if (!(fragment != null && tag.equals(fragment.getTag()))) {
                        showFragment(notificationListFragment, TrackListFragment.TAG_MAIN, false, NotificationListFragment.TAG_NOTIFICATION);
                    }
                    navList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    navList.setItemChecked(position,true);
                    hideDrawer();
                }else if(tag.equals(TrackListFavoriteFragment.TAG_FAVORITE)){
                    TrackListFavoriteFragment trackListFavoriteFragment = TrackListFavoriteFragment.newInstance();
                    if (!(fragment != null && tag.equals(fragment.getTag()))) {
                        showFragment(trackListFavoriteFragment, TrackListFragment.TAG_MAIN, false, TrackListFavoriteFragment.TAG_FAVORITE);
                    }
                    navList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    navList.setItemChecked(position,true);
                    hideDrawer();
                }else if(tag.equals(TAG_EXIT)){
                    App.getInstance().getState().deleteToken();
                    App.getInstance().getState().deleteIsFirstStart();
                    clearDatabase();
                    App.getInstance().createState();
                    Intent intent = new Intent(App.getInstance(), LogRegActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.animator.splash_exit, R.animator.splash_enter);
                    Intent syncIntent = new Intent(App.getInstance(), SynchronizeService.class);
                    App.getInstance().stopService(syncIntent);
                    hideDrawer();
                    finish();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.getInstance().getState() == null) {
            App.getInstance().createState();
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
    }


    public void onFragmentStart(int titleResId, String tag) {
        getSupportActionBar().setTitle(titleResId);
        switch (tag) {
            case TrackListFragment.TAG_MAIN : navList.setItemChecked(0, true);
                break;
            case TrackListFavoriteFragment.TAG_FAVORITE: navList.setItemChecked(1, true);
                break;
            case NotificationListFragment.TAG_NOTIFICATION : navList.setItemChecked(2, true);
                break;
        }
    }

    private void hideDrawer(){
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    @Override
    public void onBackPressed() {
        drawerToggle.syncState();
        hideDrawer();
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void showFragment(Fragment fragment,String clearToTag, boolean clearInclusive, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (clearToTag != null || clearInclusive) {
            fragmentManager.popBackStack(
                    clearToTag,
                    clearInclusive ? FragmentManager.POP_BACK_STACK_INCLUSIVE : 0
            );
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.viewer, fragment, tag);
        transaction.addToBackStack(tag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    @Override
    public void fabOnClick(){
        DialogFragment dialog = AddNotificationDialogFragment.newInstance();
        showFragment(dialog, null, false, AddNotificationDialogFragment.TAG_ADD_NOTIFICATION);
    }

    @Override
    public void trackFavoriteListOnItemClick(long _id){
        TrackFragment trackFragment = TrackFragment.newInstance();
        Bundle trackBundle = new Bundle();
        trackBundle.putLong("_id", _id);
        trackFragment.setArguments(trackBundle);
        showFragment(trackFragment, null, false, TrackFragment.TRACK_FRAGMENT_TAG);
    }

    @Override
    public void fabRunOnClick(){
        App.getInstance().getState().setRunFragmentScreen(1);
        Intent intent = new Intent(App.getInstance(), RunActivity.class);
        startActivity(intent);
        overridePendingTransition(R.animator.splash_exit, R.animator.splash_enter);
    }

    @Override
    public void trackListOnItemClicked(long _id){
        TrackFragment trackFragment = TrackFragment.newInstance();
        Bundle trackBundle = new Bundle();
        trackBundle.putLong("_id", _id);
        trackFragment.setArguments(trackBundle);
        showFragment(trackFragment, null, false, TrackFragment.TRACK_FRAGMENT_TAG);
    }

    @Override
    public void trackListOnCheckedChange(long position, int favFlag){
        SQLiteStatement statement = App.getInstance().getDb().compileStatement("UPDATE track SET favorite = ? WHERE _id = ?");
        statement.bindString(1, String.valueOf(favFlag));
        statement.bindLong(2, position);
        try {
            statement.executeUpdateDelete();
        } finally {
            statement.close();
        }
    }

    @Override
    public void notificationBtnEdit(long _id){
        Cursor cursor = App.getInstance().getDb().rawQuery("SELECT time FROM reminder WHERE _id =?", new String[]{
                String.valueOf(_id)
        });
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int timeIndex = cursor.getColumnIndexOrThrow("time");
                time = cursor.getLong(timeIndex);
                setBundleForDialog(_id,time);
            }
            cursor.close();
        }
        DialogFragment dialog = new AddNotificationDialogFragment();
        dialog.setArguments(bundle);
        showFragment(dialog, null, false, AddNotificationDialogFragment.TAG_ADD_NOTIFICATION);
    }

    private void setBundleForDialog(long _id, long time){
        bundle.putLong("_id", _id);
        bundle.putLong("time", time);
    }

    @Override
    public void notificationBtnRemove(long _id){
        removeNotification(_id);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_delete_notification), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void saveOnClick(Calendar date) {
        scheduleNotification(date);
        onBackPressed();
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_save_notification), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void reSaveNotification(Calendar date, long _id){
        removeNotification(_id);
        editNotification(date, _id);
        onBackPressed();
    }

    private void scheduleNotification(Calendar date) {
        long time = date.getTimeInMillis();
        long lastId;

        SQLiteStatement statement = App.getInstance().getDb().compileStatement("INSERT INTO reminder (time) VALUES (?)");
        statement.bindLong(1, time);

        try {
            lastId = statement.executeInsert();
        } finally {
            statement.close();
        }

        Intent notificationIntent = new Intent(App.getInstance(), NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, lastId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pendingIntent);
    }

    private void editNotification(Calendar date, long _id) {
        long time = date.getTimeInMillis();
        SQLiteStatement statement = App.getInstance().getDb().compileStatement("UPDATE reminder SET time = ? WHERE _id = ?");
        statement.bindLong(1, time);
        statement.bindLong(2, _id);

        try {
            statement.executeUpdateDelete();
        } finally {
            statement.close();
        }

        Intent notificationIntent = new Intent(App.getInstance(), NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, _id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pendingIntent);
    }

    private void removeNotification(long _id){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(App.getInstance(), NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, _id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private void clearDatabase(){
        Cursor cursor = App.getInstance().getDb().rawQuery("SELECT _id, startTime, time, distance FROM track WHERE synchronized = 0 ORDER BY _id",null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("_id");
                do {
                    long nId = cursor.getInt(idIndex);
                    removeNotification(nId);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        SQLiteStatement statementTrack = App.getInstance().getDb().compileStatement("DELETE FROM track");
        try {
            statementTrack.executeUpdateDelete();
        } finally {
            statementTrack.close();
        }
        SQLiteStatement statementTrackGps = App.getInstance().getDb().compileStatement("DELETE FROM track_gps");
        try {
            statementTrackGps.executeUpdateDelete();
        } finally {
            statementTrackGps.close();
        }
        SQLiteStatement statementReminder = App.getInstance().getDb().compileStatement("DELETE FROM reminder");
        try {
            statementReminder.executeUpdateDelete();
        } finally {
            statementReminder.close();
        }
    }
}

