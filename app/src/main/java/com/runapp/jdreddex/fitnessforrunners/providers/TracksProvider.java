package com.runapp.jdreddex.fitnessforrunners.providers;
import android.content.Intent;
import android.database.sqlite.SQLiteStatement;
import android.support.v4.content.LocalBroadcastManager;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.fragments.TrackListFragment;
import com.runapp.jdreddex.fitnessforrunners.models.AfterTracksRequest;
import com.runapp.jdreddex.fitnessforrunners.models.Track;
import com.runapp.jdreddex.fitnessforrunners.models.TracksRequestData;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TracksProvider {
    public static final String BROADCAST_TRACKS_REQUEST = "local:TracksProvider.BROADCAST_TRACKS_REQUEST";


    private static final String BASE_URL = "http://pub.zame-dev.org/";
    private static final String STATUS_REQUEST_OK = "ok";

    private static Task<Void> tracksRequestTask(final String token) {

        return Task.callInBackground(new Callable<AfterTracksRequest>() {
            @Override
            public AfterTracksRequest call() throws Exception {
                TracksRequestData tracksRequestData = new TracksRequestData();
                tracksRequestData.setToken(token);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                IRequestProvider iRequestProvider = retrofit.create(IRequestProvider.class);

                AfterTracksRequest afterTracksRequest = null;

                Response<AfterTracksRequest> response = iRequestProvider.getTracks(tracksRequestData).execute();
                if (response.isSuccessful()) {
                    afterTracksRequest = response.body();
                } else {
                    response.errorBody().close();
                }
                return afterTracksRequest;
            }
        }).onSuccess(new Continuation<AfterTracksRequest, Void>() {
            @Override
            public Void then(Task<AfterTracksRequest> task) throws Exception {
                App.getInstance().getState().setAfterTracksRequest(task.getResult());

                return null;
            }
        }, Task.UI_THREAD_EXECUTOR)
                .continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).onSuccessTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        if (App.getInstance().getState().getAfterTracksRequest().getStatus().equals(STATUS_REQUEST_OK)) {

                            int currentTrackPos = App.getInstance().getState().getTrackIdCounter();
                            int inAppTracksSize = App.getInstance().getState().getBeginsAtInAppDbList().size();
                            int serverTracksSize = App.getInstance().getState().getAfterTracksRequest().getTracks().size();

                            String inAppBeginsAt;
                            String beginsAt;

                            if (App.getInstance().getState().getTrackIdCounter() < serverTracksSize) {
                                for (int i = 0; i < serverTracksSize; i++) {
                                    for (int j = 0; j < inAppTracksSize; j++) {
                                        inAppBeginsAt = String.valueOf(App.getInstance().getState().getBeginsAtInAppDbList().get(j));
                                        beginsAt = App.getInstance().getState().getAfterTracksRequest().getTracks().get(i).getBeginsAt();
                                        if (inAppBeginsAt.equals(beginsAt)) {
                                            App.getInstance().getState().getItemsToNotSyncs().add(App.getInstance().getState().getAfterTracksRequest().getTracks().get(i).getBeginsAt());
                                        }
                                    }
                                }

                                if (App.getInstance().getState().getItemsToNotSyncs().size() > 0) {
                                    List<String> itemsToNotSyncs = App.getInstance().getState().getItemsToNotSyncs();
                                    List<Track> serverTracks = App.getInstance().getState().getAfterTracksRequest().getTracks();

                                    for (int i = 0; i < serverTracks.size(); i++) {
                                        Iterator<Track> iter = App.getInstance().getState().getAfterTracksRequest().getTracks().iterator();
                                        while (iter.hasNext()) {
                                            Track track = iter.next();

                                            if (track.getBeginsAt().equals(itemsToNotSyncs.get(i))) {
                                                iter.remove();
                                            }
                                        }
                                    }
                                }
                                for (int i = 0; i < App.getInstance().getState().getAfterTracksRequest().getTracks().size(); i++) {
                                    SQLiteStatement statement = App.getInstance().getDb().compileStatement("INSERT INTO track (startTime,time,distance,synchronized) VALUES (?,?,?,?)");
                                    statement.bindString(1, App.getInstance().getState().getAfterTracksRequest().getTracks().get(i).getBeginsAt());
                                    statement.bindString(2, App.getInstance().getState().getAfterTracksRequest().getTracks().get(i).getTime());
                                    statement.bindString(3, App.getInstance().getState().getAfterTracksRequest().getTracks().get(i).getDistance());
                                    statement.bindString(4, App.getInstance().getState().getAfterTracksRequest().getTracks().get(i).getId());
                                    try {
                                        long lastId = statement.executeInsert();
                                        App.getInstance().getState().addToMyDBIdList(String.valueOf(lastId));
                                    } finally {
                                        statement.close();
                                    }
                                }
                                if (App.getInstance().getState().getAfterTracksRequest().getTracks().size() != 0) {
                                    String id = App.getInstance().getState().getAfterTracksRequest().getTracks().get(currentTrackPos).getId();
                                    return PointsProvider.pointsRequest(
                                            App.getInstance().getState().getToken(),
                                            id
                                    );
                                } else {
                                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcastSync(new Intent(TrackListFragment.BROADCAST_FINISH_SYNCHRONIZE));
                                }
                            }
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        if (App.getInstance().getState().getAfterTracksRequest().getTracks().size() == 0) {
                            LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(new Intent(TrackListFragment.BROADCAST_FINISH_SYNCHRONIZE));
                        }
                        return null;
                    }
                });
    }

    public static Task<Void> tracksRequest(String token) {
        return tracksRequestTask(token);
    }
}
