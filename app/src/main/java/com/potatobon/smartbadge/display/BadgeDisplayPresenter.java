package com.potatobon.smartbadge.display;

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

import java.util.ArrayList;
import java.util.List;

public class BadgeDisplayPresenter implements BadgeDisplayContract.Presenter,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Connections.ConnectionRequestListener, Connections.MessageListener {


    private static final String TAG = "BadgeDisplayPresenter";

    private BadgeDisplayContract.View view;
    private ConnectivityManager connectivityManager;

    private final String serviceId;
    private final String packageName;
    private final GoogleApiClient googleApiClient;

    public BadgeDisplayPresenter(Context context, ConnectivityManager connectivityManager, String serviceId, String packageName) {
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
    public void registerView(BadgeDisplayContract.View view) {
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
                        Status status = result.getStatus();
                        if (status.isSuccess()) {
                            view.displayStatus("Advertising result: SUCCESS");
                        } else {
                            view.displayStatus("Advertising result: FAILURE " + status.getStatusCode());
                            int statusCode = status.getStatusCode();
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
                            view.displayStatus("Accept connection request: SUCCESS");
                        } else {
                            view.displayStatus("Accept connection request: FAILURE " + status.getStatusMessage());
                        }
                    }
                });
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {
        Log.d(TAG, "onMessageReceived");
        String text = new String(bytes);
        view.displayText(text);
    }

    @Override
    public void onDisconnected(String s) {

    }
}
