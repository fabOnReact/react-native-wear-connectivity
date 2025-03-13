package com.wearconnectivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.wearable.Node;
import java.util.List;

abstract class WearConnectivitySpec extends ReactContextBaseJavaModule {
  WearConnectivitySpec(ReactApplicationContext context) {
    super(context);
  }
  public abstract void sendMessage(ReadableMap messageData, Callback replyCb, Callback errCb);
  public abstract void sendGenuineMessage(String messagePath, Callback replyCb, Callback errCb);
  public abstract void getReachableNodes(Callback replyCb, Callback errCb);
  public abstract void getCapableAndReachableNodes(String capability, Callback replyCb, Callback errCb);
  public abstract void getNonCapableAndReachableNodes(String capability, Callback replyCb, Callback errCb);
  public abstract void openRemoteURI(String uri, String nodeId, Callback replyCb, Callback errCb);
}
