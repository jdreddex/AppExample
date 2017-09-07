package com.runapp.jdreddex.fitnessforrunners.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.runapp.jdreddex.fitnessforrunners.R;

public class RunDialogFragment extends DialogFragment {

    private String title;
    private String message;
    private String posBtn;
    private String negBTN;
    private static final String TAG = "TAG";

    public static RunDialogFragment newInstance(String TITLE, String MESSAGE, String POS_BTN,String NEG_BTN) {
        RunDialogFragment f = new RunDialogFragment();
        Bundle bundle = new Bundle();
        android.util.Log.e("BUNDLE NAME", MESSAGE);
        bundle.putString("TITLE", TITLE);
        bundle.putString("MESSAGE", MESSAGE);
        bundle.putString("POS_BTN", POS_BTN);
        bundle.putString("NEG_BTN", NEG_BTN);
        f.setArguments(bundle);
        return f;
    }

    public RunDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            title = this.getArguments().getString("TITLE");
            message = this.getArguments().getString("MESSAGE");
            posBtn = this.getArguments().getString("POS_BTN");
            negBTN = this.getArguments().getString("NEG_BTN");
        }catch (NullPointerException e){
            android.util.Log.e(TAG, "Exception", e);
        }

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(posBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (message.equals(getResources().getString(R.string.close_message))) {
                            getActivity().finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(negBTN, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

}
