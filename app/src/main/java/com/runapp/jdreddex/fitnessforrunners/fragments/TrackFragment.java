package com.runapp.jdreddex.fitnessforrunners.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.runapp.jdreddex.fitnessforrunners.App;
import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;
import java.util.ArrayList;


public class TrackFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap map;
    public View view;
    private android.support.v7.widget.AppCompatTextView fragmentTrackDistance;
    private android.support.v7.widget.AppCompatTextView fragmentTrack_time;

    private String distance;
    private long time;
    private long distanceFromCursor;
    private long _id;
    private String starMarker;
    private String finishMarker;


    public static final String TRACK_FRAGMENT_TAG = "TRACK_FRAGMENT_TAG";

    public long get_id() {
        return _id;
    }
    public void set_id(long _id) {
        this._id = _id;
    }

    public TrackFragment() {}

    public static TrackFragment newInstance() {
        return new TrackFragment();
    }

    private ArrayList<LatLng> latLngList = new ArrayList<>();
    private PolylineOptions trackPolyline = new PolylineOptions()
            .color(ContextCompat.getColor(App.getInstance(), R.color.colorPrimary));

    private void addToLatLngList(double lat, double lng){
        LatLng latLng = new LatLng(lat, lng);
        latLngList.add(latLng);
        trackPolyline.add(latLng);
    }
    public ArrayList<LatLng> getLatLngList(){
        return latLngList;
    }
    public PolylineOptions getTrackPolyline(){
        return trackPolyline;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        fragmentTrackDistance = (android.support.v7.widget.AppCompatTextView)view.findViewById(R.id.fragment_track_distance);
        fragmentTrack_time = (android.support.v7.widget.AppCompatTextView)view.findViewById(R.id.track_time);

        starMarker = getResources().getString(R.string.start_marker);
        finishMarker = getResources().getString(R.string.finish_marker);

        if (getArguments()!= null) {
            set_id(getArguments().getLong("_id"));
        }

        Cursor cursor = App.getInstance().getDb().rawQuery("SELECT track.time AS time, track.distance AS distance, startTime FROM track WHERE _id = ?", new String[]{
                String.valueOf(get_id())
        });
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                distanceFromCursor = cursor.getLong(cursor.getColumnIndexOrThrow("distance"));
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow("startTime"));
                time = cursor.getLong(cursor.getColumnIndexOrThrow("time"));

                Log.e("distanceFromCursor", String.valueOf(distanceFromCursor));
                int seconds = (int) (time / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                int milliseconds = (int) (time % 1000);
                String trackTime = ("" + minutes + ":"
                        + String.format("%02d", seconds) + ","
                        + String.format("%02d", milliseconds));

                distance = (String.format("%.3f", distanceFromCursor/1000f))+ " " + getString(R.string.kilometers);
                fragmentTrackDistance.setText(distance );
                fragmentTrack_time.setText(String.valueOf(trackTime));
            }
            cursor.close();
        }

        Cursor cursorLatLng = App.getInstance().getDb().rawQuery("SELECT _id, lat, lng FROM track_gps WHERE trackId = ? ORDER BY _id ", new String[]{
                String.valueOf(get_id())
        });
        if (cursorLatLng != null) {
            double lat;
            double lng;
            if (cursorLatLng.moveToFirst()) {
                int latIndex = cursorLatLng.getColumnIndexOrThrow("lat");
                int lngIndex = cursorLatLng.getColumnIndexOrThrow("lng");
                do {
                    lat = cursorLatLng.getDouble(latIndex);
                    lng = cursorLatLng.getDouble(lngIndex);
                    addToLatLngList(lat,lng);
                } while (cursorLatLng.moveToNext());
            }
            cursorLatLng.close();
        }
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if(getLatLngList().size()>0) {
            map.addMarker(new MarkerOptions()
                    .position(getLatLngList().get(0))
                    .title(starMarker)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            if (getLatLngList().size() > 1) {
                map.addMarker(new MarkerOptions()
                        .position(getLatLngList().get(getLatLngList().size() - 1))
                        .title(finishMarker)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                map.addPolyline(getTrackPolyline());
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : getLatLngList()) {
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("TAG", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onStart(){
        super.onStart();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_track_fragment, TRACK_FRAGMENT_TAG);
    }
    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity) getActivity()).onFragmentStart(R.string.title_track_fragment, TRACK_FRAGMENT_TAG);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
