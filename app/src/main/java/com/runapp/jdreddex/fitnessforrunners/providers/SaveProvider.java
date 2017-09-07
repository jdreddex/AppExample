package com.runapp.jdreddex.fitnessforrunners.providers;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.v4.content.LocalBroadcastManager;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.fragments.TrackListFragment;
import com.runapp.jdreddex.fitnessforrunners.models.AfterSaveRequest;
import com.runapp.jdreddex.fitnessforrunners.models.SaveRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.TrackToSaveRequest;

import java.util.concurrent.Callable;
import bolts.Continuation;
import bolts.Task;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by JDReddex on 25.07.2016.
 */
public class SaveProvider {
    public static final String BROADCAST_SAVE_REQUEST = "local:SaveProvider.BROADCAST_SAVE_REQUEST";
    public static final String BROADCAST_TRACK_UPLOAD_ERROR = "local:SaveProvider.BROADCAST_TRACK_UPLOAD_ERROR";
    public static final String BROADCAST_INTERNET_ERROR = "local:SaveProvider.BROADCAST_INTERNET_ERROR";
    private static final String BASE_URL = "http://pub.zame-dev.org/";
    private static final String STATUS_REQUEST_OK = "ok";
    private static final String STATUS_REQUEST_ERROR = "error";

    private static Task<Void> saveRequestTask(final TrackToSaveRequest trackToSaveRequest) {
        App.getInstance().getState().setIsSynchronizationRun(true);

        return Task.callInBackground(new Callable<AfterSaveRequest>() {
            @Override
            public AfterSaveRequest call() throws Exception {
                SaveRequestData dataToSend = new SaveRequestData();
                dataToSend.setToken(App.getInstance().getState().getToken());
                dataToSend.setId(trackToSaveRequest.getId());
                dataToSend.setTime(trackToSaveRequest.getTime());
                dataToSend.setBeginsAt(trackToSaveRequest.getBeginsAt());
                dataToSend.setPoints(trackToSaveRequest.getPoints());
                dataToSend.setDistance(trackToSaveRequest.getDistance());

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                IRequestProvider iRequestProvider = retrofit.create(IRequestProvider.class);

                AfterSaveRequest afterSaveRequest = null;

                Response<AfterSaveRequest> response = iRequestProvider.saveTrack(dataToSend).execute();
                if (response.isSuccessful()) {
                    afterSaveRequest = response.body();
                } else {
                    response.errorBody().close();
                }
                App.getInstance().getState().setAfterSaveRequest(afterSaveRequest);
                return afterSaveRequest;
            }
        }).onSuccess(new Continuation<AfterSaveRequest, AfterSaveRequest>() {
            @Override
            public AfterSaveRequest then(Task<AfterSaveRequest> task) throws Exception {
                App.getInstance().getState().setAfterSaveRequest(task.getResult());
                if (task.getResult().getStatus().equals(STATUS_REQUEST_OK)) {
                    SQLiteStatement statement = App.getInstance().getDb().compileStatement("UPDATE track SET synchronized = ? WHERE _id = ?");
                    statement.bindString(1, task.getResult().getId());
                    statement.bindString(2, String.valueOf(App.getInstance().getState().getTrackListToSend().get(App.getInstance().getState().getCurrentTrackToSend()).getMyId()));
                    try {
                        statement.executeUpdateDelete();
                    } finally {
                        statement.close();
                    }

                } else {
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcastSync(new Intent(BROADCAST_TRACK_UPLOAD_ERROR));
                }
                App.getInstance().getState().setAfterSaveRequest(task.getResult());
                return task.getResult();
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<AfterSaveRequest, AfterSaveRequest>() {
            @Override
            public AfterSaveRequest then(Task<AfterSaveRequest> task) throws Exception {
                if (task.getError() != null) {
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcastSync(new Intent(BROADCAST_INTERNET_ERROR));
                }
                return task.getResult();
            }
        }, Task.UI_THREAD_EXECUTOR).onSuccessTask(new Continuation<AfterSaveRequest, Task<Void>>() {
            @Override
            public Task<Void> then(Task<AfterSaveRequest> task) throws Exception {
                if (task.getResult().getStatus().equals(STATUS_REQUEST_OK)) {
                    App.getInstance().getState().getTrackListToSend().remove(App.getInstance().getState().getCurrentTrackToSend());

                    Cursor beginAtCursor = App.getInstance().getDb().rawQuery("SELECT startTime FROM track",null);
                    if (beginAtCursor != null) {
                        if (beginAtCursor.moveToFirst()) {
                            int startTimeIndex = beginAtCursor.getColumnIndexOrThrow("startTime");
                            do {
                                String startTime = beginAtCursor.getString(startTimeIndex);
                                App.getInstance().getState().getBeginsAtInAppDbList().add(startTime);

                            } while (beginAtCursor.moveToNext());
                        }
                        beginAtCursor.close();
                    }

                    if (App.getInstance().getState().getTrackListToSend().size() != 0) {
                        return saveRequest(App.getInstance().getState().getTrackListToSend().get(App.getInstance().getState().getCurrentTrackToSend()));
                    } else {
                        return TracksProvider.tracksRequest(App.getInstance().getState().getToken());
                    }
                } else {
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcastSync(new Intent(TrackListFragment.BROADCAST_FINISH_SYNCHRONIZE));

                    return null;
                }
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public static Task<Void> saveRequest(TrackToSaveRequest track) {
        return saveRequestTask(track);
    }
}
