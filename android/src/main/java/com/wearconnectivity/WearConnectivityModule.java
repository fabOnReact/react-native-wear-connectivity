package com.wearconnectivity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSONArguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

public class WearConnectivityModule extends WearConnectivitySpec
  implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "WearConnectivityModule ";
  private final MessageClient messageClient;
  private final CapabilityClient capabilityClient;
  private String MESSAGE_CLIENT_ADDED =
    TAG + "onMessageReceived listener added when activity is created. Client receives messages.";
  private String NO_NODES_FOUND = TAG + "sendMessage failed. No connected nodes found.";
  private String REMOVE_MESSAGE_CLIENT =
    TAG
      + "onMessageReceived listener removed when activity is destroyed. Client does not receive messages.";
  private String ADD_MESSAGE_CLIENT =
    TAG + "onMessageReceived listener added when activity is resumed. Client receives messages.";
  private String RETRIEVE_NODES_FAILED = TAG + "failed to retrieve nodes with error: ";

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    messageClient = Wearable.getMessageClient(context);
    capabilityClient = Wearable.getCapabilityClient(context);
    Log.d(TAG, MESSAGE_CLIENT_ADDED);
    messageClient.addListener(this);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  private List<Node> _retrieveNodes() {
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
      List<Node> connectedNodes = _retrieveNodes();
      if (connectedNodes != null && connectedNodes.size() > 0 && messageClient != null) {
        for (Node connectedNode : connectedNodes) {
          sendFunctionInterface.sendFunction(connectedNode);
          nodeIds.add(connectedNode.getId());
        }
      } else {
        throw new Error(NO_NODES_FOUND + " messageClient: " + messageClient + " connectedNodes: " + connectedNodes);
      }
      replyCb.invoke("messages sent to all connected nodes: " + nodeIds);
    } catch (Error e) {
      errorCb.invoke(e.toString());
    }
  }

  private void sendMessageToClient(String messagePath, Node node) {
    OnSuccessListener<Object> onSuccessListener =
      object -> Log.i(TAG, "message \"" + messagePath + "\" sent to messageClient with nodeID: " + node.getId() + " and sent message ID: " + object.toString());
    OnFailureListener onFailureListener =
      object -> {
        throw new Error("message \\\"\" + messagePath + \"\\\" not sent to messageClient with nodeID: " + node.getId() + " and sent message ID: " + object.toString());
      };
    try {
      Task<Integer> sendTask = messageClient.sendMessage(node.getId(), messagePath, null);
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
      Log.w(TAG, TAG + " event: " + event + " message: " + messageAsWritableMap);
      sendEvent(getReactApplicationContext(), event, messageAsWritableMap);
    } catch (JSONException e) {
      Log.w(TAG, TAG + "  message: " + messageEvent.getPath());
      WritableMap map = new WritableNativeMap();
      map.putString("text", messageEvent.getPath());
      sendEvent(getReactApplicationContext(), "message", map);
    }
  }

  private void sendEvent(
    ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  private List<Node> _getCapableAndReachableNodes(String capability) throws Exception {
    Task<CapabilityInfo> capabilityInfoTask = capabilityClient
      .getCapability(capability, capabilityClient.FILTER_REACHABLE);
    CapabilityInfo capabilityInfo = Tasks.await(capabilityInfoTask);

    Log.d(TAG, "Capability request succeeded." + capabilityInfo.getNodes().size());

    return new ArrayList<>(capabilityInfo.getNodes());
  }

  private WritableArray _getWritableNodes(List<Node> nodes) {
    return Arguments.makeNativeArray(
      nodes.stream().map(node -> {
        Map<String, String> nodeObj = new HashMap<>();
        nodeObj.put("displayName", node.getDisplayName());
        nodeObj.put("id", node.getId());
        return nodeObj;
      }).collect(Collectors.toList())
    );
  }

  @ReactMethod
  public void getCapableAndReachableNodes(String capability, Callback replyCb, Callback errorCb) {
    try {
      List<Node> nodes = _getCapableAndReachableNodes(capability);
      replyCb.invoke(_getWritableNodes(nodes));
    } catch (CancellationException cancellationException) {
      Log.d(TAG, "Capability request was cancelled. \n reason : " + cancellationException.getMessage());
      errorCb.invoke("Capability request was cancelled. \n reason : " + cancellationException.getMessage());
    } catch (Throwable throwable) {
      Log.d(TAG, "Capability request failed to return any results. \n reason : " + throwable.getMessage());
      errorCb.invoke("Capability request failed to return any results. \n reason : " + throwable.getMessage());
    } 
  }

  @ReactMethod
  public void getNonCapableAndReachableNodes(String capability, Callback replyCb, Callback errorCb) {
    try {
      List<Node> capableNodes = _getCapableAndReachableNodes(capability);
      List<Node> connectedNodes = new ArrayList(_retrieveNodes());

      connectedNodes.removeAll(capableNodes);

      Log.d(TAG, "Non-Capability request succeeded. Connected Nodes" + _retrieveNodes());
      Log.d(TAG, "Non-Capability request succeeded. Capable Nodes" + capableNodes);
      Log.d(TAG, "Non-Capability request succeeded. Non-Capable Nodes" + connectedNodes);

      replyCb.invoke(_getWritableNodes(connectedNodes));
    } catch (CancellationException cancellationException) {
      Log.d(TAG, "Non-Capability request was cancelled. \n reason : " + cancellationException.getMessage());
      errorCb.invoke("Non-Capability request was cancelled. \n reason : " + cancellationException.getMessage());
    } catch (Throwable throwable) {
      Log.d(TAG, "Non-Capability request failed to return any results. \n reason : " + throwable.getMessage());
      throwable.printStackTrace();
      errorCb.invoke("Non-Capability request failed to return any results. \n reason : " + throwable.getMessage());
    }
  }

  @Override
  public void onHostResume() {
    if (messageClient != null) {
      Log.d(TAG, ADD_MESSAGE_CLIENT);
      messageClient.addListener(this);
    }
  }

  @Override
  public void onHostPause() {
    Log.d(TAG, REMOVE_MESSAGE_CLIENT);
    messageClient.removeListener(this);
  }

  @Override
  public void onHostDestroy() {
    Log.d(TAG, REMOVE_MESSAGE_CLIENT);
    messageClient.removeListener(this);
  }
}
