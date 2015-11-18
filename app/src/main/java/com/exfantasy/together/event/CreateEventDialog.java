package com.exfantasy.together.event;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.exfantasy.together.R;
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
public class CreateEventDialog extends DialogFragment {

    private String TAG = this.getClass().getSimpleName();

    private Resources resources;

    //Views
    private View mCreateEventView;
    private EditText mEtEventName;    // 事件名稱
    private EditText mEtEventContent;  // 事件內容
    private EditText mEtAttendeeNum;   // 事件人數
    private EditText mEtEventTime;     // 事件時間
    private Button   mBtnCreateEvent;

    //values
    private Double centerLat;
    private Double centerLng;

    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle latlngBundle = getArguments();
        centerLat = latlngBundle.getDouble("lat");
        centerLng = latlngBundle.getDouble("lng");
        Log.i(TAG, "CreateEventDialog gets center LatLng = " + centerLat + ", " + centerLng);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        resources = getActivity().getResources();

        findViews(inflater);
        setListener(builder);

        return builder.create();
    }

    private void findViews(LayoutInflater inflater) {
        mCreateEventView = inflater.inflate(R.layout.dialog_create_event, null);
        mEtEventName = (EditText) mCreateEventView.findViewById(R.id.Et_input_event_name);
        mEtEventContent = (EditText) mCreateEventView.findViewById(R.id.Et_input_event_content);
        mEtAttendeeNum  = (EditText) mCreateEventView.findViewById(R.id.Et_input_people_num);
        mEtEventTime = (EditText) mCreateEventView.findViewById(R.id.Et_input_event_time);
        mBtnCreateEvent = (Button) mCreateEventView.findViewById(R.id.btn_create_evnet);

        mEtEventTime.setKeyListener(null);
        mEtEventTime.setFocusable(false);
        mEtEventTime.setClickable(true);
    }

    private void setListener(AlertDialog.Builder builder) {
        mEtEventTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        mBtnCreateEvent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyInput()) {
                    new CreateEventTask().execute();
                }
            }
        });

        builder.setView(mCreateEventView).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CreateEventDialog.this.getDialog().cancel();
            }
        });
    }

    private boolean verifyInput() {
        String eventName = mEtEventName.getText().toString();
        if (eventName.isEmpty()) {
            showMsgWithToast(resources.getString(R.string.warn_pls_input_event_name));
            mEtEventName.requestFocus();
            return false;
        }
        String eventContent = mEtEventContent.getText().toString();
        if (eventContent.isEmpty()) {
            showMsgWithToast(resources.getString(R.string.warn_pls_input_event_content));
            mEtEventContent.requestFocus();
            return false;
        }
        String attendeeNum = mEtAttendeeNum.getText().toString();
        if (attendeeNum.isEmpty()) {
            showMsgWithToast(resources.getString(R.string.warn_pls_input_attendee_num));
            mEtAttendeeNum.requestFocus();
            return false;
        }
        String eventTime = mEtEventTime.getText().toString();
        if (eventTime.isEmpty()) {
            showMsgWithToast(resources.getString(R.string.warn_pls_input_event_time));
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
        private String eventName;
        private String eventContent;
        private int attendeeNum;
        private long eventTime;

        private Event eventToCreate;

        @Override
        protected void onPreExecute() {
            this.eventName = mEtEventName.getText().toString();
            this.eventContent = mEtEventContent.getText().toString();
            this.attendeeNum = Integer.parseInt(mEtAttendeeNum.getText().toString());

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
            this.eventTime = DateTimeUtil.dateTimeValue(calToGetEventTime);

            eventToCreate = new Event(centerLat, centerLng, eventName, eventContent, attendeeNum, eventTime);
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            String url = resources.getString(R.string.base_url) + resources.getString(R.string.api_create_event);

            // Populate the HTTP Basic Authentitcation header with the username and password
            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
            formData.add("latitude", centerLat + "");
            formData.add("longitude", centerLng + "");
            formData.add("name", eventName);
            formData.add("content", eventContent);
            formData.add("attendeeNum", attendeeNum + "");
            formData.add("time", eventTime + "");

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
                    Log.i(TAG, "Create " + eventToCreate + " succeed");
                    showMsgWithToast(getString(R.string.hint_create_event_succeed));
                    break;

                case ResultCode.CREATE_EVENT_FAILED:
                    Log.e(TAG, "Create " + eventToCreate + " failed");
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
