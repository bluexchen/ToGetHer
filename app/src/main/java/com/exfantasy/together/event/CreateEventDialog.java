package com.exfantasy.together.event;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

    private SharedPreferences mSharedPreferences;

    //Views
    private View mCreateEventView;
    private EditText mEtEventName;    // 活動名稱
    private EditText mEtEventContent;  // 活動內容
    private EditText mEtAttendeeNum;   // 活動人數
    private EditText mEtEventDate;     // 活動日期
    private EditText mEtEventTime;     // 活動時間
    private Button   mBtnCreateEvent;

    //values
    private Double mCenterLat;
    private Double mCenterLng;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd");
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
        mEtAttendeeNum  = (EditText) mCreateEventView.findViewById(R.id.et_event_requied_people_num_at_dlg_create_event);
        mEtEventDate = (EditText) mCreateEventView.findViewById(R.id.et_event_date_at_dlg_create_event);
        mEtEventTime = (EditText) mCreateEventView.findViewById(R.id.et_event_time_at_dlg_create_event);
        mBtnCreateEvent = (Button) mCreateEventView.findViewById(R.id.btn_create_evnet_at_dlg_create_event);

        mEtEventDate.setKeyListener(null);
        mEtEventDate.setFocusable(false);
        mEtEventDate.setClickable(true);

        mEtEventTime.setKeyListener(null);
        mEtEventTime.setFocusable(false);
        mEtEventTime.setClickable(true);
    }

    private void setListener(AlertDialog.Builder builder) {
        mEtEventDate.setOnClickListener(this);
        mEtEventTime.setOnClickListener(this);
        mBtnCreateEvent.setOnClickListener(this);
        builder.setView(mCreateEventView).setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.et_event_date_at_dlg_create_event:
                showDatePicker();
                break;

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
            showMsgWithToast(getString(R.string.warn_pls_input_event_name));
            mEtEventName.requestFocus();
            return false;
        }
        String eventContent = mEtEventContent.getText().toString();
        if (eventContent.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_event_content));
            mEtEventContent.requestFocus();
            return false;
        }
        String attendeeNum = mEtAttendeeNum.getText().toString();
        if (attendeeNum.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_attendee_num));
            mEtAttendeeNum.requestFocus();
            return false;
        }
        String eventDate = mEtEventDate.getText().toString();
        if (eventDate.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_event_date));
            return false;
        }
        String eventTime = mEtEventTime.getText().toString();
        if (eventTime.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_event_time));
            return false;
        }
        return true;
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker
                = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, selectedYear);
                        cal.set(Calendar.MONTH, selectedMonth);
                        cal.set(Calendar.DAY_OF_MONTH, selectedDay);
                        String showDate = mDateFormat.format(cal.getTime());

                        mEtEventDate.setText(showDate);
                    }
        }, year, month, day);
        datePicker.show();
    }

    private void showTimePicker() {
        final Calendar cal = Calendar.getInstance();
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
        private long mCreateUserId;
        private String mEventName;
        private String mEventContent;
        private int mAttendeeNum;
        private int mEventDate;
        private int mEventTime;

        private Event mEventToCreate;

        @Override
        protected void onPreExecute() {
            mCreateUserId = mSharedPreferences.getLong(SharedPreferencesKey.USER_ID, -1);
            mEventName = mEtEventName.getText().toString();
            mEventContent = mEtEventContent.getText().toString();
            mAttendeeNum = Integer.parseInt(mEtAttendeeNum.getText().toString());
            mEventDate = getEventDate();
            mEventTime = getEventTime();

            mEventToCreate = new Event(mCenterLat, mCenterLng, mEventName, mEventContent, mAttendeeNum, mEventDate, mEventTime);
        }

        private int getEventDate() {
            String sEventDate = mEtEventDate.getText().toString();
            Calendar calToGetEventDate = Calendar.getInstance();
            try {
                Date date = mDateFormat.parse(sEventDate);

                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(date);

                calToGetEventDate.set(Calendar.YEAR, tempCal.get(Calendar.YEAR));
                calToGetEventDate.set(Calendar.MONTH, tempCal.get(Calendar.MONTH));
                calToGetEventDate.set(Calendar.DAY_OF_MONTH, tempCal.get(Calendar.DAY_OF_MONTH));
            } catch (ParseException e) {
                Log.e(TAG, "Parse event date got exception, msg: " + e.getMessage());
            }
            return DateTimeUtil.dateValue(calToGetEventDate);
        }

        private int getEventTime() {
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
            return DateTimeUtil.timeValue(calToGetEventTime);
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            OpResult createEventResult = null;
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_create_event);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("createUserId", mCreateUserId + "");
                formData.add("latitude", mCenterLat + "");
                formData.add("longitude", mCenterLng + "");
                formData.add("name", mEventName);
                formData.add("content", mEventContent);
                formData.add("attendeeNum", mAttendeeNum + "");
                formData.add("date", mEventDate + "");
                formData.add("time", mEventTime + "");

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to create " + mEventToCreate);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);

                createEventResult = response.getBody();

                Log.i(TAG, "<<<<< Create " + mEventToCreate + " done");

                return createEventResult;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Create " + mEventToCreate + " failed, err-msg: <" + e.toString() + ">", e);

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
                    showMsgWithToast(getString(R.string.msg_create_event_succeed));
                    break;

                case ResultCode.CREATE_EVENT_FAILED:
                    Log.e(TAG, "Create " + mEventToCreate + " failed");
                    showMsgWithToast(getString(R.string.msg_create_event_failed));
                    break;

                case ResultCode.COMMUNICATION_ERROR:
                    showMsgWithToast(getString(R.string.error_network_abnormal));
                    break;
            }
            closeDialog();
        }
    }
}
