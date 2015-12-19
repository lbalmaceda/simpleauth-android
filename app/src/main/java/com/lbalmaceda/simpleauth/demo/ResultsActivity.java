package com.lbalmaceda.simpleauth.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

/**
 * Created by lbalmaceda on 12/15/15.
 */
public class ResultsActivity extends AppCompatActivity {

    public static final String KEY_TOKEN = "key_token";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_activity);

        Bundle extras = getIntent().getExtras();
        String token = "error";
        if (extras != null) {
            token = extras.getString(KEY_TOKEN);
        }

        EditText resultInput = (EditText) findViewById(R.id.result_input);
        resultInput.setText(token);
    }
}
