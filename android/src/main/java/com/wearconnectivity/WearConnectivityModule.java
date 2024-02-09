package com.wearconnectivity;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST;

import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.util.RNLog;
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

public class WearConnectivityModule extends WearConnectivitySpec
    implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {
  public static final String NAME = "WearConnectivity";
  private static final String TAG = "WearConnectivityModule ";
  private final MessageClient client;

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    client = Wearable.getMessageClient(context);
    Log.d(TAG, TAG + "onMessageReceived listener added when activity is created. Client receives messages.");
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

  @ReactMethod
  public void sendMessage(String path, Promise promise) {
    try {
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
      List<Node> nodes = Tasks.await(nodeClient.getConnectedNodes());
      if (nodes.size() > 0) {
        for (Node node : nodes) {
          // TODO: Add check that node is listening (companion app activity is used)
          // https://developers.google.com/android/reference/com/google/android/gms/wearable/Node
          if (node.isNearby()) {
            sendMessageToClient(path, node, promise);
          }
        }
      } else {
        promise.reject(TAG, TAG + "sendMessage failed. No connected nodes found.");
      }
    } catch (Exception e) {
      promise.reject(TAG, TAG + "sendMessage failed with exception: " + e);
    }
  }

  private void sendMessageToClient(String path, Node node, Promise promise) {
    try {
      Task<Integer> sendTask =
          Wearable.getMessageClient(getReactApplicationContext())
              .sendMessage(node.getId(), path, null);
      OnSuccessListener<Object> onSuccessListener =
          new OnSuccessListener<Object>() {
            @Override
            public void onSuccess(Object object) {
              promise.resolve(TAG + "message sent to client with nodeID: " + object.toString());
            }
          };
      sendTask.addOnSuccessListener(onSuccessListener);
      OnFailureListener onFailureListener =
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              promise.reject(TAG, TAG + "sendMessage failed: " + e);
            }
          };
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      promise.reject(TAG, TAG + "sendMessage failed: " + e);
    }
  }

  public void onMessageReceived(MessageEvent messageEvent) {
    Log.d(TAG, TAG + "onMessageReceived received message with path: " + messageEvent.getPath());
    sendEvent(getReactApplicationContext(), messageEvent.getPath(), null);
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
      Log.d(TAG, TAG + "onMessageReceived listener added when activity is resumed. Client receives messages.");
      client.addListener(this);
    }
  }

  @Override
  public void onHostPause() {
    Log.d(TAG, TAG + "onMessageReceived listener removed when the activity paused. Client does not receive messages.");
    client.removeListener(this);
  }

  @Override
  public void onHostDestroy() {
    Log.d(TAG, TAG + "onMessageReceived listener removed when activity is destroyed. Client does not receive messages.");
    client.removeListener(this);
  }
}
