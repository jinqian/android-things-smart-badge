package com.potatobon.smartbadge;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;

public class DisplayActivity extends Activity {

    private static final String TAG = DisplayActivity.class.getSimpleName();
    /**
     * I2C bus the segment display is connected to.
     */
    private static final String I2C_BUS = BoardDefaults.getI2CPort();

    private AlphanumericDisplay mSegmentDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting SegmentDisplayActivity");

        try {
            mSegmentDisplay = new AlphanumericDisplay(I2C_BUS);
            mSegmentDisplay.setBrightness(1.0f);
            mSegmentDisplay.setEnabled(true);
            mSegmentDisplay.clear();
            mSegmentDisplay.display("QIAN");
        } catch (IOException e) {
            Log.e(TAG, "Error configuring display", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSegmentDisplay != null) {
            Log.i(TAG, "Closing display");
            try {
                mSegmentDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mSegmentDisplay = null;
            }
        }
    }
}
