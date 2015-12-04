package com.exfantasy.together.register;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.cnst.SharedPreferencesKey;
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

import java.util.Collections;

/**
 * Created by user on 2015/11/1.
 */
public class RegisterDialog extends DialogFragment implements OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    // View
    private View mRegisterView;
    private EditText mEtInputEmail;
    private EditText mEtInputPwd;
    private EditText mEtInputPwdAgain;
    private EditText mEtInputName;

    private Button mBtnRegister;
    private Button mBtnClear;

    private ImageView mImg;

    // Value

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        findViews(builder, inflater);

        setListener();

        return builder.create();
    }

    private void findViews(AlertDialog.Builder builder, LayoutInflater inflater) {
        mRegisterView = inflater.inflate(R.layout.new_register_dialog, null);
        builder.setView(mRegisterView);

        mEtInputEmail = (EditText) mRegisterView.findViewById(R.id.Et_input_email);
        mEtInputPwd = (EditText) mRegisterView.findViewById(R.id.Et_input_password);
        mEtInputPwdAgain = (EditText) mRegisterView.findViewById(R.id.Et_input_password_again);
        mEtInputName = (EditText) mRegisterView.findViewById(R.id.Et_input_name);

        mBtnRegister = (Button) mRegisterView.findViewById(R.id.btn_register_at_dlg_register);
        mBtnClear  = (Button) mRegisterView.findViewById(R.id.btn_clear_at_dlg_register);

        mImg = (ImageView) mRegisterView.findViewById(R.id.profile_image);
    }

    private void setListener() {
        mBtnRegister.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register_at_dlg_register:
                if (verifyInput()) {
                    new RegisterTask().execute();
                }
                break;

            case R.id.btn_clear_at_dlg_register:
                clearData();
                break;
        }
    }

    private boolean verifyInput() {
        String inputEmail = mEtInputEmail.getText().toString();
        String inputPwd = mEtInputPwd.getText().toString();
        String inputPwdAgain = mEtInputPwdAgain.getText().toString();
        String inputName = mEtInputName.getText().toString();

        if (inputEmail.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_email));
            mEtInputEmail.requestFocus();
            return false;
        }
        if (inputPwd.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_password));
            mEtInputPwd.requestFocus();
            return false;
        }
        if (inputPwd.length() < 6) {
            showMsgWithToast(getString(R.string.warn_input_password_length));
            mEtInputPwd.requestFocus();
            return false;
        }
        if (inputPwdAgain.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_password_again));
            mEtInputPwdAgain.requestFocus();
            return false;
        }
        if (!inputPwd.equals(inputPwdAgain)) {
            showMsgWithToast(getString(R.string.warn_pls_verify_password));
            mEtInputPwdAgain.requestFocus();
            return false;
        }
        if (inputName.isEmpty()) {
            showMsgWithToast(getString(R.string.warn_pls_input_name));
            mEtInputName.requestFocus();
            return false;
        }
        return true;
    }

    private void clearData() {
        mEtInputEmail.setText("");
        mEtInputPwd.setText("");
        mEtInputPwdAgain.setText("");
        mEtInputName.setText("");
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

    private class RegisterTask extends AsyncTask<Void, Void, OpResult> { // Params, Progress, Result
        private String mEmail;
        private String mPassword;
        private String mName;

        @Override
        protected void onPreExecute() {
            mEmail = mEtInputEmail.getText().toString();
            mPassword = mEtInputPwd.getText().toString();
            mName = mEtInputName.getText().toString();
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            OpResult registerResult = null;
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_register);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
                formData.add("email", mEmail);
                formData.add("password", mPassword);
                formData.add("name", mName);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to register with email: <" + mEmail + ">, password: <" + mPassword + ">, name: <" + mName + ">");

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);

                registerResult = response.getBody();

                Log.i(TAG, "<<<<< Register with email: <" + mEmail + ">, password: <" + mPassword + ">, name: <" + mName + "> done, result: <" + registerResult + ">");

                return registerResult;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Register with email: <" + mEmail + ">, password: <" + mPassword + ">, name: <" + mName + "> failed, err-msg: <" + e.toString() + ">", e);

                registerResult = new OpResult();
                registerResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return registerResult;
            }
        }

        @Override
        protected void onPostExecute(OpResult result) {
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case ResultCode.SUCCEED:
                    showMsgWithToast(getString(R.string.hint_register_success));
                    closeDialog();
                    break;

                case ResultCode.REGISTER_FAILED_EMAIL_ALREADY_USED:
                    showMsgWithToast(getString(R.string.hint_register_failed_with_dupilcate_email));
                    mEtInputEmail.requestFocus();
                    break;

                case ResultCode.COMMUNICATION_ERROR:
                    showMsgWithToast(getString(R.string.warn_network_error));
                    break;
            }
        }
    }
}
