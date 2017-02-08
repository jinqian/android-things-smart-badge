package com.potatobon.smartbadge.display;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;

public class BadgeDisplayActivity extends Activity implements BadgeDisplayContract.View {

    private static final String TAG = BadgeDisplayActivity.class.getSimpleName();
    /**
     * I2C bus the segment display is connected to.
     */
    private static final String I2C_BUS = BoardDefaults.getI2CPort();

    private BadgeDisplayPresenter presenter;
    private TextView statusTextView;
    private TextView receivedMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = (TextView) findViewById(R.id.status);
        receivedMessageTextView = (TextView) findViewById(R.id.received_message);

        Log.i(TAG, "Starting SegmentDisplayActivity");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        presenter = new BadgeDisplayPresenter(this, cm, getString(R.string.service_id), getPackageName());
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void displayText(String message) {
        receivedMessageTextView.setText(String.format("Received: %s", message));
        try {
            AlphanumericDisplay segmentDisplay = new AlphanumericDisplay(I2C_BUS);
            segmentDisplay.setBrightness(1.0f);
            segmentDisplay.setEnabled(true);
            segmentDisplay.clear();
            segmentDisplay.display(message);
            segmentDisplay.close();
        } catch (IOException e) {
            statusTextView.setText("Error configuring display.");
        }
    }

    @Override
    public void displayStatus(String status) {
        statusTextView.setText(String.format("Status: %s", status));
    }
}
