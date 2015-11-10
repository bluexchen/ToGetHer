package com.exfantasy.together.mapInfoEvent;

import android.app.Dialog;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.exfantasy.together.R;

/**
 * Created by Lab on 2015/11/10.
 */
public class EventDialog extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View mapView = inflater.inflate(R.layout.activity_main,null);
        Snackbar.make(mapView,"Test",Snackbar.LENGTH_LONG);
        AlertDialog dialog = builder.create();

        dialog.getWindow().getAttributes().windowAnimations = R.style.dlg_animation;

        return dialog;
    }
}
