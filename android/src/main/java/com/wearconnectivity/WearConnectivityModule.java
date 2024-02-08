package com.wearconnectivity;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
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
import java.util.concurrent.ExecutionException;

public class WearConnectivityModule extends WearConnectivitySpec implements MessageClient.OnMessageReceivedListener, LifecycleEventListener {
  public static final String NAME = "WearConnectivity";

  WearConnectivityModule(ReactApplicationContext context) {
    super(context);
    context.addLifecycleEventListener(this);
    Wearable.getMessageClient(context).addListener(this);
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
  public void increaseWearCounter(Promise promise) {
    try {
      // add a check that it has permissions for that scope
      // https://android-developers.googleblog.com/2017/11/moving-past-googleapiclient_21.html
      NodeClient nodeClient = Wearable.getNodeClient(getReactApplicationContext());
      List<Node> nodes = Tasks.await(nodeClient.getConnectedNodes());
      if (nodes.size() > 0) {
        for (Node node : nodes) {
          sendMessageToClient(node);
        }
        promise.resolve(true);
      } else {
        Toast.makeText(getReactApplicationContext(), "No connected nodes found", Toast.LENGTH_LONG).show();
      }
    } catch(Exception e) {
      Log.w("TESTING", "EXCEPTION: " + e);
    }
  }

  private void sendMessageToClient(Node node) {
    try {
      Task<Integer> sendTask =
              Wearable.getMessageClient(getReactApplicationContext()).sendMessage(
                      node.getId(), "/increase_wear_counter", null);
      OnSuccessListener<Object> onSuccessListener = new OnSuccessListener<Object>() {
        @Override
        public void onSuccess(Object object) {
          Log.w("TESTING: ", "from Phone onSuccess");
        }
      };
      sendTask.addOnSuccessListener(onSuccessListener);
      OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Log.w("TESTING: ", "from Phone onFailure with e: " + e);
        }
      };
      sendTask.addOnFailureListener(onFailureListener);
    } catch (Exception e) {
      Log.w("TESTING: ", "from Phone e: " + e);
    }
  }

  public void onMessageReceived(MessageEvent messageEvent) {
    Log.w("TESTING: ", "from Phone onMessageReceived");
    /*
    if (messageEvent.getPath().equals("/increase_phone_counter")) {
      sendEvent(getReactApplicationContext(), "increaseCounter", null);
    }
     */
  }

  @Override
  public void onHostResume() {
    // implement it
  }


  @Override
  public void onHostPause() {
    // implement it
  }

  @Override
  public void onHostDestroy() {
    Wearable.getMessageClient(getReactApplicationContext()).removeListener(this);
  }
}
