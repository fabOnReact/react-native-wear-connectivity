# WearOS setup

Follow these steps to connect a React Native app with a WearOS companion.

1. **Install the library**

   ```sh
   yarn add react-native-wear-connectivity
   ```

2. **Update `AndroidManifest.xml`**

   Add the required permissions and service to `android/app/src/main/AndroidManifest.xml`.

   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.WAKE_LOCK"/>
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

   <service
     android:name="com.wearconnectivity.WearConnectivityTask"
     android:exported="false"
     android:foregroundServiceType="dataSync|connectedDevice"
     android:permission="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />
   ```

3. **Pair the devices**

   Install the Google Play Wear app on the Android phone and pair it with the WearOS emulator or device.

4. **Run the apps**

   Start the Metro server, run the mobile app, and launch the WearOS companion. Use the snippet in `examples/android-message.ts` to verify the connection.
