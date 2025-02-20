package com.wearconnectivity;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import com.google.android.gms.common.GoogleApiAvailability;

import androidx.annotation.RequiresApi;

public class WearConnectivityModule extends WearConnectivitySpec
    implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {

  private static ReactApplicationContext reactContext;
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "react-native-wear-connectivity ";
  private final WearConnectivityMessageClient messageClient;
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
    messageClient = new WearConnectivityMessageClient(context);
    dataClient = new WearConnectivityDataClient(context);
    Log.d(TAG, CLIENT_ADDED);
    messageClient.addListener();
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
    if (connectedNodes != null && !connectedNodes.isEmpty()) {
      messageClient.sendMessage(messageData, connectedNodes, replyCb, errorCb);
    } else {
      errorCb.invoke(NO_NODES_FOUND);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void onMessageReceived(@NonNull MessageEvent messageEvent) {
    if (messageClient != null) {
      messageClient.onMessageReceived(messageEvent);
    } else {
      FLog.w(TAG, "onMessageReceived: messageClient is null");
    }
  }

  @Override
  public void onHostResume() {
    if (messageClient != null && !isListenerAdded) {
      Log.d(TAG, "Adding listener on host resume");
      messageClient.addListener();
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
      messageClient.removeListener();
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
