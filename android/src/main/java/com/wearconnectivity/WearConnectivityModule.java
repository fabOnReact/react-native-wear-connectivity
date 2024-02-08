package com.wearconnectivity;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST;

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
  private static final String TAG = "WearConnectivityModule";
  private final MessageClient client;

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    client = Wearable.getMessageClient(context);
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

  // catch the ExecutionException
  @ReactMethod
  public void sendMessage(String path, Promise promise) {
    try {
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
      List<Node> nodes = Tasks.await(nodeClient.getConnectedNodes());
      if (nodes.size() > 0) {
        for (Node node : nodes) {
          sendMessageToClient(path, node);
        }
        promise.resolve(true);
      } else {
        Toast.makeText(getReactApplicationContext(), "No connected nodes found", Toast.LENGTH_LONG)
            .show();
      }
    } catch (Exception e) {
      FLog.w(TAG, " getConnectedNodes raised Exception: " + e);
    }
  }

  private void sendMessageToClient(String path, Node node) {
    try {
      Task<Integer> sendTask =
          Wearable.getMessageClient(getReactApplicationContext())
              .sendMessage(node.getId(), path, null);
      OnSuccessListener<Object> onSuccessListener =
          new OnSuccessListener<Object>() {
            @Override
            public void onSuccess(Object object) {
              FLog.d(TAG, " sendMessage called onSuccess for path: " + path);
            }
          };
      sendTask.addOnSuccessListener(onSuccessListener);
      OnFailureListener onFailureListener =
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              FLog.d(TAG, " sendMessage called onFailure with error: " + e);
            }
          };
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      FLog.w(TAG, " sendMessage raised Exception: " + e);
    }
  }

  public void onMessageReceived(MessageEvent messageEvent) {
    FLog.d(TAG, " onMessageReceived called for path: " + messageEvent.getPath());
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
      client.addListener(this);
    }
  }

  @Override
  public void onHostPause() {
    client.removeListener(this);
  }

  @Override
  public void onHostDestroy() {
    client.removeListener(this);
  }
}
