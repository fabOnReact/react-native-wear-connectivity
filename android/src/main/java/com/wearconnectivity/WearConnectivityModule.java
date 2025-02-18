package com.wearconnectivity;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSONArguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
  private final MessageClient client;
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
    client = Wearable.getMessageClient(context);
    Log.d(TAG, CLIENT_ADDED);
    client.addListener(this);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void sendMessage(ReadableMap messageData, Callback replyCb, Callback errorCb) {
    List<Node> connectedNodes = retrieveNodes(errorCb);
    if (connectedNodes != null && connectedNodes.size() > 0 && client != null) {
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

  public void sendFileToWear(File file) {
    Asset asset = createAssetFromFile(file);
    PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/file_transfer");
    dataMapRequest.getDataMap().putAsset("file", asset);
    dataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());

    PutDataRequest request = dataMapRequest.asPutDataRequest();
    Task<DataItem> task = Wearable.getDataClient(getReactContext()).putDataItem(request);
    task.addOnSuccessListener(dataItem -> {
      FLog.w(TAG, "File sent successfully");
    }).addOnFailureListener(e -> {
      FLog.w(TAG, "File sent failed: " + e);
    });
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
    if (client != null && !isListenerAdded) {
      Log.d(TAG, "Adding listener on host resume");
      client.addListener(this);
      isListenerAdded = true;
    }
  }

  @Override
  public void onHostPause() {
    Log.d(TAG, "onHostPause: leaving listener active for background events");
  }

  @Override
  public void onHostDestroy() {
    if (client != null && isListenerAdded) {
      Log.d(TAG, "Removing listener on host destroy");
      client.removeListener(this);
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

  private Asset createAssetFromFile(File file) {
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      byte[] byteArray = new byte[(int) file.length()];
      fileInputStream.read(byteArray);
      fileInputStream.close();
      return Asset.createFromBytes(byteArray);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static ReactApplicationContext getReactContext() {
    return reactContext;
  }
}
