package com.exfantasy.together.setting;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.exfantasy.together.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class SettingDialog extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View setup_recycleView = inflater.inflate(R.layout.dialog_setup, null);
        RecyclerView RV = (RecyclerView) setup_recycleView.findViewById(R.id.setup_recyclerview);
        RV.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<String> setupItem = new ArrayList<>();
        String[] setupArray = getActivity().getResources().getStringArray(R.array.setup);
        Collections.addAll(setupItem, setupArray);



        SettingListViewAdapter settingListViewAdapter = new SettingListViewAdapter(setupItem);
        RV.setAdapter(settingListViewAdapter);
        builder.setView(setup_recycleView);

        return builder.create();
    }
}