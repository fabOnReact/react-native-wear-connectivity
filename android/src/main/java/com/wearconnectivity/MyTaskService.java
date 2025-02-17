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

public class MyTaskService extends HeadlessJsTaskService {

  private static final int NOTIFICATION_ID = 1337;
  private static final String CHANNEL_ID = "wear_connectivity_channel";

  @Override
  public void onCreate() {
    super.onCreate();
    // For Android O and above, create a notification channel.
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
    // Build a minimal notification to satisfy foreground service requirements.
    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wear Connectivity")
            .setContentText("Processing background task")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build();

    // Promote the service to the foreground.
    startForeground(NOTIFICATION_ID, notification);

    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      // Extract necessary data from the extras (i.e., message path or other data)
      return new HeadlessJsTaskConfig(
              "SomeTaskName",             // Name of the task
              Arguments.fromBundle(extras), // Data passed to the task
              5000,                       // Timeout in milliseconds
              false                       // Task can run in the background
      );
    }
    return null;
  }
}