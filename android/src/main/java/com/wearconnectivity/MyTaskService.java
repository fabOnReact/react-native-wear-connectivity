package com.wearconnectivity;

import android.content.Intent;
import android.os.Bundle;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;

public class MyTaskService extends HeadlessJsTaskService {

  @Override
  protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      // Extract necessary data from the extras (i.e., message path or other data)
      return new HeadlessJsTaskConfig(
          "SomeTaskName", // Name of the task
          Arguments.fromBundle(extras), // Data passed to the task
          5000, // Timeout in milliseconds
          false // Task can run in the background
      );
    }
    return null;
  }
}
