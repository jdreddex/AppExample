package com.runapp.jdreddex.fitnessforrunners.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.adapters.RecyclerViewCursorAdapter;
import com.runapp.jdreddex.fitnessforrunners.adapters.TrackFavoriteAdapter;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;
import com.runapp.jdreddex.fitnessforrunners.loaders.SimpleCursorLoader;

public class TrackListFavoriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TrackFavoriteAdapter.TrackFavoriteViewHolder.ClickListener {

    public View view;
    private static final int LOADER_ID = 0;
    private RecyclerView trackRecyclerView;
    private RecyclerViewCursorAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static final String TAG_FAVORITE = "TAG_FAVORITE";
    public ITrackFavoriteListListener trackFavoriteListListener;

    public interface ITrackFavoriteListListener{
        void trackFavoriteListOnItemClick(long position);
    }

    public TrackListFavoriteFragment() {}

    public static TrackListFavoriteFragment newInstance() {
        return new TrackListFavoriteFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_favorite_list, container, false);
        trackRecyclerView = (RecyclerView)view.findViewById(R.id.track_favorite_recycler_view);

        trackRecyclerView.setHasFixedSize(true);
        trackRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mLayoutManager = new LinearLayoutManager(App.getInstance());
        trackRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new TrackFavoriteAdapter(App.getInstance(), null, this);
        getLoaderManager().initLoader(LOADER_ID, null, this);

        trackRecyclerView.setAdapter(adapter);
        return view;
    }

    static class TrackListLoader extends SimpleCursorLoader {
        public TrackListLoader(Context context) {
            super(context);
        }
        @Override
        public Cursor loadInBackground() {
            return App.getInstance().getDb().rawQuery("SELECT track._id AS _id,track.time AS time, track.startTime AS startTime, track.distance AS distance, favorite FROM track WHERE favorite = 1 ORDER BY _id DESC", null);
        }
    }

    @Override
    public void onItemClicked(int position) {
        trackFavoriteListListener.trackFavoriteListOnItemClick(adapter.getItemId(position));
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
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_track_favorite, TAG_FAVORITE);
        try {
            trackFavoriteListListener = (ITrackFavoriteListListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement ITrackFavoriteListListener");
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_track_favorite, TAG_FAVORITE);
    }
}
