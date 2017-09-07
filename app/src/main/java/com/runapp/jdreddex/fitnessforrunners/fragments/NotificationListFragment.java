package com.runapp.jdreddex.fitnessforrunners.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.receivers.NotificationReceiver;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;
import com.runapp.jdreddex.fitnessforrunners.adapters.NotificationAdapter;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.adapters.RecyclerViewCursorAdapter;
import com.runapp.jdreddex.fitnessforrunners.loaders.SimpleCursorLoader;

/**
 * Created by JDReddex on 11.07.2016.
 */
public class NotificationListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        NotificationAdapter.NotificationViewHolder.ClickListener,
        NotificationReceiver.INotificationReceiverListener{

    public View view;
    public FloatingActionButton fab;
    public INotificationListListener notificationListListener;
    private static final int LOADER_ID = 0;
    private RecyclerView trackRecyclerView;
    private RecyclerViewCursorAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;
    public static final String TAG_NOTIFICATION = "TAG_NOTIFICATION";

    public interface INotificationListListener{
        void fabOnClick();
        void notificationBtnEdit(long _id);
        void notificationBtnRemove(long _id);
    }

    public NotificationListFragment() {}

    public static NotificationListFragment newInstance() {
        return new NotificationListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationReceiver.setListener(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification_list, container, false);
        trackRecyclerView = (RecyclerView)view.findViewById(R.id.notification_recycler_view);
        fab = (FloatingActionButton)  view.findViewById(R.id.fab);


        mLayoutManager = new LinearLayoutManager(App.getInstance());
        adapter = new NotificationAdapter(App.getInstance(), null, this);

        trackRecyclerView.setHasFixedSize(true);
        trackRecyclerView.setItemAnimator(new DefaultItemAnimator());
        trackRecyclerView.setLayoutManager(mLayoutManager);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        trackRecyclerView.setAdapter(adapter);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                notificationListListener.fabOnClick();
            }
        });
        return view;
    }

    public Cursor newCursor(){
        return App.getInstance().getDb().rawQuery("SELECT reminder._id AS _id,reminder.time AS time FROM reminder ORDER BY time", null);

    }

    static class TrackListLoader extends SimpleCursorLoader {
        public TrackListLoader(Context context) {
            super(context);
        }
        @Override
        public Cursor loadInBackground() {
            return App.getInstance().getDb().rawQuery("SELECT reminder._id AS _id,reminder.time AS time FROM reminder ORDER BY time", null);
        }
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
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_notification, TAG_NOTIFICATION);
        try {
            notificationListListener = (INotificationListListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement INotificationListListener");
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_notification, TAG_NOTIFICATION);
        adapter.swapCursor(newCursor());
    }

    @Override
    public void notificationReceiverUpdateList(){
        adapter.swapCursor(newCursor());
        int count = adapter.getSelectedItemCount();
        if (count == 1) {
            actionMode.finish();
        }
    }

    @Override
    public void onLongClickNotification(int position) {
        if (actionMode == null) {
            actionMode = getActivity().startActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.btn_delete:
                    notificationListListener.notificationBtnRemove(adapter.removeItem());
                    adapter.swapCursor(newCursor());
                    mode.finish();
                    return true;
                case R.id.btn_edit:
                    notificationListListener.notificationBtnEdit(adapter.getSelectedItemId());
                    mode.finish();
                    return true;
                case R.id.home:
                    notificationListListener.notificationBtnEdit(adapter.getSelectedItemId());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
        }
    }
}
