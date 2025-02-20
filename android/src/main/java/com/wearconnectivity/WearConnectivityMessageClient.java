package com.wearconnectivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSONArguments;
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

public class WearConnectivityMessageClient implements MessageClient.OnMessageReceivedListener {

    private static final String TAG = "WearConnectivityMessageClient";
    private final MessageClient messageClient;
    private final ReactApplicationContext reactContext;

    public WearConnectivityMessageClient(ReactApplicationContext context) {
        this.reactContext = context;
        this.messageClient = Wearable.getMessageClient(context);
    }

    public MessageClient getMessageClient() {
        return messageClient;
    }

    public void addListener() {
        messageClient.addListener(this);
    }

    public void removeListener() {
        messageClient.removeListener(this);
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

    /**
     * Called when a message is received.
     * Forwards the message to a HeadlessJs service.
     */
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
}