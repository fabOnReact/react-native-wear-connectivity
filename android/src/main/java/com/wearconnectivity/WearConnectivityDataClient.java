package com.wearconnectivity;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.facebook.react.bridge.ReactApplicationContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

public class WearConnectivityDataClient implements DataClient.OnDataChangedListener, LifecycleEventListener {
    private static final String TAG = "WearConnectivityDataClient";
    private DataClient dataClient;
    private static ReactApplicationContext reactContext;

    public WearConnectivityDataClient(ReactApplicationContext context) {
        dataClient = Wearable.getDataClient(context);
        reactContext = context;
        dataClient.addListener(this);
        context.addLifecycleEventListener(this);
    }

    /**
     * Sends a file (as an Asset) using the DataClient API.
     * @param uri path to the file to be sent.
     */
    public void sendFile(String uri, Promise promise) {
        File file = new File(uri);
        Asset asset = createAssetFromFile(file);
        if (asset == null) {
            FLog.w(TAG, "Failed to create asset from file.");
            return;
        }
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/file_transfer");
        dataMapRequest.getDataMap().putAsset("file", asset);
        dataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());
        PutDataRequest request = dataMapRequest.asPutDataRequest();
        Task<DataItem> task = dataClient.putDataItem(request);
        task.addOnSuccessListener(dataItem -> {
            promise.resolve("File sent successfully via DataClient.");
        }).addOnFailureListener(e -> {
            promise.reject("File sending failed: " + e);
        });
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals("/voice_transfer")) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Asset asset = dataMap.getAsset("file");
                    if (asset != null) {
                        receiveFile(asset);
                    }
                }
            }
        }
    }

    /**
     * Helper method to create an Asset from a file.
     * @param file the file to convert.
     * @return the resulting Asset, or null if an error occurred.
     */
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

    private void receiveFile(Asset asset) {
        Task<DataClient.GetFdForAssetResponse> task = dataClient.getFdForAsset(asset);
        task.addOnSuccessListener(new OnSuccessListener<DataClient.GetFdForAssetResponse>() {
            @Override
            public void onSuccess(DataClient.GetFdForAssetResponse response) {
                InputStream is = response.getInputStream();
                if (is == null) {
                    FLog.w(TAG,"WatchFileReceiveError " +  "InputStream is null");
                    return;
                }
                try {
                    // Save the received file to internal storage. Here, we use "received_file.jpg"
                    File file = new File(getReactContext().getFilesDir(), "received_file.jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    // Dispatch an event with the file path
                    FLog.w(TAG,"WatchFileReceived file.getAbsolutePath(): " + file.getAbsolutePath());
                    dispatchEvent("WatchFileReceived", file.getAbsolutePath());
                } catch (IOException e) {
                    FLog.w(TAG,"WatchFileReceiveError: " + e.getMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FLog.w(TAG, "WatchFileReceiveError: " + e.toString());
            }
        });
    }

    private void dispatchEvent(String eventName, String body) {
        getReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, body);
    }

    @Override
    public void onHostResume() {
        // do nothing
    }

    @Override
    public void onHostPause() {
        // do nothing
    }

    @Override
    public void onHostDestroy() {
        dataClient.removeListener(this);
    }
}