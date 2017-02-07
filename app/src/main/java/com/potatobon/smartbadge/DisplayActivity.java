package com.potatobon.smartbadge;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;

public class DisplayActivity extends Activity implements DisplayContract.View {

    private static final String TAG = DisplayActivity.class.getSimpleName();
    /**
     * I2C bus the segment display is connected to.
     */
    private static final String I2C_BUS = BoardDefaults.getI2CPort();

    private AlphanumericDisplay segmentDisplay;
    private DisplayPresenter presenter;

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
        presenter = new DisplayPresenter(this, cm, getString(R.string.service_id), getPackageName());
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
        if (segmentDisplay != null) {
            Log.i(TAG, "Closing display");
            try {
                segmentDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                segmentDisplay = null;
            }
        }
    }

    @Override
    public void displayText(String message) {
        receivedMessageTextView.setText(String.format("Received: %s", message));
        try {
            segmentDisplay = new AlphanumericDisplay(I2C_BUS);
            segmentDisplay.setBrightness(1.0f);
            segmentDisplay.setEnabled(true);
            segmentDisplay.clear();
            segmentDisplay.display(message);
        } catch (IOException e) {
            Log.e(TAG, "Error configuring display", e);
        }
    }

    @Override
    public void displayStatus(String status) {
        statusTextView.setText(String.format("Status: %s", status));
    }
}
