package com.runapp.jdreddex.fitnessforrunners.providers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.models.AfterLoginRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.LoginRequestData;

import java.util.concurrent.Callable;
import bolts.Continuation;
import bolts.Task;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by JDReddex on 25.07.2016.
 */
public class LoginProvider {
    public static final String BROADCAST_LOGIN_REQUEST = "local:LoginProvider.BROADCAST_LOGIN_REQUEST";
    private static final String BASE_URL = "http://pub.zame-dev.org/";

    private static Task<Void> loginRequestTask(final String email,
                                               final String password) {
        App.getInstance().getState().setIsTaskRun(true);

        return Task.callInBackground(new Callable<AfterLoginRequestData>() {
            @Override
            public AfterLoginRequestData call() throws Exception {
                LoginRequestData loginRequestData = new LoginRequestData();
                loginRequestData.setEmail(email);
                loginRequestData.setPassword(password);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                IRequestProvider iRequestProvider = retrofit.create(IRequestProvider.class);

                AfterLoginRequestData afterLoginRequestData = null;

                Response<AfterLoginRequestData> response = iRequestProvider.userLogin(loginRequestData).execute();
                if (response.isSuccessful()) {
                    afterLoginRequestData = response.body();
                } else {
                    response.errorBody().close();
                }
                return afterLoginRequestData;
            }
        }).onSuccess(new Continuation<AfterLoginRequestData, Void>() {
            @Override
            public Void then(Task<AfterLoginRequestData> task) throws Exception {
                App.getInstance().getState().setAfterLoginRequestData(task.getResult());
                App.getInstance().getState().setToken(App.getInstance().getState().getAfterLoginRequestData().getToken());
                LocalBroadcastManager.getInstance(App.getInstance())
                        .sendBroadcast(new Intent(BROADCAST_LOGIN_REQUEST));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                App.getInstance().getState().setIsTaskRun(false);
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public static void loginRequest(String email, String password) {
            loginRequestTask(email, password);
    }
}
