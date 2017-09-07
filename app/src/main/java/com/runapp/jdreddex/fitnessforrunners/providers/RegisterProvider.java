package com.runapp.jdreddex.fitnessforrunners.providers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.models.AfterRegisterToken;
import com.runapp.jdreddex.fitnessforrunners.models.RegisterRequestData;

import java.util.concurrent.Callable;
import bolts.Continuation;
import bolts.Task;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterProvider {
    public static final String BROADCAST_REGISTER_REQUEST = "local:RegisterProvider.BROADCAST_REGISTER_REQUEST";
    private static final String BASE_URL = "http://pub.zame-dev.org/";

    private static Task<Void> registerRequestTask(final String email, final String password,final String firstName,final String lastName) {
        App.getInstance().getState().setIsTaskRun(true);

        return Task.callInBackground(new Callable<AfterRegisterToken>() {
            @Override
            public AfterRegisterToken call() throws Exception {
                RegisterRequestData registerRequestData = new RegisterRequestData();
                registerRequestData.setEmail(email);
                registerRequestData.setFirstName(firstName);
                registerRequestData.setLastName(lastName);
                registerRequestData.setPassword(password);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                IRequestProvider iRequestProvider = retrofit.create(IRequestProvider.class);

                AfterRegisterToken afterRegisterToken = null;

                Response<AfterRegisterToken> response = iRequestProvider.userRegister(registerRequestData).execute();
                if (response.isSuccessful()) {
                    afterRegisterToken = response.body();
                } else {
                    response.errorBody().close();
                }
                return afterRegisterToken;
            }
        }).onSuccess(new Continuation<AfterRegisterToken, Void>() {
            @Override
            public Void then(Task<AfterRegisterToken> task) throws Exception {
                App.getInstance().getState().setAfterRegisterToken(task.getResult());
                LocalBroadcastManager.getInstance(App.getInstance())
                        .sendBroadcast(new Intent(BROADCAST_REGISTER_REQUEST));
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

    public static void registerRequest(String email, String password, String firstName, String lastName) {
        if (!App.getInstance().getState().isTaskRun()) {
            registerRequestTask(email, password, firstName, lastName);
        }
    }
}
