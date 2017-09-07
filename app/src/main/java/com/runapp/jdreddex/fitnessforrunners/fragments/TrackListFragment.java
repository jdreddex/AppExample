package com.runapp.jdreddex.fitnessforrunners.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.adapters.RecyclerViewCursorAdapter;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;
import com.runapp.jdreddex.fitnessforrunners.loaders.SimpleCursorLoader;
import com.runapp.jdreddex.fitnessforrunners.adapters.TrackAdapter;
import com.runapp.jdreddex.fitnessforrunners.services.SynchronizeService;


/**
 * Created by JDReddex on 08.07.2016.
 */
public class TrackListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TrackAdapter.TrackViewHolder.ClickListener{
    public View view;

    private static final int LOADER_ID = 0;
    private RecyclerView trackRecyclerView;
    private RecyclerViewCursorAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public FloatingActionButton fabRun;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public ITrackListListener trackListListener;
    private ProgressDialog progressDialog;
    private BroadcastReceiver finishReceiver;

    public String synchronization;
    public String trackSaved;
    public static final String TAG_MAIN = "TAG_MAIN";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = "error";
    public static final String BROADCAST_FINISH_SYNCHRONIZE = "local:SynchronizationService.BROADCAST_FINISH_SYNCHRONIZE";

    public interface ITrackListListener{
        void fabRunOnClick();
        void trackListOnItemClicked(long position);
        void trackListOnCheckedChange(long position, int favFlag);
    }

    public TrackListFragment() {}

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_list, container, false);
        trackRecyclerView = (RecyclerView)view.findViewById(R.id.track_recycler_view);
        fabRun = (FloatingActionButton) view.findViewById(R.id.fab_run);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);

        synchronization = getResources().getString(R.string.synchronization);
        trackSaved = getResources().getString(R.string.track_saved);

        trackRecyclerView.setHasFixedSize(true);
        trackRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mLayoutManager = new LinearLayoutManager(App.getInstance());
        trackRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new TrackAdapter(App.getInstance(), null, this);
        getLoaderManager().initLoader(LOADER_ID, null, this);

        trackRecyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!App.getInstance().getState().isSynchronizationRun()) {
                    App.getInstance().getState().setIsSynchronizationRun(true);
                    synchronize();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        fabRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                trackListListener.fabRunOnClick();
            }
        });

        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                App.getInstance().getState().setIsSynchronizationRun(false);
                    Intent syncIntent = new Intent(App.getInstance(), SynchronizeService.class);
                    App.getInstance().stopService(syncIntent);
                    App.getInstance().createState();
                    adapter.swapCursor(newCursor());

                dismissProgressDialog();
            }
        };

        return view;
    }

    static class TrackListLoader extends SimpleCursorLoader {
        public TrackListLoader(Context context) {
            super(context);
        }
        @Override
        public Cursor loadInBackground() {
            return App.getInstance().getDb().rawQuery("SELECT track._id AS _id,track.time AS time, track.startTime AS startTime, track.distance AS distance, favorite FROM track ORDER BY _id DESC", null);
        }
    }
    public Cursor newCursor(){
        return App.getInstance().getDb().rawQuery("SELECT track._id AS _id,track.time AS time, track.startTime AS startTime, track.distance AS distance, favorite FROM track ORDER BY _id DESC", null);
    }

    @Override
    public void onItemClicked(int position) {
        trackListListener.trackListOnItemClicked(adapter.getItemId(position));
    }
    @Override
    public void onCheckedChange(int position, int fav) {
        trackListListener.trackListOnCheckedChange(adapter.getItemId(position), fav);
        adapter.swapCursor(newCursor());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new TrackListLoader(App.getInstance());
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onStart(){
        super.onStart();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_track, TAG_MAIN);
        try {
            trackListListener = (ITrackListListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ITrackListListener");
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_track, TAG_MAIN);
        adapter.swapCursor(newCursor());
        LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(
                finishReceiver,
                new IntentFilter(BROADCAST_FINISH_SYNCHRONIZE)
        );
//        if (App.getInstance().getState().isFirstStart()){
//            if (showProgressDialog()) {
//                synchronize();
//            }
//            App.getInstance().getState().setIsFirstStart(false);
//        }
    }

    @Override
    public void onPause(){
        LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(finishReceiver);
        super.onPause();
    }

    private void synchronize(){
        Toast.makeText(App.getInstance(), synchronization, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(App.getInstance(), SynchronizeService.class);
        App.getInstance().startService(intent);
    }
    private boolean showProgressDialog() {
        if (progressDialog != null) {
            return false;
        }
        progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading), true, false);
        return true;
    }
    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
//if (App.getInstance().getState() == null) {
//        finish();
//        startActivity(this, StartActivity.class); // Если перезапуск - то начнём всё с первого экрана
//        }
