package com.runapp.jdreddex.fitnessforrunners.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.fragments.RunDialogFragment;
import com.runapp.jdreddex.fitnessforrunners.providers.SaveProvider;
import com.runapp.jdreddex.fitnessforrunners.services.LocationService;
import com.runapp.jdreddex.fitnessforrunners.R;

/**
 * Created by JDReddex on 16.07.2016.
 */
public class RunActivity extends AppCompatActivity {
    private LinearLayout screenOne;
    private AppCompatButton btnStart;
    private LinearLayout screenTwo;
    private AppCompatTextView runTimer;
    private AppCompatButton btnFinish;
    private LinearLayout screenThree;
    private AppCompatTextView runTimerResult;
    private AppCompatTextView distance;
    private Toolbar toolbar;
    private BroadcastReceiver timerReceiver;
    private BroadcastReceiver saveReceiver;
    private SharedPreferences sPref;

    private String title;
    private String toastRunBackPress;
    private String distanceText;
    private String kilometers;

    private Drawable upArrow;

    private static final String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String SAVED_DISTANCE = "SAVED_DISTANCE";
    private static final String SAVED_TIME = "SAVED_TIME";
    private static final String RUN_ACTIVITY_STATE = "RUN_ACTIVITY_STATE";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";

    private final AnimatorSet anim1 = (AnimatorSet) AnimatorInflater.loadAnimator(App.getInstance(), R.animator.flip_reg_log);
    private final AnimatorSet anim2 = (AnimatorSet) AnimatorInflater.loadAnimator(App.getInstance(), R.animator.flip_reg_log_exit);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        screenOne = (LinearLayout)findViewById(R.id.run_screen_1);
        screenTwo = (LinearLayout)findViewById(R.id.run_screen_2);
        screenThree = (LinearLayout)findViewById(R.id.run_screen_3);
        btnStart = (AppCompatButton)findViewById(R.id.start_btn);
        btnFinish = (AppCompatButton)findViewById(R.id.finish_btn);
        runTimer = (AppCompatTextView)findViewById(R.id.run_timer);
        runTimerResult = (AppCompatTextView)findViewById(R.id.run_timer_result);
        distance = (AppCompatTextView)findViewById(R.id.run_distance);
        toolbar = (Toolbar)findViewById(R.id.toolbar);

        title = getResources().getString(R.string.title_run);
        toastRunBackPress = getResources().getString(R.string.toast_run_back_press);
        distanceText = getResources().getString(R.string.full_distance_run);
        kilometers = getResources().getString(R.string.kilometers);

        upArrow = ContextCompat.getDrawable(App.getInstance().getApplicationContext(), R.drawable.ic_arrow_back_white_24dp);

        timerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String time = intent.getStringExtra(LocationService.TIME);
                runTimer.setText(time);
            }
        };

        saveReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = App.getInstance().getState().getAfterSaveRequest().getStatus();

                if (status.equals(STATUS_ERROR)) {
                    String code = App.getInstance().getState().getAfterSaveRequest().getCode();
                    Toast.makeText(App.getInstance(), code, Toast.LENGTH_SHORT).show();
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                RunDialogFragment.newInstance(null,
                        getResources().getString(R.string.no_internet_long), getResources().getString(R.string.btn_ok), null).show(fragmentManager, null);

            }
        };

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getInstance().getState().setRunFragmentScreen(2);
                anim1.setTarget(screenOne);
                anim2.setTarget(screenTwo);
                anim1.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selectScreen();
                        anim2.start();
                    }
                }, 250L);
                btnFinish.setEnabled(true);
                App.getInstance().getState().setRunFragmentScreen(2);
                Intent intent = new Intent(App.getInstance(), LocationService.class);
                startService(intent);
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(App.getInstance(), LocationService.class);
                stopService(intent);
                btnFinish.setEnabled(false);
                App.getInstance().getState().setRunFragmentScreen(3);
                selectScreen();
                anim1.setTarget(screenTwo);
                anim2.setTarget(screenThree);
                anim1.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        anim2.start();
                        App.getInstance().getState().setLastTimeOfRun(String.valueOf(runTimer.getText()));
                        runTimerResult.setText(App.getInstance().getState().getLastTimeOfRun());
                        String distanceInput = String.format(distanceText, (float)(App.getInstance().getState().getRunDistance()/1000f))+" "+ kilometers ;
                        distance.setText(distanceInput);
                    }
                }, 250L);
                btnFinish.setEnabled(false);
            }
        });
        //TODO
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(App.getInstance(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (App.getInstance().getState().isLocationRun()) {
            showToast();
        }else{
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (App.getInstance().getState().isLocationRun()) {
            showToast();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        selectScreen();
        loadState();
        LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(
                saveReceiver,
                new IntentFilter(SaveProvider.BROADCAST_SAVE_REQUEST)
        );
        LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(
                timerReceiver,
                new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );
    }

    @Override
    public void onPause(){
        super.onPause();
        saveState();
        LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(timerReceiver);
        LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(saveReceiver);
    }

    private void showToast(){
        Toast.makeText(getApplicationContext(), toastRunBackPress, Toast.LENGTH_SHORT).show();
    }

    private void selectScreen(){
        switch (App.getInstance().getState().getRunFragmentScreen()){
            case 1:
                screenOne.setVisibility(View.VISIBLE);
                screenTwo.setVisibility(View.GONE);
                screenThree.setVisibility(View.GONE);
                break;
            case 2:
                screenOne.setVisibility(View.GONE);
                screenTwo.setVisibility(View.VISIBLE);
                screenThree.setVisibility(View.GONE);
                break;
            case 3:
                screenOne.setVisibility(View.GONE);
                screenTwo.setVisibility(View.GONE);
                screenThree.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveState() {
        sPref = getSharedPreferences(RUN_ACTIVITY_STATE,MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_DISTANCE, distance.getText().toString());
        ed.putString(SAVED_TIME, runTimerResult.getText().toString());
        ed.apply();
    }

    private void loadState() {
        if(App.getInstance().getState().getRunFragmentScreen()==3) {
            sPref = getSharedPreferences(RUN_ACTIVITY_STATE,MODE_PRIVATE);
            String savedDistance = sPref.getString(SAVED_DISTANCE, "");
            String savedTime = sPref.getString(SAVED_TIME, "");
            distance.setText(savedDistance);
            runTimerResult.setText(savedTime);
        }
    }
}

