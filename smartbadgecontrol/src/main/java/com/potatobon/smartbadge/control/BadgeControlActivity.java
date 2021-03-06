package com.potatobon.smartbadge.control;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class BadgeControlActivity extends AppCompatActivity implements BadgeControlContract.View {

    private static final String TAG = BadgeControlActivity.class.getSimpleName();
    private BadgeControlPresenter presenter;

    private AppCompatButton sendButton;
    private EditText textToDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (AppCompatButton) findViewById(R.id.send_text);
        textToDisplay = (EditText) findViewById(R.id.text_to_display);

        presenter = new BadgeControlPresenter(this, getString(R.string.service_id));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = textToDisplay.getText().toString();
                Log.d(TAG, "Text to send: " + text);
                presenter.setTextToDisplay(text);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.registerView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unregisterView();
    }

    @Override
    public void showConnectedToMessage(String endpointName) {
        Toast.makeText(getApplicationContext(), getString(R.string.connected_to, endpointName), Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void showApiNotConnected() {
        Toast.makeText(getApplicationContext(), getString(R.string.google_api_not_connected), Toast.LENGTH_LONG).show();
    }
}
