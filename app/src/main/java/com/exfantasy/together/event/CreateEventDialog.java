package com.exfantasy.together.event;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.cnst.SharedPreferencesKey;
import com.exfantasy.together.util.DateTimeUtil;
import com.exfantasy.together.vo.Event;
import com.exfantasy.together.vo.OpResult;
import com.exfantasy.together.vo.ResultCode;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Created by DanielChen on 15/11/2.
 */
public class CreateEventDialog extends DialogFragment implements View.OnClickListener, DialogInterface.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    private Resources mResources;
    private SharedPreferences mSharedPreferences;

    //Views
    private View mCreateEventView;
    private EditText mEtEventName;    // 事件名稱
    private EditText mEtEventContent;  // 事件內容
    private EditText mEtAttendeeNum;   // 事件人數
    private EditText mEtEventTime;     // 事件時間
    private Button   mBtnCreateEvent;

    //values
    private Double mCenterLat;
    private Double mCenterLng;

    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle latlngBundle = getArguments();
        mCenterLat = latlngBundle.getDouble("lat");
        mCenterLng = latlngBundle.getDouble("lng");
        Log.i(TAG, "CreateEventDialog gets center LatLng = " + mCenterLat + ", " + mCenterLng);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        mResources = getActivity().getResources();
        mSharedPreferences = getActivity().getSharedPreferences(SharedPreferencesKey.TOGEHER_KEY, Context.MODE_PRIVATE);

        findViews(builder, inflater);
        setListener(builder);

        return builder.create();
    }

    private void findViews(AlertDialog.Builder builder, LayoutInflater inflater) {
        mCreateEventView = inflater.inflate(R.layout.dialog_create_event, null);
        builder.setView(mCreateEventView);

        mEtEventName = (EditText) mCreateEventView.findViewById(R.id.et_event_name_at_dlg_create_event);
        mEtEventContent = (EditText) mCreateEventView.findViewById(R.id.et_event_content_at_dlg_create_event);
        mEtAttendeeNum  = (EditText) mCreateEventView.findViewById(R.id.et_people_num_at_dlg_create_event);
        mEtEventTime = (EditText) mCreateEventView.findViewById(R.id.et_event_time_at_dlg_create_event);
        mBtnCreateEvent = (Button) mCreateEventView.findViewById(R.id.btn_create_evnet_at_dlg_create_event);

        mEtEventTime.setKeyListener(null);
        mEtEventTime.setFocusable(false);
        mEtEventTime.setClickable(true);
    }

    private void setListener(AlertDialog.Builder builder) {
        mEtEventTime.setOnClickListener(this);
        mBtnCreateEvent.setOnClickListener(this);
        builder.setView(mCreateEventView).setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_event_time_at_dlg_create_event:
                showTimePicker();
                break;

            case R.id.btn_create_evnet_at_dlg_create_event:
                if (verifyInput()) {
                    new CreateEventTask().execute();
                }
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        closeDialog();
    }

    private boolean verifyInput() {
        String eventName = mEtEventName.getText().toString();
        if (eventName.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_event_name));
            mEtEventName.requestFocus();
            return false;
        }
        String eventContent = mEtEventContent.getText().toString();
        if (eventContent.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_event_content));
            mEtEventContent.requestFocus();
            return false;
        }
        String attendeeNum = mEtAttendeeNum.getText().toString();
        if (attendeeNum.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_attendee_num));
            mEtAttendeeNum.requestFocus();
            return false;
        }
        String eventTime = mEtEventTime.getText().toString();
        if (eventTime.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_event_time));
            return false;
        }
        return true;
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        TimePickerDialog timePicker
                = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                        cal.set(Calendar.MINUTE, selectedMinute);
                        String showTime = mTimeFormat.format(cal.getTime());

                        mEtEventTime.setText(showTime);
                    }
        }, hour, minute, false);
        timePicker.show();
    }

    private void showMsgWithToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeDialog() {
        this.dismiss();
    }

    private class CreateEventTask extends AsyncTask<Void, Void, OpResult> {   //Params, Progress, Result
        private long mUserId;
        private String mEventName;
        private String mEventContent;
        private int mAttendeeNum;
        private long mEventTime;

        private Event mEventToCreate;

        @Override
        protected void onPreExecute() {
            mUserId = mSharedPreferences.getLong(SharedPreferencesKey.USER_ID, -1);
            mEventName = mEtEventName.getText().toString();
            mEventContent = mEtEventContent.getText().toString();
            mAttendeeNum = Integer.parseInt(mEtAttendeeNum.getText().toString());

            String sEventTime = mEtEventTime.getText().toString();
            Calendar calToGetEventTime = Calendar.getInstance();
            try {
                Date time = mTimeFormat.parse(sEventTime);
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(time);

                calToGetEventTime.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY));
                calToGetEventTime.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE));
            } catch (ParseException e) {
                Log.e(TAG, "Parse event time got exception, msg: " + e.getMessage());
            }
            mEventTime = DateTimeUtil.dateTimeValue(calToGetEventTime);

            mEventToCreate = new Event(mCenterLat, mCenterLng, mEventName, mEventContent, mAttendeeNum, mEventTime);
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            String url = mResources.getString(R.string.base_url) + mResources.getString(R.string.api_create_event);

            // Populate the HTTP Basic Authentitcation header with the username and password
            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
            formData.add("userId", mUserId + "");
            formData.add("latitude", mCenterLat + "");
            formData.add("longitude", mCenterLng + "");
            formData.add("name", mEventName);
            formData.add("content", mEventContent);
            formData.add("attendeeNum", mAttendeeNum + "");
            formData.add("time", mEventTime + "");

            HttpEntity<MultiValueMap<String, String>> requestEntity
                    = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);

            OpResult createEventResult = null;
            try {
                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);

                createEventResult = response.getBody();

                return createEventResult;
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);

                createEventResult = new OpResult();
                createEventResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return createEventResult;
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);

                createEventResult = new OpResult();
                createEventResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return createEventResult;
            }
        }

        @Override
        protected void onPostExecute(OpResult result) {
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case ResultCode.SUCCEED:
                    Log.i(TAG, "Create " + mEventToCreate + " succeed");
                    showMsgWithToast(getString(R.string.hint_create_event_succeed));
                    break;

                case ResultCode.CREATE_EVENT_FAILED:
                    Log.e(TAG, "Create " + mEventToCreate + " failed");
                    showMsgWithToast(getString(R.string.hint_create_event_failed));
                    break;

                case ResultCode.COMMUNICATION_ERROR:
                    showMsgWithToast(getString(R.string.warn_network_error));
                    break;
            }
            closeDialog();
        }
    }
}
