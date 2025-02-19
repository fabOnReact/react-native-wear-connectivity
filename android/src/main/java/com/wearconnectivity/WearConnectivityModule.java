package com.wearconnectivity;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSONArguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.common.GoogleApiAvailability;

import androidx.annotation.RequiresApi;
import com.facebook.react.HeadlessJsTaskService;
import android.content.Intent;
import android.os.Bundle;

public class WearConnectivityModule extends WearConnectivitySpec
    implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {

  private static ReactApplicationContext reactContext;
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "react-native-wear-connectivity ";
  private final MessageClient messageClient;
  private final WearConnectivityDataClient dataClient;
  private boolean isListenerAdded = false;

  private String CLIENT_ADDED =
      TAG + "onMessageReceived listener added when activity is created. Client receives messages.";
  private String NO_NODES_FOUND = TAG + "sendMessage failed. No connected nodes found.";
  private String REMOVE_CLIENT =
      TAG
          + "onMessageReceived listener removed when activity is destroyed. Client does not receive messages.";
  private String ADD_CLIENT =
      TAG + "onMessageReceived listener added when activity is resumed. Client receives messages.";
  private String RETRIEVE_NODES_FAILED = TAG + "failed to retrieve nodes with error: ";
  private String CONNECTED_DEVICE_IS_FAR = " Device is too far for bluetooth connection. ";
  private String INSTALL_GOOGLE_PLAY_WEARABLE = "The Android mobile phone needs to install the Google Play Wear app. ";
  private String MISSING_GOOGLE_PLAY_SERVICES = "GooglePlay Services not available.";

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    context.addLifecycleEventListener(this);
    messageClient = Wearable.getMessageClient(context);
    dataClient = new WearConnectivityDataClient(context);
    Log.d(TAG, CLIENT_ADDED);
    messageClient.addListener(this);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void sendFile(String fileName, Promise promise) {
    if (dataClient != null) {
      dataClient.sendFile(fileName, promise);
    } else {
      promise.reject("E_SEND_FAILED", "Failed to send file");
    }
  }

  @ReactMethod
  public void sendMessage(ReadableMap messageData, Callback replyCb, Callback errorCb) {
    List<Node> connectedNodes = retrieveNodes(errorCb);
    if (connectedNodes != null && connectedNodes.size() > 0 && messageClient != null) {
      for (Node connectedNode : connectedNodes) {
        if (connectedNode.isNearby()) {
          sendMessageToClient(messageData, connectedNode, replyCb, errorCb);
        } else {
          FLog.w(
                  TAG,
                  TAG
                          + "connectedNode: "
                          + connectedNode.getDisplayName()
                          + CONNECTED_DEVICE_IS_FAR);
        }
      }
    }
  }

  /*
  @ReactMethod
  public void sendFile(String uri, ReadableMap metadata, Promise promise) {
    // Retrieve connected nodes; adjust retrieveNodes if needed to remove error callback
    List<Node> connectedNodes = retrieveNodes(null);
    if (connectedNodes == null || connectedNodes.isEmpty() || messageClient == null) {
      promise.reject("E_NO_NODE", "No connected nodes found or messageClient is null");
      return;
    }

    // Loop through connected nodes and send the message to the first nearby node.
    for (Node connectedNode : connectedNodes) {
      if (connectedNode.isNearby()) {
        sendMessageToClient(
                messageData,
                connectedNode,
                new Callback() {
                  @Override
                  public void invoke(Object... args) {
                    // Resolve promise with the reply data (if any)
                    if (args.length > 0) {
                      promise.resolve(args[0]);
                    } else {
                      promise.resolve(null);
                    }
                  }
                },
                new Callback() {
                  @Override
                  public void invoke(Object... args) {
                    // Reject the promise with an error message
                    if (args.length > 0 && args[0] instanceof String) {
                      promise.reject("E_SEND_FAILED", (String) args[0]);
                    } else {
                      promise.reject("E_SEND_FAILED", "Failed to send message");
                    }
                  }
                }
        );
        return; // Stop after sending to the first nearby node
      }
    }

    // If no nearby node was found, reject the promise.
    promise.reject("E_NO_NEARBY_NODE", "No nearby node found");
  }*/


  private void sendMessageToClient(
      ReadableMap messageData, Node node, Callback replyCb, Callback errorCb) {
    OnSuccessListener<Object> onSuccessListener =
        object -> replyCb.invoke("message sent to client with nodeID: " + object.toString());
    OnFailureListener onFailureListener =
        object -> errorCb.invoke("message sent to client with nodeID: " + object.toString());
    try {
      // the last parameter is for file transfer (for ex. audio)
      JSONObject messageJSON = new JSONObject(messageData.toHashMap());
      Task<Integer> sendTask = messageClient.sendMessage(node.getId(), messageJSON.toString(), null);
      sendTask.addOnSuccessListener(onSuccessListener);
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      errorCb.invoke("sendMessage failed: " + e);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    ReactApplicationContext context = getReactContext();
    try {
      JSONObject jsonObject = new JSONObject(messageEvent.getPath());
      WritableMap messageAsWritableMap = (WritableMap) JSONArguments.fromJSONObject(jsonObject);
      String event = jsonObject.getString("event");
      FLog.w(TAG, TAG + " event: " + event + " message: " + messageAsWritableMap);
      Intent service = new Intent(getReactContext(), com.wearconnectivity.WearConnectivityTask.class);
      Bundle bundle = Arguments.toBundle(messageAsWritableMap);
      service.putExtras(bundle);
      getReactContext().startForegroundService(service);
      HeadlessJsTaskService.acquireWakeLockNow(getReactContext());
    } catch (JSONException e) {
      FLog.w(
              TAG,
              TAG
                      + "onMessageReceived with message: "
                      + messageEvent.getPath()
                      + " failed with error: "
                      + e);
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

  private List<Node> retrieveNodes(Callback errorCb) {
    try {
      int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getReactContext());
      ConnectionResult connectionResult = new ConnectionResult(result);
      if (!connectionResult.isSuccess()) {
        errorCb.invoke( MISSING_GOOGLE_PLAY_SERVICES + connectionResult.getErrorMessage());
        return null;
      }
      NodeClient nodeClient = Wearable.getNodeClient(getReactContext());
      try {
        Tasks.await(GoogleApiAvailability.getInstance().checkApiAvailability(nodeClient));
      } catch (Exception e) {
        errorCb.invoke(INSTALL_GOOGLE_PLAY_WEARABLE + e);
        return null;
      }
      return Tasks.await(nodeClient.getConnectedNodes());
    } catch (Exception e) {
      errorCb.invoke(RETRIEVE_NODES_FAILED + e);
      return null;
    }
  }

  private static ReactApplicationContext getReactContext() {
    return reactContext;
  }
}
