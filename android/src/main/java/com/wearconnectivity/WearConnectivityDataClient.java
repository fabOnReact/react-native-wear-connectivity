package com.wearconnectivity;

import android.util.Log;
import com.facebook.common.logging.FLog;
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

public class WearConnectivityDataClient {
    private static final String TAG = "WearConnectivityDataClient";
    private DataClient dataClient;
    private static ReactApplicationContext reactContext;

    public WearConnectivityDataClient(ReactApplicationContext context) {
        dataClient = Wearable.getDataClient(context);
        reactContext = context;
    }

    /**
     * Sends a file (as an Asset) using the DataClient API.
     * @param path to the file to be sent.
     */
    public void sendFile(String uri, Promise promise) {
        File file = new File(uri);
        Asset asset = createAssetFromFile(file);
        if (asset == null) {
            FLog.w(TAG, "Failed to create asset from file.");
            return;
        }
        // Create a DataMapRequest with a defined path.
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
}