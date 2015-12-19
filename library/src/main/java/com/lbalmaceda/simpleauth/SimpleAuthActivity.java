package com.lbalmaceda.simpleauth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by lbalmaceda on 12/19/15.
 */
public class SimpleAuthActivity extends AppCompatActivity {

    private static final String TAG = SimpleAuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter loginResultFilter = new IntentFilter(SimpleAuthFlow.ACTION_LOGIN_RESULT);
        LocalBroadcastManager.getInstance(SimpleAuthActivity.this).registerReceiver(mLoginResultReceiver, loginResultFilter);

        Intent launchLogin = new Intent(SimpleAuthActivity.this, SimpleAuthFlow.class);
        launchLogin.putExtras(getIntent());
        startActivity(launchLogin);
    }

    @Override
    protected void onDestroy() {
        try {
            LocalBroadcastManager.getInstance(SimpleAuthActivity.this).unregisterReceiver(mLoginResultReceiver);
        } catch (Exception ignored) {
            //not registered
        }
        super.onDestroy();
    }

    /**
     * Listens for SimpleAuth login results
     */
    private final BroadcastReceiver mLoginResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(SimpleAuthFlow.EXTRA_SUCCESS, false);
            if (success) {
                String token = intent.getStringExtra(SimpleAuthFlow.EXTRA_TOKEN);
                Log.d(TAG, String.format("Received token: %s", token));
                Intent resultIntent = new Intent();
                resultIntent.putExtra(SimpleAuthFlow.EXTRA_TOKEN, token);
                SimpleAuthActivity.this.setResult(RESULT_OK, intent);
            } else {
                Toast.makeText(SimpleAuthActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
            }
            SimpleAuthActivity.this.finish();
        }
    };

}
