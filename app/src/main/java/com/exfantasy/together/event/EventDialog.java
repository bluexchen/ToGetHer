package com.exfantasy.together.event;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.cnst.SharedPreferencesKey;
import com.exfantasy.together.components.cardView.CardAdapter;
import com.exfantasy.together.components.cardView.MsgRecordItem;
import com.exfantasy.together.vo.Message;
import com.exfantasy.together.vo.OpResult;
import com.exfantasy.together.vo.ResultCode;
import com.exfantasy.together.vo.User;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by User on 2015/12/8.
 */
public class EventDialog extends DialogFragment implements View.OnClickListener, DialogInterface.OnClickListener{

    public static final String TAG = EventDialog.class.getSimpleName();

    // Variables
    private long mUserId;
    private long mEventId;
    private long mCreateUserId;

    // Variables to set UI
    private User mCreateUser;
    private String mEventContent;
    private int mAttendeeNum;
    private String mAttendee;
    private List<MsgRecordItem> mMessageRecordList = new ArrayList<MsgRecordItem>();

    // UI Components
    private View mEventView;
    private TextView mTvCreateUser;
    private TextView mTvEventContent;
    private TextView mTvEventAttendeeNum;
    private TextView mTvEventAttendee;
    private EditText mEtMessage;
    private RecyclerView mRecyclerView;
    private Button mBtnLeaveMsg;
    private Button mBtnJoin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        getData();

        findViews(builder, inflater);

        setUiShowContent();

        setRecyclerView();

        setListener(builder);

        return builder.create();
    }

    private void getData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SharedPreferencesKey.TOGEHER_KEY, Context.MODE_PRIVATE);
        Bundle bundle = getArguments();

        mUserId = sharedPreferences.getLong(SharedPreferencesKey.USER_ID, -1);
        mEventId = bundle.getLong("eventId");
        mCreateUserId = bundle.getLong("createUserId");

        List<User> userList = bundle.getParcelableArrayList("eventAttendee");
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            buffer.append("[").append(user.getUserId()).append("-").append(user.getName()).append("]");
            if (user.getUserId() == mCreateUserId) {
                mCreateUser = user;
            }
        }

        mEventContent = bundle.getString("eventContent");
        mAttendeeNum = bundle.getInt("eventAttendeeNum");
        mAttendee = buffer.toString();
        if(bundle.getParcelableArrayList("eventMessage") != null){
            List<Message> messageList = bundle.getParcelableArrayList("eventMessage");
            for(int i = 0; i < messageList.size(); i++){
                Message message = messageList.get(i);
                mMessageRecordList.add(new MsgRecordItem(message.getCreateUserName(), message.getContent()));
            }
        }
    }

    private void findViews(AlertDialog.Builder builder, LayoutInflater inflater) {
        mEventView = inflater.inflate(R.layout.dialog_event, null);

        builder.setView(mEventView);

        mTvCreateUser = (TextView) mEventView.findViewById(R.id.tv_create_user_at_dlg_event);
        mTvEventContent = (TextView) mEventView.findViewById(R.id.tv_event_content_at_dlg_event);
        mTvEventAttendeeNum = (TextView) mEventView.findViewById(R.id.tv_attendee_num_at_dlg_event);
        mTvEventAttendee = (TextView) mEventView.findViewById(R.id.tv_event_attendee_at_dlg_event);
        mEtMessage = (EditText) mEventView.findViewById(R.id.et_message_at_dlg_event);
        mRecyclerView = (RecyclerView) mEventView.findViewById(R.id.recycler_view_at_dlg_event);
        mBtnLeaveMsg = (Button) mEventView.findViewById(R.id.btn_leaveMsg_at_dlg_event);
        mBtnJoin = (Button) mEventView.findViewById(R.id.btn_join_at_dlg_event);
        if (mUserId == mCreateUserId) {
            mBtnJoin.setVisibility(View.GONE);
        }
    }

    private void setRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mMessageRecordList.size() != 0) {
            CardAdapter adapter = new CardAdapter(mMessageRecordList.toArray(new MsgRecordItem[mMessageRecordList.size()]));
            final float scale = getContext().getResources().getDisplayMetrics().density;
            int itemHeightpixels = (int) (59 * scale + 0.5f);
            int viewHeightpixels = (int) (130 * scale + 0.5f);
            int contentHeight = adapter.getItemCount()* itemHeightpixels;
            if (contentHeight < viewHeightpixels){
                mRecyclerView.getLayoutParams().height = contentHeight;
            }
            mRecyclerView.setAdapter(adapter);
        } else {
            mRecyclerView.getLayoutParams().height = 0;
        }
    }

    private void setUiShowContent() {
        mTvCreateUser.setText("建立活動者: [" + mCreateUser.getUserId() + "-" + mCreateUser.getName() + "]");
        mTvEventContent.setText("活動內容: "+ mEventContent);
        mTvEventAttendeeNum.setText("參加人數: " + mAttendeeNum);
        mTvEventAttendee.setText("目前參與者: " + mAttendee);
    }

    private void setListener(AlertDialog.Builder builder) {
        builder.setNegativeButton(R.string.cancel, this);
        mBtnLeaveMsg.setOnClickListener(this);
        mBtnJoin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_leaveMsg_at_dlg_event:
                if (checkLeaveMsg()) {
                    new LeaveMsgTask().execute();
                }
                break;

            case R.id.btn_join_at_dlg_event:
                new JoinTask().execute();
                break;
        }
    }

    private boolean checkLeaveMsg() {
        boolean msgIsEmpty = mEtMessage.getText().toString().isEmpty();
        if (msgIsEmpty) {
            showMsgWithToast(getString(R.string.warn_pls_input_message));
            return false;
        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        closeDialog();
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

    private class LeaveMsgTask extends AsyncTask<Void, Void, OpResult> { // Params, Progress, Result

        private String mContent;

        @Override
        protected void onPreExecute() {
            mContent = mEtMessage.getText().toString();
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            OpResult leaveMsgResult = null;
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_leave_msg);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("eventId", mEventId + "");
                formData.add("createUserId", mUserId + "");
                formData.add("content", mContent);

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to leave message by create-user-id: <" + mUserId + ">, event-id: <" + mEventId + ">, content: <" + mContent + ">");

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);

                leaveMsgResult = response.getBody();

                Log.i(TAG, "<<<<< Leave message by create-user-id: <" + mUserId + ">, event-id: <" + mEventId + ">, content: <" + mContent + "> done, result: <" + leaveMsgResult + ">");

                return leaveMsgResult;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Leave message by create-user-id: <" + mUserId + ">, event-id: <" + mEventId + ">, content: <" + mContent + "> failed, err-msg: <" + e.toString() + ">", e);

                leaveMsgResult = new OpResult();
                leaveMsgResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return leaveMsgResult;
            }
        }

        @Override
        protected void onPostExecute(OpResult result) {
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case ResultCode.SUCCEED:
                    showMsgWithToast(getString(R.string.msg_leave_msg_succeed));
                    break;

                case ResultCode.LEAVE_MSG_FAILED_WITH_EVENT_IS_NULL:
                    showMsgWithToast(getString(R.string.msg_leave_msg_failed_with_cannot_find_mapping_event));
                    break;

                case ResultCode.LEAVE_MSG_FAILED_WITH_USER_IS_NULL:
                    showMsgWithToast(getString(R.string.msg_leave_msg_failed_with_cannot_find_mapping_user));
                    break;

                case ResultCode.JOIN_EVENT_FAILED_WITH_EXCEPTION:
                    String resultMsg = result.getResultMsg();
                    showMsgWithToast(getString(R.string.msg_leave_msg_failed_with_server_exception));
                    Log.e(TAG, "<<<<< Leave message failed with error-msg: " + resultMsg);
                    break;

                case ResultCode.COMMUNICATION_ERROR:
                    showMsgWithToast(getString(R.string.error_network_abnormal));
                    break;
            }
            closeDialog();
        }
    }

    private class JoinTask extends AsyncTask<Void, Void, OpResult> { // Params, Progress, Result

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            OpResult joinResult = null;
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_join_event);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("joinUserId", mUserId + "");
                formData.add("eventId", mEventId + "");

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to join event by join-user-id: <" + mUserId + ">, event-id: <" + mEventId + ">");

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);

                joinResult = response.getBody();

                Log.i(TAG, "<<<<< Join event by join-user-id: <" + mUserId + ">, event-id: <" + mEventId + "> done, result: <" + joinResult + ">");

                return joinResult;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Join event by join-user-id: <" + mUserId + ">, event-id: <" + mEventId + "> failed, err-msg: <" + e.toString() + ">", e);

                joinResult = new OpResult();
                joinResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return joinResult;
            }
        }

        @Override
        protected void onPostExecute(OpResult result) {
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case ResultCode.SUCCEED:
                    showMsgWithToast(getString(R.string.msg_join_event_succeed));
                    break;

                case ResultCode.JOIN_EVENT_FAILED_WITH_EVENT_IS_NULL:
                    showMsgWithToast(getString(R.string.msg_join_event_failed_with_cannot_find_mapping_event));
                    break;

                case ResultCode.JOIN_EVENT_FAILED_WITH_JOIN_USER_CREATED:
                    showMsgWithToast(getString(R.string.msg_join_event_failed_with_join_user_created));
                    break;

                case ResultCode.JOIN_EVENT_FAILED_WITH_USER_IS_NULL:
                    showMsgWithToast(getString(R.string.msg_join_event_failed_with_cannot_find_mapping_user));
                    break;

                case ResultCode.JOIN_EVENT_FAILED_WITH_ALREADY_JOINED:
                    showMsgWithToast(getString(R.string.msg_join_event_failed_with_already_joined));
                    break;

                case ResultCode.JOIN_EVENT_FAILED_WITH_EXCEPTION:
                    String resultMsg = result.getResultMsg();
                    showMsgWithToast(getString(R.string.msg_join_event_failed_with_server_exception));
                    Log.e(TAG, "<<<<< Join event failed with error-msg: " + resultMsg);
                    break;

                case ResultCode.COMMUNICATION_ERROR:
                    showMsgWithToast(getString(R.string.error_network_abnormal));
                    break;
            }
            closeDialog();
        }
    }
}
