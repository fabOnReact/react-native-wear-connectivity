package com.wearconnectivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;

abstract class WearConnectivitySpec extends ReactContextBaseJavaModule {
  WearConnectivitySpec(ReactApplicationContext context) {
    super(context);
  }

  public abstract void sendMessage(ReadableMap messageData, Callback replyCb, Callback errCb);

  public abstract void sendMessageAsync(ReadableMap messageData, Promise promise);
}
