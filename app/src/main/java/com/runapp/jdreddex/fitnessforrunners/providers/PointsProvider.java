package com.runapp.jdreddex.fitnessforrunners.providers;

import android.content.Intent;
import android.database.sqlite.SQLiteStatement;
import android.support.v4.content.LocalBroadcastManager;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.fragments.TrackListFragment;
import com.runapp.jdreddex.fitnessforrunners.models.AfterPointsRequest;
import com.runapp.jdreddex.fitnessforrunners.models.PointsRequestData;
import java.util.concurrent.Callable;
import bolts.Continuation;
import bolts.Task;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PointsProvider {
    public static final String BROADCAST_POINTS_REQUEST = "local:PointsProvider.BROADCAST_POINTS_REQUEST";
    private static final String BASE_URL = "http://pub.zame-dev.org/";
    private static final String STATUS_REQUEST_OK = "ok";

    private static Task<Void> pointsRequestTask(final String token, final String id ) {
        return Task.callInBackground(new Callable<AfterPointsRequest>() {
            @Override
            public AfterPointsRequest call() throws Exception {
                PointsRequestData pointsRequestData = new PointsRequestData();
                pointsRequestData.setToken(App.getInstance().getState().getToken());
                pointsRequestData.setId(id);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                IRequestProvider iRequestProvider = retrofit.create(IRequestProvider.class);

                AfterPointsRequest afterPointsRequest = null;

                Response<AfterPointsRequest> response = iRequestProvider.getPoints(pointsRequestData).execute();
                if (response.isSuccessful()) {
                    afterPointsRequest = response.body();
                } else {
                    response.errorBody().close();
                }
                return afterPointsRequest;
            }
        }).onSuccess(new Continuation<AfterPointsRequest, Void>() {
            @Override
            public Void then(Task<AfterPointsRequest> task) throws Exception {
                App.getInstance().getState().setAfterPointsRequest(task.getResult());
                if (App.getInstance().getState().getAfterPointsRequest().getStatus().equals(STATUS_REQUEST_OK)) {

                    int currentTrackPos = App.getInstance().getState().getTrackIdCounter();
                    int pointsSize = App.getInstance().getState().getAfterPointsRequest().getPoints().size();

                    String currentMyDBTrackID = App.getInstance().getState().getMyDBIdList().get(currentTrackPos);
                    if (0 < pointsSize) {
                        for (int i = 0; i < pointsSize; i++) {
                            SQLiteStatement statement = App.getInstance().getDb().compileStatement("INSERT INTO track_gps (trackId,lat,lng) VALUES (?,?,?)");
                            statement.bindString(1, currentMyDBTrackID);
                            statement.bindString(2, String.valueOf(App.getInstance().getState().getAfterPointsRequest().getPoints().get(i).getLat()));
                            statement.bindString(3, String.valueOf(App.getInstance().getState().getAfterPointsRequest().getPoints().get(i).getLng()));
                            try {
                                statement.executeInsert();
                            } finally {
                                statement.close();
                            }
                        }
                    } else {
                        return null;
                    }
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR)
                .continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        //TODO IF ERROR

                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).onSuccessTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {

                        App.getInstance().getState().incrementTrackIdCounter();
                        int currentTrackPos = App.getInstance().getState().getTrackIdCounter();
                        int tracksSize = App.getInstance().getState().getAfterTracksRequest().getTracks().size();

                        App.getInstance().getState().incrementTrackIdCounter();

                        if (currentTrackPos < tracksSize) {
                            String id = App.getInstance().getState().getAfterTracksRequest().getTracks().get(currentTrackPos).getId();
                            return PointsProvider.pointsRequest(
                                    App.getInstance().getState().getToken(),
                                    id);
                        }
                        App.getInstance().getState().setIsSynchronizationRun(false);
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).onSuccess(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        if (!App.getInstance().getState().isSynchronizationRun()) {
                            LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(new Intent(TrackListFragment.BROADCAST_FINISH_SYNCHRONIZE));
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }


    public static Task<Void> pointsRequest(String token, String id) {
        pointsRequestTask(token,id);
        return null;
    }
}

