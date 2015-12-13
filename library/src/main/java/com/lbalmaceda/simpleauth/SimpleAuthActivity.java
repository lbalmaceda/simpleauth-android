package com.lbalmaceda.simpleauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lbalmaceda.simpleauth.net.SocialConnection;

import java.util.UUID;


/**
 * Created by lbalmaceda on 12/12/15.
 */
public class SimpleAuthActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = SimpleAuthActivity.class.getSimpleName();

    public static final String KEY_AUTH_CLIENT_ID = "key_auth_client_id";
    public static final String KEY_AUTH_DOMAIN = "key_auth_domain";
    public static final String KEY_AUTH_MODE = "key_auth_mode";
    public static final String EXTRA_TOKEN = "extra_token";

    private static final String DEEPLINK_SCHEME = "simpleauth";
    private static final String HASH_STATE = "state=";
    private static final String HASH_ACCESS_TOKEN = "access_token=";
    private static final String KEY_LAST_STATE = "key_last_state";

    private String mAuthClientId;
    private String mAuthDomain;

    private AuthMode mAuthMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();

        //Get the view
        setContentView(R.layout.simpleauth_activity);

        Button fbBtn = (Button) findViewById(R.id.simpleauth_fb_btn);
        Button twBtn = (Button) findViewById(R.id.simpleauth_tw_btn);
        EditText emailInput = (EditText) findViewById(R.id.simpleauth_email_input);
        EditText passwordInput = (EditText) findViewById(R.id.simpleauth_password_input);
        Button loginBtn = (Button) findViewById(R.id.simpleauth_login_btn);
        fbBtn.setOnClickListener(this);
        twBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);

        fbBtn.setVisibility(mAuthMode == AuthMode.SOCIAL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);
        twBtn.setVisibility(mAuthMode == AuthMode.SOCIAL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);
        emailInput.setVisibility(mAuthMode == AuthMode.EMAIL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);
        passwordInput.setVisibility(mAuthMode == AuthMode.EMAIL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);
        loginBtn.setVisibility(mAuthMode == AuthMode.EMAIL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);
    }

    private void handleIntent() {
        Uri data = getIntent().getData();
        if (data != null && data.getScheme().equals(DEEPLINK_SCHEME)) {
            //coming from a social redirect
            String fragment = data.getFragment();
            if (fragment == null) {
                //FIXME: handle error
            }
            int tokenStart = fragment.indexOf(HASH_ACCESS_TOKEN) + HASH_ACCESS_TOKEN.length();
            int tokenEnd = fragment.indexOf("&", tokenStart);
            if (tokenEnd == -1) {
                tokenEnd = fragment.length();
            }
            String resultToken = fragment.substring(tokenStart, tokenEnd);
            int stateStart = fragment.indexOf(HASH_STATE) + HASH_STATE.length();
            int stateEnd = fragment.indexOf("&", stateStart);
            if (stateEnd == -1) {
                stateEnd = fragment.length();
            }
            String resultState = fragment.substring(stateStart, stateEnd);
            String lastState = getLastState();
            boolean stateIsOK = lastState.equals(resultState);
            Log.d(TAG, String.format("Result: %s, %s, %b", resultToken, resultState, stateIsOK));

            if (stateIsOK) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_TOKEN, resultToken);
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();

        } else {
            //should be the first call, or coming back with a canceled state
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mAuthClientId = extras.getString(KEY_AUTH_CLIENT_ID);
                mAuthDomain = extras.getString(KEY_AUTH_DOMAIN);
                try {
                    mAuthMode = (AuthMode) extras.getSerializable(KEY_AUTH_MODE);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("AuthMode must be one of the following: AuthMode.Social, AuthMode.Email, AuthMode.Both");
                }
            }
            if (mAuthClientId == null || mAuthDomain == null || mAuthMode == null) {
                throw new IllegalArgumentException("Missing some of these parameters: AuthClientID, AuthDomain, AuthMode");
            }

            //Valid params
            Log.d(TAG, String.format("Params: %s, %s, %s", mAuthClientId, mAuthDomain, mAuthMode));
        }
    }

    private void performSocialLogin(SocialConnection connection) {

        String redirectUrl = "simpleauth://social";
        String state = UUID.randomUUID().toString();
        saveLastState(state);
        String url = String.format("https://%s.auth0.com/authorize?response_type=token&client_id=%s&connection=%s&redirect_uri=%s&state=%s",
                mAuthDomain, mAuthClientId, connection.toString(), redirectUrl, state);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.simpleauth_fb_btn) {
            performSocialLogin(SocialConnection.FACEBOOK);
        } else if (id == R.id.simpleauth_tw_btn) {
            performSocialLogin(SocialConnection.TWITTER);
        } else if (id == R.id.simpleauth_login_btn) {
        }
    }

    private void saveLastState(String state) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SimpleAuthActivity.this);
        sp.edit().putString(KEY_LAST_STATE, state).apply();
    }

    private String getLastState() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SimpleAuthActivity.this);
        return sp.getString(KEY_LAST_STATE, "");
    }
}
