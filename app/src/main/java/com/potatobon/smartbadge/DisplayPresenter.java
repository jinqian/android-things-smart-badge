package com.potatobon.smartbadge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DisplayPresenter implements DisplayContract.Presenter,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Connections.ConnectionRequestListener, Connections.MessageListener {


    private static final String TAG = "DisplayPresenter";

    private DisplayContract.View view;
    private ConnectivityManager connectivityManager;

    private final String serviceId;
    private final String packageName;
    private final GoogleApiClient googleApiClient;

    public DisplayPresenter(Context context, ConnectivityManager connectivityManager, String serviceId, String packageName) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
        this.connectivityManager = connectivityManager;
        this.serviceId = serviceId;
        this.packageName = packageName;
    }

    @Override
    public void unregisterView() {
        this.view = null;
        googleApiClient.disconnect();
    }

    @Override
    public void registerView(DisplayContract.View view) {
        this.view = view;
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected!");
        view.displayStatus("onConnected!");
        startAdvertising();
    }

    private void startAdvertising() {
        Log.d(TAG, "startAdvertising");
        view.displayStatus("startAdvertising!");

        if (!isConnectedToNetwork()) {
            Log.d(TAG, "Not connected to internet.");
            view.displayStatus("Not connected to internet!");
            return;
        }

        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(packageName));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);
        Nearby.Connections.startAdvertising(googleApiClient, serviceId, appMetadata, 0L, this)
                .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
                    @Override
                    public void onResult(@NonNull Connections.StartAdvertisingResult result) {
                        Log.d(TAG, "startAdvertising:onResult:" + result);
                        if (result.getStatus().isSuccess()) {
                            Log.d(TAG, "startAdvertising:onResult: SUCCESS");

                        } else {
                            Log.d(TAG, "startAdvertising:onResult: FAILURE ");
                            int statusCode = result.getStatus().getStatusCode();
                            if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                                Log.d(TAG, "STATUS_ALREADY_ADVERTISING");
                            } else {
                                Log.d(TAG, "STATE_READY");
                            }
                        }
                    }
                });
    }

    private boolean isConnectedToNetwork() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended!");
        view.displayStatus("onConnectionSuspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed!");
        view.displayStatus("onConnectionFailed!");
    }

    @Override
    public void onConnectionRequest(String endpointId, String deviceId, String endpointName, byte[] payload) {
        Nearby.Connections.acceptConnectionRequest(googleApiClient, endpointId, payload, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "acceptConnectionRequest: SUCCESS");

                        } else {
                            Log.d(TAG, "acceptConnectionRequest: FAILURE");
                        }
                    }
                });
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        Log.d(TAG, "onMessageReceived");
        String text = String.valueOf(ByteBuffer.wrap(bytes).getChar());
        view.displayText(text);
    }

    @Override
    public void onDisconnected(String s) {

    }
}
