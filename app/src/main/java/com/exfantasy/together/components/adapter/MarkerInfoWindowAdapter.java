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
        TextView tvEventId = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_id);
        TextView tvCreateUserId = (TextView) mView.findViewById(R.id.dlg_marker_tv_create_user_id);
        TextView tvTitle = (TextView) mView.findViewById(R.id.dlg_marker_tv_title);
        TextView tvLat = (TextView) mView.findViewById(R.id.dlg_marker_tv_lat);
        TextView tvLng = (TextView) mView.findViewById(R.id.dlg_marker_tv_lng);
        TextView tvAttendeeNum = (TextView) mView.findViewById(R.id.dlg_marker_tv_attendee_num);
        TextView tvEventTime = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_time);

        LatLng latLng = marker.getPosition();
        String eventTitle = marker.getTitle();

        String snippet = marker.getSnippet();
        String[] split = snippet.split(";");
        String eventId = split[0];
        String createUserId = split[1];
        String attendeeNum = split[2];
        String eventTime = split[3];

        tvEventId.setText("EventId: " + eventId);
        tvCreateUserId.setText("CreateUserId: " + createUserId);
        tvTitle.setText("Title: " + eventTitle);
        tvLat.setText("Latitude: " + latLng.latitude);
        tvLng.setText("Longitude: " + latLng.longitude);
        tvAttendeeNum.setText("AttendeeNum: " + attendeeNum);
        tvEventTime.setText("Event Time: " + eventTime);

        return mView;
    }
}