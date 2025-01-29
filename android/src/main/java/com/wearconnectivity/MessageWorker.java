package com.wearconnectivity;

import com.facebook.react.bridge.ReactApplicationContext;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.react.bridge.JSONArguments;

public class MessageWorker extends Worker {
    private static final String TAG = "WearConnectivityWorker";

    public MessageWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String messagePath = getInputData().getString("messagePath");

        if (messagePath == null) {
            Log.e(TAG, "No message received");
            return Result.failure();
        }

        try {
            JSONObject jsonObject = new JSONObject(messagePath);
            WritableMap messageAsWritableMap = (WritableMap) JSONArguments.fromJSONObject(jsonObject);
            String event = jsonObject.getString("event");
            FLog.w(TAG, "Processing event in background: " + event + " message: " + messageAsWritableMap);

            // Send event to JS
            sendEvent(event, messageAsWritableMap);

            return Result.success();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse message: " + e);
            return Result.failure();
        }
    }

    private void sendEvent(String eventName, WritableMap params) {
        ReactApplicationContext reactContext = WearConnectivityModule.getReactContext();
        if (reactContext != null) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
    }
}
