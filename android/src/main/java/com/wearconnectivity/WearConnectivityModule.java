package com.wearconnectivity;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSONArguments;
import com.facebook.react.bridge.LifecycleEventListener;
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
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class WearConnectivityModule extends WearConnectivitySpec
    implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "WearConnectivityModule ";
  private final MessageClient client;
  private String CLIENT_ADDED =
      TAG + "onMessageReceived listener added when activity is created. Client receives messages.";
  private String NO_NODES_FOUND = TAG + "sendMessage failed. No connected nodes found.";
  private String REMOVE_CLIENT =
      TAG
          + "onMessageReceived listener removed when activity is destroyed. Client does not receive messages.";
  private String ADD_CLIENT =
      TAG + "onMessageReceived listener added when activity is resumed. Client receives messages.";
  private String RETRIEVE_NODES_FAILED = TAG + "failed to retrieve nodes with error: ";

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    client = Wearable.getMessageClient(context);
    Log.d(TAG, CLIENT_ADDED);
    client.addListener(this);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  private List<Node> retrieveNodes(Callback errorCb) {
    try {
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
      // TODO: implement Runnable to run task in the background thread
      // https://stackoverflow.com/a/64969640/7295772
      return Tasks.await(nodeClient.getConnectedNodes());
    } catch (Exception e) {
      errorCb.invoke(RETRIEVE_NODES_FAILED + e);
      return null;
    }
  }

  @ReactMethod
  public void sendMessage(ReadableMap messageData, Callback replyCb, Callback errorCb) {
    List<Node> connectedNodes = retrieveNodes(errorCb);
    if (connectedNodes != null && connectedNodes.size() > 0 && client != null) {
      for (Node connectedNode : connectedNodes) {
        if (connectedNode.isNearby()) {
          sendMessageToClient(messageData, connectedNode, replyCb, errorCb);
        }
      }
    } else {
      FLog.w(TAG, NO_NODES_FOUND + " client: " + client + " connectedNodes: " + connectedNodes);
    }
  }

  private void sendMessageToClient(
      ReadableMap messageData, Node node, Callback replyCb, Callback errorCb) {
    OnSuccessListener<Object> onSuccessListener =
        object -> replyCb.invoke("message sent to client with nodeID: " + object.toString());
    OnFailureListener onFailureListener =
        object -> errorCb.invoke("message sent to client with nodeID: " + object.toString());
    try {
      // the last parameter is for file transfer (for ex. audio)
      JSONObject messageJSON = new JSONObject(messageData.toHashMap());
      Task<Integer> sendTask = client.sendMessage(node.getId(), messageJSON.toString(), null);
      sendTask.addOnSuccessListener(onSuccessListener);
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      errorCb.invoke("sendMessage failed: " + e);
    }
  }

  public void onMessageReceived(MessageEvent messageEvent) {
    try {
      JSONObject jsonObject = new JSONObject(messageEvent.getPath());
      WritableMap messageAsWritableMap = (WritableMap) JSONArguments.fromJSONObject(jsonObject);
      String event = jsonObject.getString("event");
      FLog.w(TAG, TAG + " event: " + event + " message: " + messageAsWritableMap);
      sendEvent(getReactApplicationContext(), event, messageAsWritableMap);
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

  private void sendEvent(
      ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @Override
  public void onHostResume() {
    if (client != null) {
      Log.d(TAG, ADD_CLIENT);
      client.addListener(this);
    }
  }

  @Override
  public void onHostPause() {
    Log.d(TAG, REMOVE_CLIENT);
    client.removeListener(this);
  }

  @Override
  public void onHostDestroy() {
    Log.d(TAG, REMOVE_CLIENT);
    client.removeListener(this);
  }
}
