package com.wearconnectivity;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.JSONArguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WearConnectivityModule extends WearConnectivitySpec
    implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "WearConnectivityModule ";
  private final MessageClient client;

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    client = Wearable.getMessageClient(context);
    Log.d(
        TAG,
        TAG
            + "onMessageReceived listener added when activity is created. Client receives messages.");
    client.addListener(this);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  private List<Node> retrieveNodes(Promise promise) {
    try {
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
      // TODO: implement Runnable to run task in the background thread
      // https://stackoverflow.com/a/64969640/7295772
      return Tasks.await(nodeClient.getConnectedNodes());
    } catch (Exception e) {
      promise.reject(TAG, TAG + "sendMessage failed with exception: " + e);
      return null;
    }
  }

  @ReactMethod
  public void sendMessage(ReadableMap messageData, Promise promise) {
    List<Node> connectedNodes = retrieveNodes(promise);
    if (connectedNodes != null && connectedNodes.size() > 0 && client != null) {
      for (Node connectedNode : connectedNodes) {
        if (connectedNode.isNearby()) {
          sendMessageToClient(messageData, connectedNode, promise);
        }
      }
    } else {
      promise.reject(
          TAG,
          TAG
              + "sendMessage failed. No connected nodes found. client: "
              + client
              + " connectedNodes: "
              + connectedNodes);
    }
  }

  private void sendMessageToClient(ReadableMap messageData, Node node, Promise promise) {
    try {
      // the last parameter is for file transfer (for ex. audio)
      Log.d(TAG, TAG + "messageData.toString(): " + messageData.toString());
      JSONObject messageJSON = new JSONObject(messageData.toHashMap());
      Task<Integer> sendTask = client.sendMessage(node.getId(), messageJSON.toString(), null);
      OnSuccessListener<Object> onSuccessListener =
          object ->
              promise.resolve(TAG + "message sent to client with nodeID: " + object.toString());
      OnFailureListener onFailureListener =
          object ->
              promise.resolve(TAG + "message sent to client with nodeID: " + object.toString());
      sendTask.addOnSuccessListener(onSuccessListener);
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      promise.reject(TAG, TAG + "sendMessage failed: " + e);
    }
  }

  public void onMessageReceived(MessageEvent messageEvent) {
    try {
      JSONObject jsonObject = new JSONObject(messageEvent.getPath());
      WritableMap messageAsWritableMap = (WritableMap) JSONArguments.fromJSONObject(jsonObject);
      String event = jsonObject.getString("event");
      sendEvent(getReactApplicationContext(), event, messageAsWritableMap);
    } catch (JSONException e) {
      FLog.w(TAG, TAG + "onMessageReceived with message: "
              + messageEvent.getPath() + " failed with error: " + e);
    }
  }

  private void sendEvent(
      ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @Override
  public void onHostResume() {
    if (client != null) {
      Log.d(
          TAG,
          TAG
              + "onMessageReceived listener added when activity is resumed. Client receives messages.");
      client.addListener(this);
    }
  }

  @Override
  public void onHostPause() {
    Log.d(
        TAG,
        TAG
            + "onMessageReceived listener removed when the activity paused. Client does not receive messages.");
    client.removeListener(this);
  }

  @Override
  public void onHostDestroy() {
    Log.d(
        TAG,
        TAG
            + "onMessageReceived listener removed when activity is destroyed. Client does not receive messages.");
    client.removeListener(this);
  }
}
