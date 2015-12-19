package com.lbalmaceda.simpleauth.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.lbalmaceda.simpleauth.AuthMode;
import com.lbalmaceda.simpleauth.SimpleAuthActivity;
import com.lbalmaceda.simpleauth.SimpleAuthFlow;

/**
 * Created by lbalmaceda on 12/13/15.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOGIN_REQUEST = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final Spinner spinner = (Spinner) findViewById(R.id.login_mode_spinner);
        final String[] items = getResources().getStringArray(R.array.login_modes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, items);
        spinner.setAdapter(adapter);

        Button loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = spinner.getSelectedItemPosition();
                switch (pos) {
                    case 0:
                        beginLoginFlow(AuthMode.EMAIL);
                        break;
                    case 1:
                        beginLoginFlow(AuthMode.SOCIAL);
                        break;
                    case 2:
                        beginLoginFlow(AuthMode.BOTH);
                        break;
                }
            }
        });

    }


    /**
     * Starts the login activity with SimpleAuth library
     *
     * @param mode a valid SimpleAuth AuthMode
     */
    private void beginLoginFlow(AuthMode mode) {
        Intent i = new Intent(MainActivity.this, SimpleAuthActivity.class);
        i.putExtra(SimpleAuthFlow.KEY_AUTH_DOMAIN, Constants.AUTH_DOMAIN);
        i.putExtra(SimpleAuthFlow.KEY_AUTH_CLIENT_ID, Constants.AUTH_CLIENT_ID);
        i.putExtra(SimpleAuthFlow.KEY_AUTH_MODE, mode);
        startActivityForResult(i, LOGIN_REQUEST);
    }

    /**
     * Shows the auth token in a new activity
     *
     * @param authToken the logged in user auth token.
     */
    private void showResult(String authToken) {
        Intent i = new Intent(MainActivity.this, ResultsActivity.class);
        i.putExtra(ResultsActivity.KEY_TOKEN, authToken);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                String token = data.getStringExtra(SimpleAuthFlow.EXTRA_TOKEN);
                Log.d(TAG, String.format("Received token: %s", token));
                showResult(token);
            } else {
                Toast.makeText(MainActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
