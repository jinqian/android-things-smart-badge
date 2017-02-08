package com.potatobon.smartbadge.control;

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
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class BadgeControlPresenter implements BadgeControlContract.Presenter,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        Connections.EndpointDiscoveryListener, Connections.MessageListener {

    private static final String TAG = "BadgeControlPresenter";

    private Context context;
    private String otherEndpointId;
    private BadgeControlContract.View view;

    private final GoogleApiClient googleApiClient;
    private final String serviceId;

    public BadgeControlPresenter(Context context, String serviceId) {
        this.context = context;
        this.serviceId = serviceId;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Nearby.CONNECTIONS_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void registerView(BadgeControlContract.View view) {
        this.view = view;
        googleApiClient.connect();
    }

    @Override
    public void unregisterView() {
        this.view = null;
        googleApiClient.disconnect();
    }

    @Override
    public void setTextToDisplay(String message) {
        if (!googleApiClient.isConnected()) {
            view.showApiNotConnected();
        } else {
            Nearby.Connections.sendReliableMessage(googleApiClient, otherEndpointId, message.getBytes());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startDiscovery();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return true;
            }
        }
        return false;
    }

    private void startDiscovery() {
        Log.d(TAG, "startDiscovery");
        if (!isConnectedToNetwork()) {
            Log.d(TAG, "startDiscovery: not connected to WiFi network.");
            return;
        }

        Nearby.Connections.startDiscovery(googleApiClient, serviceId, 0L, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (!isViewRegistered()) {
                            return;
                        }
                        if (status.isSuccess()) {
                            Log.d(TAG, "Discovery result: SUCCESS ");
                        } else {
                            Log.d(TAG, "Discovery result: FAILURE");
                            int statusCode = status.getStatusCode();
                            if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING) {
                                Log.d(TAG, "STATUS_ALREADY_DISCOVERING");
                            }
                        }
                    }
                });
    }

    public boolean isViewRegistered() {
        return view != null;
    }

    @Override
    public void onEndpointFound(String endpointId, String deviceId, String serviceId, String endpointName) {
        Log.d(TAG, "onEndpointFound:" + endpointId + ":" + endpointName);
        connectTo(endpointId, endpointName);
    }

    @Override
    public void onEndpointLost(String s) {
        Log.d(TAG, "onEndpointLost:" + s);
    }

    private void connectTo(String endpointId, final String endpointName) {
        Log.d(TAG, "connectTo:" + endpointId + ":" + endpointName);
        Nearby.Connections.sendConnectionRequest(googleApiClient, null, endpointId, null,
                new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String endpointId, Status status, byte[] bytes) {
                        Log.d(TAG, "onConnectionResponse:" + endpointId + ":" + status);
                        if (!isViewRegistered()) {
                            return;
                        }
                        if (status.isSuccess()) {
                            Log.d(TAG, "onConnectionResponse: " + endpointName + " SUCCESS");
                            view.showConnectedToMessage(endpointName);
                            otherEndpointId = endpointId;
                        } else {
                            Log.d(TAG, "onConnectionResponse: " + endpointName + " FAILURE");
                        }
                    }
                }, this);
    }

    @Override
    public void onMessageReceived(String s, byte[] bytes, boolean b) {

    }

    @Override
    public void onDisconnected(String s) {

    }
}
