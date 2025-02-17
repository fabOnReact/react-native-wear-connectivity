package com.wearconnectivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;

public class WearConnectivityTask extends HeadlessJsTaskService {

  private static final int NOTIFICATION_ID = 1337;
  private static final String CHANNEL_ID = "wear_connectivity_channel";

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(
              CHANNEL_ID,
              "Wear Connectivity",
              NotificationManager.IMPORTANCE_LOW
      );
      NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      if (manager != null) {
        manager.createNotificationChannel(channel);
      }
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wear Connectivity")
            .setContentText("Processing background task")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build();
    startForeground(NOTIFICATION_ID, notification);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      return new HeadlessJsTaskConfig(
              "WearConnectivityTask",
              Arguments.fromBundle(extras),
              5000,
              false
      );
    }
    return null;
  }
}