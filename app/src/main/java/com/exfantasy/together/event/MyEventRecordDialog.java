package com.exfantasy.together.event;

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

/**
 * TODO: document your custom view class.
 */
public class MyEventRecordDialog extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View recentlyAction_recycleView = inflater.inflate(R.layout.dialog_my_events_record, null);
        RecyclerView RV = (RecyclerView) recentlyAction_recycleView.findViewById(R.id.recentlyAction_recyclerview);
        RV.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArrayList<String> item = new ArrayList<>();
        item.add("敬請期待");


        MyEventRecordListViewAdapter MyEventRecordListViewAdapter = new MyEventRecordListViewAdapter(item);
        RV.setAdapter(MyEventRecordListViewAdapter);
        builder.setView(recentlyAction_recycleView);

        return builder.create();
    }
}