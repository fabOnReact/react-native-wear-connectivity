# Huawei Setup

To use Huawei wearables with this library you must configure Huawei Mobile Services (HMS) and pair the watch with your phone.

## HMS Core configuration

1. Create an app in the [Huawei Developer Console](https://developer.huawei.com/).  
2. Download the `agconnect-services.json` file and place it in your Android project under `android/agconnect-services.json`.
3. Ensure the following are added to your `android/build.gradle`:
   - `maven { url 'https://developer.huawei.com/repo/' }`
   - `classpath 'com.huawei.agconnect:agcp:1.9.1.301'`
   - `implementation 'com.huawei.hms:wearengine:5.0.0.300'`
4. Apply the `com.huawei.agconnect` plugin and sync your project.

## Pairing the watch

1. Install the Huawei Health app on your mobile device.
2. Sign in with your Huawei ID and add your wearable device.
3. Ensure the watch is connected via Bluetooth before testing message or file transfer APIs.

These steps prepare the environment so `react-native-wear-engine` can communicate with the paired Huawei watch through this library's unified API.
