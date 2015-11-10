package com.exfantasy.together.login;

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
import android.widget.TextView;

import com.exfantasy.together.R;
import com.exfantasy.together.register.RegisterDialog;
import com.exfantasy.together.vo.LoginResult;

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
 * Created by user on 2015/10/27.
 */
public class LoginDialog extends DialogFragment {
    private String TAG = this.getClass().getSimpleName();

    private Resources resources;

    // View
    private View loginView;
    private Button btnLogin;
    private TextView tvLinkRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        findViews(inflater);
        setListener(builder);

        resources = getActivity().getResources();

        AlertDialog dialog = builder.create();

        return dialog;
    }

    private void findViews(LayoutInflater inflater) {
        loginView = inflater.inflate(R.layout.dialog_login, null);
        btnLogin = (Button) loginView.findViewById(R.id.btn_login_at_dlg_login);
        tvLinkRegister = (TextView) loginView.findViewById(R.id.link_signup_at_dlg_login);
    }

    private void setListener(AlertDialog.Builder builder) {
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginTask().execute();
            }
        });

        tvLinkRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
                LoginDialog.this.getDialog().cancel();

            }
        });

        builder.setView(loginView).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginDialog.this.getDialog().cancel();
            }
        });
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

    private class LoginTask extends AsyncTask<Void, Void, LoginResult> { //Params, Progress, Result
        private String email;

        private String password;

        @Override
        protected void onPreExecute() {
            EditText editText = (EditText) getDialog().findViewById(R.id.input_email);
            this.email = editText.getText().toString();

            editText = (EditText) getDialog().findViewById(R.id.input_password);
            this.password = editText.getText().toString();
        }

        @Override
        protected LoginResult doInBackground(Void... params) {
            String url = resources.getString(R.string.base_url) + resources.getString(R.string.api_login);

            // Populate the HTTP Basic Authentitcation header with the username and password
            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate(true);
            restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
            formData.add("email", email);
            formData.add("password", password);

            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);
            try {
                ResponseEntity<LoginResult> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, LoginResult.class);
                return response.getBody();
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(LoginResult result) {
            System.out.println(result);
        }
    }
}
