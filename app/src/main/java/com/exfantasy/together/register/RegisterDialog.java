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

    private Resources mResources;
    private SharedPreferences mSharedPreferences;

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

        mResources = getActivity().getResources();
        mSharedPreferences = getActivity().getSharedPreferences(SharedPreferencesKey.TOGEHER_KEY, Context.MODE_PRIVATE);

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
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_email));
            mEtInputEmail.requestFocus();
            return false;
        }
        if (inputPwd.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_password));
            mEtInputPwd.requestFocus();
            return false;
        }
        if (inputPwd.length() < 6) {
            showMsgWithToast(mResources.getString(R.string.warn_input_password_length));
            mEtInputPwd.requestFocus();
            return false;
        }
        if (inputPwdAgain.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_password_again));
            mEtInputPwdAgain.requestFocus();
            return false;
        }
        if (!inputPwd.equals(inputPwdAgain)) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_verify_password));
            mEtInputPwdAgain.requestFocus();
            return false;
        }
        if (inputName.isEmpty()) {
            showMsgWithToast(mResources.getString(R.string.warn_pls_input_name));
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

    private void saveRegisterSucceedToSharedPreferences() {
        // TODO
    }

    private class RegisterTask extends AsyncTask<Void, Void, OpResult> { // Params, Progress, Result
        private String email;
        private String password;
        private String name;

        @Override
        protected void onPreExecute() {
            email = mEtInputEmail.getText().toString();
            password = mEtInputPwd.getText().toString();
            name = mEtInputName.getText().toString();
        }

        @Override
        protected OpResult doInBackground(Void... params) {
            // Populate the HTTP Basic Authentitcation header with the username and password
            String url = mResources.getString(R.string.base_url) + mResources.getString(R.string.api_register);

            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
            formData.add("email", email);
            formData.add("password", password);
            formData.add("name", name);

            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
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
                    showMsgWithToast(getString(R.string.hint_register_success));
                    saveRegisterSucceedToSharedPreferences();
                    closeDialog();
                    break;

                case ResultCode.REGISTER_FAILED_EMAIL_ALREADY_USED:
                    showMsgWithToast(getString(R.string.hint_register_failed_with_dupilcate_email));
                    mEtInputEmail.requestFocus();
                    break;
            }
        }
    }
}
