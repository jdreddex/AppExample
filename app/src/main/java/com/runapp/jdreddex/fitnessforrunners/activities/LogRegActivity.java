package com.runapp.jdreddex.fitnessforrunners.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.widget.ScrollView;
import android.support.v7.widget.AppCompatTextView;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.providers.LoginProvider;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.providers.RegisterProvider;

public class LogRegActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ScrollView registerScreen;
    private AppCompatEditText registerEmail;
    private AppCompatEditText registerFirstName;
    private AppCompatEditText registerLastName;
    private AppCompatEditText registerPass;
    private AppCompatEditText registerPassRepeat;
    private AppCompatButton registerBtn;
    private AppCompatTextView changerLogin;

    private ScrollView loginScreen;
    private AppCompatEditText loginEmail;
    private AppCompatEditText loginPass;
    private AppCompatButton loginBtn;
    private AppCompatTextView changerRegister;
    private ProgressDialog progressDialog;
    private AppCompatTextView registerViewError;
    private AppCompatTextView loginViewError;

    private String fieldEmpty;
    private String errorShort;
    private String errorPassCheck;
    private String passStr;

    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";

    private static final String LOG_REG_ACTIVITY_STATE = "LOG_REG_ACTIVITY_STATE";
    private static final String LOG_SCREEN = "LOG_SCREEN";
    private static final String LOG_EMAIL = "LOG_EMAIL";
    private static final String REG_SCREEN = "REG_SCREEN";
    private static final String REG_EMAIL = "REG_EMAIL";
    private static final String REG_FIRST_NAME = "REG_FIRST_NAME";
    private static final String REG_LAST_NAME = "REG_LAST_NAME";

    private static final int MIN_PASSWORD_LENGTH = 5;

    private BroadcastReceiver registerReceiver;
    private BroadcastReceiver loginReceiver;

    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        toolbar = (Toolbar)findViewById(R.id.toolbar);

        registerScreen = (ScrollView) findViewById(R.id.register_screen);
        registerEmail = (AppCompatEditText) findViewById(R.id.register_email);
        registerFirstName = (AppCompatEditText) findViewById(R.id.register_first_name);
        registerLastName = (AppCompatEditText) findViewById(R.id.register_last_name);
        registerPass = (AppCompatEditText) findViewById(R.id.register_pass);
        registerPassRepeat = (AppCompatEditText) findViewById(R.id.register_pass_repeat);
        registerBtn = (AppCompatButton) findViewById(R.id.register_btn);
        changerLogin = (AppCompatTextView) findViewById(R.id.register_btn_login);

        loginScreen = (ScrollView) findViewById(R.id.login_screen);
        loginEmail = (AppCompatEditText) findViewById(R.id.login_email);
        loginPass = (AppCompatEditText) findViewById(R.id.login_pass);
        loginBtn = (AppCompatButton) findViewById(R.id.login_btn);
        changerRegister = (AppCompatTextView) findViewById(R.id.login_btn_register);
        registerViewError = (AppCompatTextView) findViewById(R.id.register_error_hint);
        loginViewError = (AppCompatTextView) findViewById(R.id.login_error_hint);

        fieldEmpty = getString(R.string.field_empty);
        errorShort = getString(R.string.error_short);
        errorPassCheck = getString(R.string.error_pass_check);

        passStr = getString(R.string.password);

        changerLogin.setPaintFlags(changerLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        changerRegister.setPaintFlags( Paint.UNDERLINE_TEXT_FLAG);

        loginBtn.setEnabled(true);
        registerBtn.setEnabled(true);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tryLogin()){
                    if (showProgressDialog()) {
                        LoginProvider.loginRequest(loginEmail.getText().toString().trim(),
                                loginPass.getText().toString().trim());
                    }
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tryRegister()){
                    if (showProgressDialog()) {
                        RegisterProvider.registerRequest(registerEmail.getText().toString().trim(),
                                registerPass.getText().toString().trim(),
                                registerFirstName.getText().toString().trim(),
                                registerLastName.getText().toString().trim());
                    }
                }
            }
        });

        registerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = App.getInstance().getState().getAfterRegisterToken().getStatus();
                if (status.equals(STATUS_OK)) {
                    deleteState();
                    App.getInstance().getState().setToken(App.getInstance().getState().getAfterRegisterToken().getToken());
                    Intent registerOk = new Intent(App.getInstance(), MainActivity.class);
                    startActivity(registerOk);
                    overridePendingTransition(R.animator.splash_exit, R.animator.splash_enter);
                    finish();
                } else if (status.equals(STATUS_ERROR)) {
                    registerViewError.setText(App.getInstance().getState().getAfterRegisterToken().getCode());
                }
                dismissProgressDialog();
                progressDialog = null;
            }
        };

        loginReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = App.getInstance().getState().getAfterLoginRequestData().getStatus();
                if (status.equals(STATUS_OK)) {
                    deleteState();
                    App.getInstance().getState().setToken(App.getInstance().getState().getAfterLoginRequestData().getToken());
                    Intent loginOk = new Intent(App.getInstance(), MainActivity.class);
                    startActivity(loginOk);
                    overridePendingTransition(R.animator.splash_exit, R.animator.splash_enter);
                    finish();
                } else if (status.equals(STATUS_ERROR)) {
                    loginViewError.setText(App.getInstance().getState().getAfterLoginRequestData().getCode());
                }
                dismissProgressDialog();
                progressDialog = null;
            }
        };

        changerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changerLogin.setEnabled(false);

                final AnimatorSet anim1 = (AnimatorSet) AnimatorInflater.loadAnimator(App.getInstance(),
                        R.animator.flip_reg_log);
                anim1.setTarget(registerScreen);

                final AnimatorSet anim2 = (AnimatorSet) AnimatorInflater.loadAnimator(App.getInstance(),
                        R.animator.flip_reg_log_exit);
                anim2.setTarget(loginScreen);

                anim1.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        registerScreen.setVisibility(View.GONE);
                        loginScreen.setVisibility(View.VISIBLE);
                        anim2.start();

                    }
                }, 250L);
                changerRegister.setEnabled(true);
            }
        });

        changerRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changerRegister.setEnabled(false);
                final AnimatorSet anim1 = (AnimatorSet) AnimatorInflater.loadAnimator(App.getInstance(),
                        R.animator.flip_reg_log);
                anim1.setTarget(loginScreen);

                final AnimatorSet anim2 = (AnimatorSet) AnimatorInflater.loadAnimator(App.getInstance(),
                        R.animator.flip_reg_log_exit);
                anim2.setTarget(registerScreen);

                anim1.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loginScreen.setVisibility(View.GONE);
                        registerScreen.setVisibility(View.VISIBLE);
                        anim2.start();
                        changerLogin.setEnabled(true);
                    }
                }, 250L);
            }
        });

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (App.getInstance().getState() == null) {
            App.getInstance().createState();
        }
        if (App.getInstance().getState().isTaskRun()) {
            showProgressDialog();
        }
        loadState();
        LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(
                registerReceiver,
                new IntentFilter(RegisterProvider.BROADCAST_REGISTER_REQUEST)
        );
        LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(
                loginReceiver,
                new IntentFilter(LoginProvider.BROADCAST_LOGIN_REQUEST)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(registerReceiver);
        LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(loginReceiver);
        saveState();
        dismissProgressDialog();
    }

    private boolean showProgressDialog() {
        if (progressDialog != null) {
            return false;
        }
        progressDialog = ProgressDialog.show(this,"",getString(R.string.loading),true,false);
        return true;
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public boolean tryLogin(){
        loginEmail.setError(null);
        loginPass.setError(null);

        String emailText = loginEmail.getText().toString().trim();
        String pass = loginPass.getText().toString().trim();

        boolean hasError = false;

        if (emailText.length() == 0) {
            loginEmail.setError(getString(R.string.email_empty));
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            loginEmail.setError(getString(R.string.wrong_email));
            hasError = true;
        }

        if (pass.length()==0) {
            loginPass.setError(fieldEmpty);
            hasError = true;
        } else if (pass.length() < MIN_PASSWORD_LENGTH) {
            loginPass.setError(String.format(errorShort,passStr, MIN_PASSWORD_LENGTH));
            hasError = true;
        }
        return hasError;
    }
    public boolean tryRegister(){
        registerEmail.setError(null);
        registerFirstName.setError(null);
        registerLastName.setError(null);
        registerPass.setError(null);
        registerPassRepeat.setError(null);

        String emailText = registerEmail.getText().toString().trim();
        String firstName = registerFirstName.getText().toString().trim();
        String lastName = registerLastName.getText().toString().trim();
        String pass = registerPass.getText().toString().trim();
        String passRepeat = registerPassRepeat.getText().toString().trim();

        boolean hasError = false;

        if (emailText.length() == 0) {
            registerEmail.setError(getString(R.string.email_empty));
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            registerEmail.setError(getString(R.string.wrong_email));
            hasError = true;
        }

        if (pass.length()==0) {
            registerPass.setError(fieldEmpty);
            hasError = true;
        } else if (pass.length() < MIN_PASSWORD_LENGTH) {
            registerPass.setError(String.format(errorShort,passStr, MIN_PASSWORD_LENGTH));
            hasError = true;
        }

        if (passRepeat.length()==0) {
            registerPassRepeat.setError(fieldEmpty);
            hasError = true;
        } else if (!pass.equals(passRepeat)) {
            registerPassRepeat.setError(errorPassCheck);
            hasError = true;
        }

        if (firstName.length()==0) {
            registerFirstName.setError(fieldEmpty);
            hasError = true;
        }
        if (lastName.length()==0) {
            registerLastName.setError(fieldEmpty);
            hasError = true;
        }
        return hasError;
    }
    void saveState() {
        sPref = getSharedPreferences(LOG_REG_ACTIVITY_STATE,MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(LOG_SCREEN, loginScreen.getVisibility());
        ed.putString(LOG_EMAIL, loginEmail.getText().toString());

        ed.putInt(REG_SCREEN, registerScreen.getVisibility());
        ed.putString(REG_EMAIL, registerEmail.getText().toString());
        ed.putString(REG_FIRST_NAME, registerFirstName.getText().toString());
        ed.putString(REG_LAST_NAME, registerLastName.getText().toString());
        ed.apply();
    }

    void loadState() {
        if(App.getInstance().getState().getRunFragmentScreen()==3) {
            sPref = getSharedPreferences(LOG_REG_ACTIVITY_STATE,MODE_PRIVATE);
            int logScreenState = sPref.getInt(LOG_SCREEN, 0);
            String logEmailState = sPref.getString(LOG_EMAIL, "");

            int regScreenState = sPref.getInt(REG_SCREEN, 8);
            String regEmailState = sPref.getString(REG_EMAIL, "");
            String regFirstNameState = sPref.getString(REG_FIRST_NAME, "");
            String regLastNameState = sPref.getString(REG_LAST_NAME, "");

            if (logScreenState == 0){
                loginScreen.setVisibility(View.VISIBLE);
            }else{
                loginScreen.setVisibility(View.GONE);
            }
            loginEmail.setText(logEmailState);

            if (regScreenState == 0){
                registerScreen.setVisibility(View.VISIBLE);
            }else{
                registerScreen.setVisibility(View.GONE);
            }
            registerEmail.setText(regEmailState);
            registerFirstName.setText(regFirstNameState);
            registerLastName.setText(regLastNameState);
        }
    }
    void deleteState() {
        sPref = getSharedPreferences(LOG_REG_ACTIVITY_STATE,MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.remove(LOG_EMAIL);
        ed.remove(LOG_SCREEN);
        ed.remove(REG_SCREEN);
        ed.remove(REG_EMAIL);
        ed.remove(REG_FIRST_NAME);
        ed.remove(REG_LAST_NAME);
        ed.apply();
    }
}
