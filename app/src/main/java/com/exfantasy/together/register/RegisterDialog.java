package com.exfantasy.together.register;

import android.app.Dialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.vo.OpResult;

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

import java.util.Collections;

/**
 * Created by user on 2015/11/1.
 */
public class RegisterDialog extends DialogFragment implements OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    private Resources resources;

    // View
    private View registerView;
    private EditText mEtInputEmail;
    private EditText mEtInputPwd;
    private EditText mEtInputPwdAgain;
    private EditText mEtInputName;

    private Button mBtnRegister;
    private Button mBtnClear;
    private ImageButton mImgBtnUpload;
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
        registerView = inflater.inflate(R.layout.dialog_register, null);
        builder.setView(registerView);

        resources = getActivity().getResources();

        findViews();
        setListener();

        return builder.create();
    }

    private void findViews() {
        mEtInputEmail = (EditText) registerView.findViewById(R.id.Et_input_email);
        mEtInputPwd = (EditText) registerView.findViewById(R.id.Et_input_password);
        mEtInputPwdAgain = (EditText) registerView.findViewById(R.id.Et_input_password_again);
        mEtInputName = (EditText) registerView.findViewById(R.id.Et_input_name);

        mBtnRegister = (Button) registerView.findViewById(R.id.Btn_register);
        mBtnClear  = (Button) registerView.findViewById(R.id.Btn_clear);
        mImgBtnUpload = (ImageButton) registerView.findViewById(R.id.ImgBtnUploadImg);
        mImg = (ImageView) registerView.findViewById(R.id.imageView2);
    }

    private void setListener() {
        mBtnRegister.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        mImgBtnUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Btn_register:
                Log.d(TAG, "Register button clicked!");
                if (verifyInput()) {
                    new RegisterTask().execute();
                }
                break;

            case R.id.Btn_clear:
                Log.d(TAG, "Clear button clicked!");
                clearData();
                break;

            case R.id.ImgBtnUploadImg:
                Log.d(TAG, "Image upload button clicked!");
                showImDialog();
                break;
        }
    }

    private boolean verifyInput() {
        String inputEmail = mEtInputEmail.getText().toString();
        String inputPwd = mEtInputPwd.getText().toString();
        String inputPwdAgain = mEtInputPwdAgain.getText().toString();
        String inputName = mEtInputName.getText().toString();

        if (inputEmail.isEmpty()) {
            showErrorMsg(resources.getString(R.string.warn_pls_input_email));
            mEtInputEmail.requestFocus();
            return false;
        }
        if (inputPwd.isEmpty()) {
            showErrorMsg(resources.getString(R.string.warn_pls_input_password));
            mEtInputPwd.requestFocus();
            return false;
        }
        if (inputPwd.length() < 6) {
            showErrorMsg(resources.getString(R.string.warn_input_password_length));
            mEtInputPwd.requestFocus();
            return false;
        }
        if (inputPwdAgain.isEmpty()) {
            showErrorMsg(resources.getString(R.string.warn_pls_input_password_again));
            mEtInputPwdAgain.requestFocus();
            return false;
        }
        if (!inputPwd.equals(inputPwdAgain)) {
            showErrorMsg(resources.getString(R.string.warn_pls_verify_passwrod));
            mEtInputPwdAgain.requestFocus();
            return false;
        }
        if (inputName.isEmpty()) {
            showErrorMsg(resources.getString(R.string.warn_pls_input_name));
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

    private void showErrorMsg(final String errorMsg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReplyMsg(final String replyMsg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), replyMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // for login Dialog
    private void showImDialog() {

        UploadImgDialog newFragment = new UploadImgDialog();
        newFragment.setmImg(mImg);
        newFragment.show(getFragmentManager(), "UploadImgDialog");
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
            String url = resources.getString(R.string.base_url) + resources.getString(R.string.api_register);

            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate(true);
            restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
            formData.add("email", email);
            formData.add("password", password);
            formData.add("name", name);

            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
            try {
                ResponseEntity<OpResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, OpResult.class);
                showReplyMsg(getString(R.string.hint_register_success));
                closeRegisterDialog();
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
            System.out.println(result);
        }

        private void closeRegisterDialog(){
            RegisterDialog.this.getDialog().cancel();
        }
    }
}
