package com.lbalmaceda.simpleauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lbalmaceda.simpleauth.net.EPLoginResponse;
import com.lbalmaceda.simpleauth.net.EPRequest;
import com.lbalmaceda.simpleauth.net.LoginAPI;
import com.lbalmaceda.simpleauth.net.SocialConnection;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.UUID;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Created by lbalmaceda on 12/12/15.
 */
public class SimpleAuthActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = SimpleAuthActivity.class.getSimpleName();

    public static final String ACTION_LOGIN_RESULT = "action_login_success";

    public static final String KEY_AUTH_CLIENT_ID = "key_auth_client_id";
    public static final String KEY_AUTH_DOMAIN = "key_auth_domain";
    public static final String KEY_AUTH_MODE = "key_auth_mode";

    public static final String EXTRA_TOKEN = "extra_token";
    public static final String EXTRA_SUCCESS = "extra_success";

    private static final String DEEPLINK_SCHEME = "simpleauth";
    private static final String HASH_STATE = "state=";
    private static final String HASH_ACCESS_TOKEN = "access_token=";
    private static final String KEY_LAST_STATE = "key_last_state";

    private String mAuthClientId;
    private String mAuthDomain;

    private AuthMode mAuthMode;
    private LoginAPI mLoginApi;
    private EditText mEmailInput;
    private EditText mPasswordInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //get the authentication flow settings
            mAuthClientId = extras.getString(KEY_AUTH_CLIENT_ID);
            mAuthDomain = extras.getString(KEY_AUTH_DOMAIN);
            try {
                mAuthMode = (AuthMode) extras.getSerializable(KEY_AUTH_MODE);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("AuthMode must be one of the following: AuthMode.Social, AuthMode.Email, AuthMode.Both");
            }

            if (mAuthClientId == null || mAuthDomain == null || mAuthMode == null) {
                throw new IllegalArgumentException("Missing some of these parameters: AuthClientID, AuthDomain, AuthMode");
            }

            //Valid params
            Log.d(TAG, String.format("Params: %s, %s, %s", mAuthClientId, mAuthDomain, mAuthMode));
            setContentView(R.layout.simpleauth_activity);
            setView();
        }
    }

    /**
     * Loads the layout and its widgets
     */
    private void setView() {
        Button fbBtn = (Button) findViewById(R.id.simpleauth_fb_btn);
        Button twBtn = (Button) findViewById(R.id.simpleauth_tw_btn);
        mEmailInput = (EditText) findViewById(R.id.simpleauth_email_input);
        mPasswordInput = (EditText) findViewById(R.id.simpleauth_password_input);
        mPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO && validEmailPasswordInput()) {
                    String email = mEmailInput.getText().toString().trim();
                    String password = mPasswordInput.getText().toString().trim();
                    performEmailAndPasswordLogin(email, password);
                }
                return false;
            }
        });
        Button loginBtn = (Button) findViewById(R.id.simpleauth_login_btn);
        fbBtn.setOnClickListener(this);
        twBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);

        View socialHolder = findViewById(R.id.simpleauth_social);
        View emailpasswordHolder = findViewById(R.id.simpleauth_email_password);
        socialHolder.setVisibility(mAuthMode == AuthMode.SOCIAL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);
        emailpasswordHolder.setVisibility(mAuthMode == AuthMode.EMAIL || mAuthMode == AuthMode.BOTH ? View.VISIBLE : View.GONE);

        //Only create adapter if that mode is going to be used
        if (mAuthMode == AuthMode.EMAIL || mAuthMode == AuthMode.BOTH) {
            initNetworking();
        }
    }

    /**
     * Handles the redirect response from the social login flow
     *
     * @param intent the new received intent
     */
    private void handleSocialResult(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.getScheme().equals(DEEPLINK_SCHEME)) {
            //coming from a social redirect
            String fragment = data.getFragment();
            if (fragment == null) {
                Log.e(TAG, "Missing response data.");
                sendBackResult(null);
                return;
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
            Log.d(TAG, String.format("Social login result: %s, %s, %b", resultToken, resultState, stateIsOK));
            sendBackResult(resultToken);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "OnNewIntent: " + intent.getData());
        handleSocialResult(intent);
    }

    /**
     * Starts the login flow for the given Social network.
     *
     * @param connection a valid SocialConnection. ex: twitter, facebook
     */
    private void performSocialLogin(SocialConnection connection) {
        Log.d(TAG, "Social login in progress..");
        String redirectUrl = DEEPLINK_SCHEME + "://social";
        String state = UUID.randomUUID().toString();
        saveLastState(state);
        String url = String.format("https://%s.auth0.com/authorize?response_type=token&client_id=%s&connection=%s&redirect_uri=%s&state=%s",
                mAuthDomain, mAuthClientId, connection.toString(), redirectUrl, state);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_FROM_BACKGROUND);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    /**
     * Broadcasts a new intent with the result of the login and finishes this activity.
     *
     * @param token the received login token, or null if an error happened.
     */
    private void sendBackResult(String token) {
        Intent result = new Intent(ACTION_LOGIN_RESULT);
        if (token == null || token.isEmpty()) {
            result.putExtra(EXTRA_SUCCESS, false);
        } else {
            result.putExtra(EXTRA_SUCCESS, true);
            result.putExtra(EXTRA_TOKEN, token);
        }
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(SimpleAuthActivity.this);
        manager.sendBroadcast(result);
        finish();
    }

    /**
     * Initializes the Retrofit network adapter and creates the API interface.
     */
    private void initNetworking() {
        OkHttpClient client = new OkHttpClient();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.interceptors().add(interceptor);
        }
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("https://%s.auth0.com/", mAuthDomain))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mLoginApi = retrofit.create(LoginAPI.class);
    }

    /**
     * Starts the login flow with the given Email and Password.
     * Email must exist and belong to a registered user.
     *
     * @param email    a non empty email string
     * @param password a non empty password string
     */
    private void performEmailAndPasswordLogin(String email, String password) {
        Log.d(TAG, "Email&Password login in progress..");
        EPRequest data = new EPRequest(mAuthClientId, email, password);
        Call<EPLoginResponse> call = mLoginApi.emailLogin(data);
        call.enqueue(new Callback<EPLoginResponse>() {
            @Override
            public void onResponse(Response<EPLoginResponse> response, Retrofit retrofit) {
                if (response.body() == null) {
                    if (response.code() == 401) {
                        Log.e(TAG, "Invalid username or password");
                        Toast.makeText(SimpleAuthActivity.this, R.string.simpleauth_toast_response_invalid_username_password, Toast.LENGTH_SHORT).show();
                        mEmailInput.setText("");
                        mPasswordInput.setText("");
                    } else {
                        Log.e(TAG, "Error parsing the response");
                    }
                    //err
                } else {
                    sendBackResult(response.body().getAccessToken());
                }
                Log.d(TAG, "Email&Password login success");
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(SimpleAuthActivity.this, R.string.simpleauth_toast_connection_error, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Email&Password failure.");
                Log.d(TAG, t.getMessage());
            }
        });
    }

    /**
     * Checks if the email and password input text is valid and shows an error if not.
     *
     * @return whether the email and password input text is valid (non empty).
     */
    private boolean validEmailPasswordInput() {
        String email = mEmailInput.getText().toString().trim();
        String password = mPasswordInput.getText().toString().trim();
        if (email.isEmpty()) {
            mEmailInput.setError(getString(R.string.simpleauth_error_input_email));
        }
        if (password.isEmpty()) {
            mPasswordInput.setError(getString(R.string.simpleauth_error_input_password));
        }
        return !(email.isEmpty() || password.isEmpty());
    }

    /**
     * Saves in the SharedPreferences the last OAuth2 state sent.
     *
     * @param state the state String.
     */
    private void saveLastState(String state) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SimpleAuthActivity.this);
        sp.edit().putString(KEY_LAST_STATE, state).apply();
    }

    /**
     * Retrieves from the SharedPreferences the last OAuth2 state sent.
     *
     * @return the last state String.
     */
    private String getLastState() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SimpleAuthActivity.this);
        return sp.getString(KEY_LAST_STATE, "");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.simpleauth_fb_btn) {
            performSocialLogin(SocialConnection.FACEBOOK);
        } else if (id == R.id.simpleauth_tw_btn) {
            performSocialLogin(SocialConnection.TWITTER);
        } else if (id == R.id.simpleauth_login_btn) {
            if (validEmailPasswordInput()) {
                String email = mEmailInput.getText().toString().trim();
                String password = mPasswordInput.getText().toString().trim();
                performEmailAndPasswordLogin(email, password);
            }
        }
    }
}
