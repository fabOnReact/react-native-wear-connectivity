package com.wearconnectivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSONArguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.HeadlessJsTaskService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WearConnectivityMessageClient implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {

    private static final String TAG = "WearConnectivityMessageClient";
    private final MessageClient messageClient;
    private final ReactApplicationContext reactContext;
    private boolean isListenerAdded;

    public WearConnectivityMessageClient(ReactApplicationContext context) {
        this.reactContext = context;
        this.messageClient = Wearable.getMessageClient(context);
        messageClient.addListener(this);
        context.addLifecycleEventListener(this);
    }

    /**
     * Sends a message to the first nearby node among the provided connectedNodes.
     * If no nearby node is found, it invokes the error callback.
     */
    public void sendMessage(ReadableMap messageData, List<Node> connectedNodes, Callback replyCb, Callback errorCb) {
        for (Node node : connectedNodes) {
            if (node.isNearby()) {
                sendMessageToClient(messageData, node, replyCb, errorCb);
                return;
            }
        }
        errorCb.invoke("No nearby node found");
    }

    /**
     * Called when a message is received.
     * Forwards the message to a HeadlessJs service.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        try {
            JSONObject jsonObject = new JSONObject(messageEvent.getPath());
            WritableMap messageAsWritableMap = (WritableMap) JSONArguments.fromJSONObject(jsonObject);
            String event = jsonObject.getString("event");
            FLog.w(TAG, TAG + " event: " + event + " message: " + messageAsWritableMap);
            Intent service = new Intent(reactContext, com.wearconnectivity.WearConnectivityTask.class);
            Bundle bundle = Arguments.toBundle(messageAsWritableMap);
            service.putExtras(bundle);
            reactContext.startForegroundService(service);
            HeadlessJsTaskService.acquireWakeLockNow(reactContext);
        } catch (JSONException e) {
            FLog.w(TAG, TAG + " onMessageReceived with message: " + messageEvent.getPath() + " failed with error: " + e);
        }
    }

    @Override
    public void onHostResume() {
        if (messageClient != null && !isListenerAdded) {
            Log.d(TAG, "Adding listener on host resume");
            messageClient.addListener(this);
            isListenerAdded = true;
        }
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause: leaving listener active for background events");
    }

    @Override
    public void onHostDestroy() {
        if (messageClient != null && isListenerAdded) {
            Log.d(TAG, "Removing listener on host destroy");
            messageClient.removeListener(this);
            isListenerAdded = false;
        }
    }

    /**
     * Helper method that sends a message to a specific node.
     */
    private void sendMessageToClient(ReadableMap messageData, Node node, Callback replyCb, Callback errorCb) {
        OnSuccessListener<Object> onSuccessListener = object -> replyCb.invoke("message sent to client with nodeID: " + object.toString());
        OnFailureListener onFailureListener = error -> errorCb.invoke("message sending failed: " + error.toString());
        try {
            JSONObject messageJSON = new JSONObject(messageData.toHashMap());
            Task<Integer> sendTask = messageClient.sendMessage(node.getId(), messageJSON.toString(), null);
            sendTask.addOnSuccessListener(onSuccessListener);
            sendTask.addOnFailureListener(onFailureListener);
        } catch (Exception e) {
            errorCb.invoke("sendMessage failed: " + e);
        }
    }
}