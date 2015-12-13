package com.lbalmaceda.simpleauth.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.lbalmaceda.simpleauth.AuthMode;
import com.lbalmaceda.simpleauth.SimpleAuthActivity;

/**
 * Created by lbalmaceda on 12/13/15.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOGIN_RESULT = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(MainActivity.this, SimpleAuthActivity.class);
        i.putExtra(SimpleAuthActivity.KEY_AUTH_DOMAIN, Constants.AUTH_DOMAIN);
        i.putExtra(SimpleAuthActivity.KEY_AUTH_CLIENT_ID, Constants.AUTH_CLIENT_ID);
        i.putExtra(SimpleAuthActivity.KEY_AUTH_MODE, AuthMode.SOCIAL);
        startActivityForResult(i, LOGIN_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_RESULT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                String token = data.getStringExtra(SimpleAuthActivity.EXTRA_TOKEN);
                Log.d(TAG, String.format("Received token: %s", token));
            } else {
                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
