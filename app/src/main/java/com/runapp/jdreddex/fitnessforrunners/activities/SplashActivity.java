package com.runapp.jdreddex.fitnessforrunners.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;

/**
 * Created by JDReddex on 07.07.2016.
 */
public class SplashActivity extends AppCompatActivity {
    private ImageView splashLogo;
    private static final long SPLASH_TIME_OUT = 3000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashLogo = (ImageView)findViewById(R.id.splash_logo);

        ObjectAnimator anim = ObjectAnimator.ofFloat(splashLogo, "rotation", 0.0f, 500.0f);
        anim.setDuration(3000L);
        anim.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(App.getInstance().getState().isTokenExist()) {
                    Intent intent = new Intent(App.getInstance(), MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.animator.splash_exit, R.animator.splash_enter);
                    finish();
                }else{
                    Intent intent = new Intent(App.getInstance(), LogRegActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.animator.splash_exit, R.animator.splash_enter);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }
}


