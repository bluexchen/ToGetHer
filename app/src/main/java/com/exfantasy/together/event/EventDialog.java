package com.exfantasy.together.event;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.exfantasy.together.R;
import com.exfantasy.together.components.floatingActionButton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Created by User on 2015/12/8.
 */
public class EventDialog extends DialogFragment  implements View.OnClickListener, DialogInterface.OnClickListener{
    private View mEventView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        findViews(builder, inflater);
        setListener(builder);
        return builder.create();
    }

    private void findViews(AlertDialog.Builder builder, LayoutInflater inflater) {
        mEventView = inflater.inflate(R.layout.dialog_event, null);
        builder.setView(mEventView);
    }

    private void setListener(AlertDialog.Builder builder) {
        builder.setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        closeDialog();
    }

    private void closeDialog() {
        this.dismiss();
    }
}
