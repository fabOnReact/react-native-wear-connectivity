# Mi Band Setup

To connect a Mi Band device with this library:

1. Pair the band with your Android phone using the official **Mi Fitness** (or **Zepp Life**) application.
2. Ensure Bluetooth is enabled and the band is connected before launching your React Native app.
3. Grant the application the required permissions:
   - `android.permission.BLUETOOTH_CONNECT`
   - `android.permission.BLUETOOTH_SCAN`
   - `android.permission.ACCESS_FINE_LOCATION` (required for Bluetooth Low Energy scanning on older Android versions)
   - `android.permission.BODY_SENSORS` if accessing heart rate or other biometric data
   - `android.permission.POST_NOTIFICATIONS` to receive notifications on recent Android versions

Without pairing through the Mi Fitness application the device will not accept connections from third‑party apps.
