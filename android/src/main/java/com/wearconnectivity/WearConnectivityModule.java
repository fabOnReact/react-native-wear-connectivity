package com.wearconnectivity;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import com.google.android.gms.common.GoogleApiAvailability;

public class WearConnectivityModule extends WearConnectivitySpec {

  private static ReactApplicationContext reactContext;
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "react-native-wear-connectivity ";
  private final WearConnectivityMessageClient messageClient;
  private final WearConnectivityDataClient dataClient;
  private boolean isListenerAdded = false;
  private String NO_NODES_FOUND = TAG + "sendMessage failed. No connected nodes found.";
  private String RETRIEVE_NODES_FAILED = TAG + "failed to retrieve nodes with error: ";
  private String INSTALL_GOOGLE_PLAY_WEARABLE = "The Android mobile phone needs to install the Google Play Wear app. ";
  private String MISSING_GOOGLE_PLAY_SERVICES = "GooglePlay Services not available.";

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    messageClient = new WearConnectivityMessageClient(context);
    dataClient = new WearConnectivityDataClient(context);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  /**
   * send a file to wearOs
   * @param filePath the path of the file to be sent
   * @param promise
   */
  @ReactMethod
  public void sendFile(String filePath, ReadableMap metadata, Promise promise) {
    if (dataClient != null) {
      dataClient.sendFile(filePath, promise);
    } else {
      promise.reject("E_SEND_FAILED", "Failed to send file");
    }
  }

  /**
   * Sends a message to the first nearby node among the provided connectedNodes.
   * If no nearby node is found, it invokes the error callback.
   */
  @ReactMethod
  public void sendMessage(ReadableMap messageData, Callback replyCb, Callback errorCb) {
    List<Node> connectedNodes = retrieveNodes(errorCb);
    if (connectedNodes != null && !connectedNodes.isEmpty()) {
      messageClient.sendMessage(messageData, connectedNodes, replyCb, errorCb);
    } else {
      errorCb.invoke(NO_NODES_FOUND);
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
