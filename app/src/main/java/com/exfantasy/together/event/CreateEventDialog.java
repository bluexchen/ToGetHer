package com.exfantasy.together.event;

import android.app.Dialog;
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
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Collections;

/**
 * Created by DanielChen on 15/11/2.
 */
public class  CreateEventDialog extends DialogFragment implements OnClickListener{

    private String TAG = this.getClass().getSimpleName();

    private Resources resources;

    //Views
    View mCreateEventView;
    EditText mEtEventName;     //事件名稱
    EditText mEtEventContent;  //事件內容
    EditText mEtAttendeeNum;   //事件人數
    EditText mEteventTime;  //暫時用手動輸入
    Button   mBtnCreateEvent;
//    DatePickerDialog datePickerDialog;

    //values
    Double centerLat;
    Double centerLng;

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

        findViews(inflater);
        setListener(builder);

        resources = getActivity().getResources();

        return builder.create();
    }

    @Override
    public void onClick(View v) {

    }

    private void findViews(LayoutInflater inflater) {
        mCreateEventView = inflater.inflate(R.layout.dialog_create_event, null);
        mEtEventName = (EditText) mCreateEventView.findViewById(R.id.Et_input_event_name);
        mEtEventContent = (EditText) mCreateEventView.findViewById(R.id.Et_input_event_content);
        mEtAttendeeNum  = (EditText) mCreateEventView.findViewById(R.id.Et_input_people_num);
        mEteventTime = (EditText) mCreateEventView.findViewById(R.id.Et_input_event_time);
        mBtnCreateEvent = (Button) mCreateEventView.findViewById(R.id.btn_create_evnet);

        mEteventTime.setKeyListener(null);
        mEteventTime.setFocusable(false);
        mEteventTime.setClickable(true);
    }

    private void setListener(AlertDialog.Builder builder) {

        mEteventTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Calendar now = Calendar.getInstance();
//
//                datePickerDialog = DatePickerDialog.newInstance(
//                        getActivity(),
//                        now.get(Calendar.YEAR),
//                        now.get(Calendar.MONTH),
//                        now.get(Calendar.DAY_OF_MONTH)
//                );
//
//                datePickerDialog.show(getActivity().getFragmentManager(), "datePickerDialog");


            }
        });

        mBtnCreateEvent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateEventTask().execute();
            }
        });

        builder.setView(mCreateEventView).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CreateEventDialog.this.getDialog().cancel();
            }
        });
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

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            this.eventTime = DateTimeUtil.dateTimeValue(calendar);

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
            RestTemplate restTemplate = new RestTemplate(true);
            restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
            formData.add("latitude", centerLat + "");
            formData.add("longitude", centerLng + "");
            formData.add("name", eventName);
            formData.add("content", eventContent);
            formData.add("attendeeNum", attendeeNum + "");
            formData.add("time", eventTime + "");

            HttpEntity<MultiValueMap<String, String>> requestEntity
                    = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);
            try {
                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);
                return response.getBody();
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(OpResult result) {
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case ResultCode.SUCCEED:
                    Log.i(TAG, "Create " + eventToCreate + " succeed");
                    break;

                default:
                    Log.e(TAG, "Create " + eventToCreate + " failed");
                    break;
            }
        }
    }

}
