package com.exfantasy.together.components.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.exfantasy.together.R;

public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View mView;

    public MarkerInfoWindowAdapter(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.dialog_marker, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView tvTitle = (TextView) mView.findViewById(R.id.dlg_marker_title);
        TextView tvLat = (TextView) mView.findViewById(R.id.dlg_marker_tv_lat);
        TextView tvLng = (TextView) mView.findViewById(R.id.dlg_marker_tv_lng);
        TextView tvAttendeeNum = (TextView) mView.findViewById(R.id.dlg_marker_tv_attendee_num);
        TextView tvEventTime = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_time);

        LatLng latLng = marker.getPosition();

        tvTitle.setText("Title: " + marker.getTitle());
        tvLat.setText("Latitude: " + latLng.latitude);
        tvLng.setText("Longitude: " + latLng.longitude);

        String snippet = marker.getSnippet();
        String[] split = snippet.split(";");

        tvAttendeeNum.setText("AttendeeNum: " + split[0]);
        tvEventTime.setText("Event Time: " + split[1]);

        return mView;
    }
}