package com.exfantasy.together.login;

import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.cnst.SharedPreferencesKey;
import com.exfantasy.together.components.floatingActionButton.FloatingActionButton;
import com.exfantasy.together.register.RegisterDialog;
import com.exfantasy.together.vo.LoginResult;
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
 * Created by user on 2015/10/27.
 */
public class LoginDialog extends DialogFragment implements View.OnClickListener, DialogInterface.OnClickListener {
    private String TAG = this.getClass().getSimpleName();

    private Resources mResource;
    private SharedPreferences mSharedPreferences;

    private View mLoginView;
    private EditText mEtEmail;
    private EditText mEtPassword;
    private Button mBtnLoginAtDlgLogin;
    private TextView mTvLinkRegister;

    private TextView mTvNameAtMenu;
    private TextView mTvEmailAtMenu;
    private LinearLayout mBtnLoginAtMenu;
    private LinearLayout mBtnLogoutAtMenu;

    private FloatingActionButton mFabCreateEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        mResource = getActivity().getResources();
        mSharedPreferences = getActivity().getSharedPreferences(SharedPreferencesKey.TOGEHER_KEY, Context.MODE_PRIVATE);

        findViews(builder, inflater);

        setListener(builder);

        return builder.create();
    }

    private void findViews(AlertDialog.Builder builder, LayoutInflater inflater) {
        mLoginView = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(mLoginView);

        mEtEmail = (EditText) mLoginView.findViewById(R.id.et_email_at_dlg_login);
        mEtPassword = (EditText) mLoginView.findViewById(R.id.et_password_at_dlg_login);
        mBtnLoginAtDlgLogin = (Button) mLoginView.findViewById(R.id.btn_login_at_dlg_login);

        mTvLinkRegister = (TextView) mLoginView.findViewById(R.id.tv_signup_at_dlg_login);

        mTvNameAtMenu = (TextView) getActivity().findViewById(R.id.tv_username_at_menu);
        mTvEmailAtMenu = (TextView) getActivity().findViewById(R.id.tv_user_email_at_menu);
        mBtnLoginAtMenu = (LinearLayout) getActivity().findViewById(R.id.btn_login_at_menu);
        mBtnLogoutAtMenu = (LinearLayout) getActivity().findViewById(R.id.btn_logout_at_menu);

        mFabCreateEvent = (FloatingActionButton) getActivity().findViewById(R.id.fab_create_event);
    }

    private void setListener(AlertDialog.Builder builder) {
        mBtnLoginAtDlgLogin.setOnClickListener(this);
        mTvLinkRegister.setOnClickListener(this);
        builder.setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_at_dlg_login:
                new LoginTask().execute();
                break;

            case R.id.tv_signup_at_dlg_login:
                showRegisterDialog();
                closeDialog();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        closeDialog();
    }

    // for login Dialog
    private void showRegisterDialog() {
        RegisterDialog registerDialogFragment = new RegisterDialog();
        registerDialogFragment.show(getFragmentManager(),"registerDialog");
//        replaceFrtoBackStack(registerDialogFragment, "registerDialog");

    }

//    //Daniel保留，後續測試用
//    public void replaceFrtoBackStack(Fragment fragment,String tag) {
//        FragmentTransaction t = getActivity().getSupportFragmentManager().beginTransaction();
//        t.replace(R.id.new_register_dialog_1, fragment, tag);
//        t.addToBackStack(null);
//        t.commit();
//
//    }

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

    private void saveLoginSucceedToSharedPreferences(long userId, String email, String name, String userIconUrl) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SharedPreferencesKey.ALREADY_LOGINED, true);
        editor.putLong(SharedPreferencesKey.USER_ID, userId);
        editor.putString(SharedPreferencesKey.EMAIL, email);
        editor.putString(SharedPreferencesKey.NAME, name);
        editor.putString(SharedPreferencesKey.USER_ICON_URL, userIconUrl);
        editor.commit();
    }

    private void changeUiStatusWithLoginSucceed(String name, String email) {
        mTvNameAtMenu.setText(name);
        mTvEmailAtMenu.setText(email);
        mBtnLoginAtMenu.setVisibility(View.GONE);
        mBtnLogoutAtMenu.setVisibility(View.VISIBLE);
        mFabCreateEvent.setVisibility(View.VISIBLE);
    }

    private class LoginTask extends AsyncTask<Void, Void, LoginResult> { //Params, Progress, Result
        private String email;
        private String password;

        @Override
        protected void onPreExecute() {
            this.email = mEtEmail.getText().toString();
            this.password = mEtPassword.getText().toString();
        }

        @Override
        protected LoginResult doInBackground(Void... params) {
            String url = mResource.getString(R.string.base_url) + mResource.getString(R.string.api_login);

            // Populate the HTTP Basic Authentitcation header with the username and password
            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
            formData.add("email", email);
            formData.add("password", password);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, requestHeaders);

            LoginResult loginResult = null;
            try {
                ResponseEntity<LoginResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, LoginResult.class);

                loginResult = response.getBody();

                return loginResult;
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);

                loginResult = new LoginResult();
                loginResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return loginResult;
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);

                loginResult = new LoginResult();
                loginResult.setResultCode(ResultCode.COMMUNICATION_ERROR);

                return loginResult;
            }
        }

        @Override
        protected void onPostExecute(LoginResult result) {
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case ResultCode.SUCCEED:
                    showMsgWithToast(getString(R.string.hint_login_success));
                    saveLoginSucceedToSharedPreferences(result.getUserId(), result.getEmail(), result.getName(), result.getUserIconUrl());
                    changeUiStatusWithLoginSucceed(result.getName(), result.getEmail());
                    closeDialog();
                    break;

                case ResultCode.LOGIN_FAILED_CANNOT_FIND_USER_BY_EMAIL:
                    showMsgWithToast(getString(R.string.hint_login_failed_with_email_not_existed));
                    mEtEmail.requestFocus();
                    break;

                case ResultCode.LOGIN_FAILED_PASSWORD_INVALID:
                    showMsgWithToast(getString(R.string.hint_login_failed_with_error_password));
                    mEtPassword.requestFocus();
                    break;

                case ResultCode.COMMUNICATION_ERROR:
                    showMsgWithToast(getString(R.string.warn_network_error));
                    break;
            }
        }
    }
}
