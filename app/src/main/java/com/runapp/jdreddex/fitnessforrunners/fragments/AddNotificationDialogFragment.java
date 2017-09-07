package com.runapp.jdreddex.fitnessforrunners.fragments;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.runapp.jdreddex.fitnessforrunners.R;
import com.runapp.jdreddex.fitnessforrunners.activities.MainActivity;

import java.util.Calendar;

/**
 * Created by JDReddex on 13.07.2016.
 */
public class AddNotificationDialogFragment extends DialogFragment {
    public View view;
    public DatePicker datePicker;
    public TimePicker timePicker;
    public static final String TAG_ADD_NOTIFICATION = "TAG_ADD_NOTIFICATION";
    public AddNotificationDialogListener mListener;

    public interface AddNotificationDialogListener {
        void saveOnClick(Calendar date);
        void reSaveNotification(Calendar date, long _id);
    }

    public AddNotificationDialogFragment(){
    }

    public static AddNotificationDialogFragment newInstance() {
        AddNotificationDialogFragment f = new AddNotificationDialogFragment();
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_new_notification, container, false);
        datePicker = (DatePicker)view.findViewById(R.id.datePicker);
        timePicker = (TimePicker)view.findViewById(R.id.timePicker);

        timePicker.setIs24HourView(true);

        if (getArguments()!= null) {
            long editTime = getArguments().getLong("time");

            Calendar calendar= Calendar.getInstance();
            calendar.setTimeInMillis(editTime);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(calendar.get(Calendar.MINUTE));
            }else {
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
            }
            datePicker.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        }
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_btn:

                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year =  datePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    int hour =  timePicker.getHour();
                    int minute =  timePicker.getMinute();
                    calendar.set(year, month, day, hour, minute);
                    if(getArguments() != null){
                        long _id = getArguments().getLong("_id");
                        mListener.reSaveNotification(calendar, _id);
                    }else {
                        mListener.saveOnClick(calendar);
                    }
                }else {
                    int hour =  timePicker.getCurrentHour();
                    int minute =  timePicker.getCurrentMinute();
                    calendar.set(year, month, day, hour, minute);
                    if(getArguments() != null){
                        long _id = getArguments().getLong("_id");
                        mListener.reSaveNotification(calendar, _id);
                    }else {
                        mListener.saveOnClick(calendar);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_notification_add, menu);
    }

    @Override
    public void onStart(){
        super.onStart();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_new_notification, TAG_ADD_NOTIFICATION);
        try {
            mListener = (AddNotificationDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement AddNotificationDialogListener");
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).onFragmentStart(R.string.title_new_notification, TAG_ADD_NOTIFICATION);
    }
}
