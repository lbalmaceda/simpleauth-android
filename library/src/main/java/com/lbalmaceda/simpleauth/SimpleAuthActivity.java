package com.lbalmaceda.simpleauth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by lbalmaceda on 12/12/15.
 */
public class SimpleAuthActivity extends AppCompatActivity {

    public static final String TAG = SimpleAuthActivity.class.getSimpleName();

    public static final String KEY_AUTH_CLIENT_ID = "key_auth_client_id";
    public static final String KEY_AUTH_DOMAIN = "key_auth_domain";
    public static final String KEY_AUTH_MODE = "key_auth_mode";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String authClientId = null;
        String authDomain = null;
        AuthMode authMode = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            authClientId = extras.getString(KEY_AUTH_CLIENT_ID);
            authDomain = extras.getString(KEY_AUTH_DOMAIN);
            try {
                authMode = (AuthMode) extras.getSerializable(KEY_AUTH_MODE);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("AuthMode must be one of the following: AuthMode.Social, AuthMode.Email, AuthMode.Both");
            }
        }
        if (authClientId == null || authDomain == null || authMode == null) {
            throw new IllegalArgumentException("Missing some of these parameters: AuthClientID, AuthDomain, AuthMode");
        }

        //Valid params
        Log.d(TAG, String.format("Params: %s, %s, %s", authClientId, authDomain, authMode));


        //Get the view
        setContentView(R.layout.simpleauth_activity);

        Button fbBtn = (Button) findViewById(R.id.simpleauth_fb_btn);
        Button twBtn = (Button) findViewById(R.id.simpleauth_tw_btn);
        EditText emailInput = (EditText) findViewById(R.id.simpleauth_email_input);
        EditText passwordInput = (EditText) findViewById(R.id.simpleauth_password_input);
        Button loginBtn = (Button) findViewById(R.id.simpleauth_login_btn);

    }
}
