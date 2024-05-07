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
import java.util.ArrayList;
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

  private List<Node> retrieveNodes() {
    try {
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
      // TODO: implement Runnable to run task in the background thread
      // https://stackoverflow.com/a/64969640/7295772
      return Tasks.await(nodeClient.getConnectedNodes());
    } catch (Exception e) {
      throw new Error(RETRIEVE_NODES_FAILED + e);
    }
  }

  @ReactMethod
  public void sendMessage(ReadableMap messageData, Callback replyCb, Callback errorCb) {
    sendAnyMessage((connectedNode) -> {
      JSONObject messageJSON = new JSONObject(messageData.toHashMap());
      sendMessageToClient(messageJSON.toString(), connectedNode);
    }, replyCb, errorCb);
  }

  @ReactMethod
  public void sendGenuineMessage(String messagePath, Callback replyCb, Callback errorCb) {
    sendAnyMessage((connectedNode) -> {
      sendMessageToClient(messagePath, connectedNode);
    }, replyCb, errorCb);
  }

  private interface SendFunctionInterface {
    public void sendFunction(Node node);
  }

  private void sendAnyMessage(SendFunctionInterface sendFunctionInterface, Callback replyCb, Callback errorCb) {
    try {
      List<String> nodeIds = new ArrayList<>();
      List<Node> connectedNodes = retrieveNodes();
      if (connectedNodes != null && connectedNodes.size() > 0 && client != null) {
        for (Node connectedNode : connectedNodes) {
          sendFunctionInterface.sendFunction(connectedNode);
          nodeIds.add(connectedNode.getId());
        }
      } else {
        throw new Error(NO_NODES_FOUND + " client: " + client + " connectedNodes: " + connectedNodes);
      }
      replyCb.invoke("messages sent to all connected nodes: " + nodeIds);
    } catch (Error e) {
      errorCb.invoke(e.toString());
    }
  }

  private void sendMessageToClient(String messagePath, Node node) {
    OnSuccessListener<Object> onSuccessListener =
      object -> Log.i(TAG, "message \"" + messagePath + "\" sent to client with nodeID: " + node.getId() + " and sent message ID: " + object.toString());
    OnFailureListener onFailureListener =
        object -> { throw new Error("message \\\"\" + messagePath + \"\\\" not sent to client with nodeID: " + node.getId() +  " and sent message ID: " + object.toString()); };
    try {
      Task<Integer> sendTask = client.sendMessage(node.getId(), messagePath, null);
      sendTask.addOnSuccessListener(onSuccessListener);
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      throw new Error("sendMessage failed: " + e);
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
