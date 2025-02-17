package com.wearconnectivity;

import android.app.ActivityManager;
import android.content.Context;
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
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.facebook.react.HeadlessJsTaskService;
import android.content.Intent;  // For Intent class
import android.os.Bundle;       // For Bundle class
import com.facebook.react.bridge.ReactApplicationContext;  // For ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule;  // If you haven't already imported this

public class WearConnectivityModule extends WearConnectivitySpec
    implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {

  private static ReactApplicationContext reactContext;
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "react-native-wear-connectivity ";
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

  private List<Node> retrieveNodes(Callback errorCb) {
    try {
      int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getReactApplicationContext());
      ConnectionResult connectionResult = new ConnectionResult(result);
      if (!connectionResult.isSuccess()) {
        errorCb.invoke( MISSING_GOOGLE_PLAY_SERVICES + connectionResult.getErrorMessage());
        return null;
      }
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
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

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Context context = getReactApplicationContext();
    if (!isAppOnForeground(context)) {
      Intent service = new Intent(getReactApplicationContext(), com.wearconnectivity.MyTaskService.class);
      Bundle bundle = new Bundle();

      bundle.putString("foo", "bar");
      service.putExtras(bundle);

      Log.w("TESTING ", "startForegroundService");
      getReactApplicationContext().startForegroundService(service);
      HeadlessJsTaskService.acquireWakeLockNow(getReactApplicationContext());
    } else {  // Optionally handle messages differently when the app is in the foreground
      Log.w("TESTING", "App is in foreground; handling message without starting Headless JS service");
      // For example, you might send the event directly to JS:
      // sendEvent(getReactApplicationContext(), "yourEventName", yourWritableMap);
    }
  }

  /**
   * Checks if the app is in the foreground.
   */
  private boolean isAppOnForeground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null) {
      return false;
    }
    final String packageName = context.getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      Log.w("TESTING ", "packageName: " + packageName);
      Log.w("TESTING ", "appProcess.processName: " + appProcess.processName);
      if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
              appProcess.processName.equals(packageName)) {
        return true;
      }
    }
    return false;
  }
  /*
  public void onMessageReceived(MessageEvent messageEvent) {
    try {
      JSONObject jsonObject = new JSONObject(messageEvent.getPath());
      WritableMap messageAsWritableMap = (WritableMap) JSONArguments.fromJSONObject(jsonObject);
      String event = jsonObject.getString("event");
      FLog.w(TAG, TAG + " event: " + event + " message: " + messageAsWritableMap);
      // sendEvent(getReactApplicationContext(), event, messageAsWritableMap);

      Data inputData = new Data.Builder()
        .putString("messagePath", messageEvent.getPath())
        .build();

      HeadlessJsTaskService.acquireWakeLockNow(reactContext);

      OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MessageWorker.class)
            .setInputData(inputData)
            .build();

      WorkManager.getInstance(getReactApplicationContext()).enqueue(workRequest);
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
  */

  private void sendEvent(
      ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  public static ReactApplicationContext getReactContext() {
    return reactContext;
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
    // Log.d(TAG, REMOVE_CLIENT);
    // client.removeListener(this);
  }

  @Override
  public void onHostDestroy() {
    // Log.d(TAG, REMOVE_CLIENT);
    // client.removeListener(this);
  }
}
