# Apple Watch Setup

This library integrates with [`react-native-watch-connectivity`](https://github.com/mtford90/react-native-watch-connectivity)
to enable communication between an iOS app and its paired Apple Watch.

## Xcode configuration

1. Open the iOS project in Xcode.
2. Select **File > New > Target…** and add a **Watch App for iOS App**.
3. In **Signing & Capabilities** for both the iOS app and the WatchKit extension:
   - Add **App Groups** and use the same identifier (e.g. `group.com.example.watch`).
   - Enable **Background Modes** and tick **Uses Bluetooth LE accessories**.

## Entitlements

Both the iOS application and the WatchKit extension must include the chosen
`com.apple.security.application-groups` entry in their entitlements files so
that the two apps can communicate.

## Pairing with a watch

1. Pair an Apple Watch with the iPhone or simulator using the **Watch** app.
2. In Xcode choose a run destination that includes both the phone and watch.
3. Build and run; Xcode installs the watch app and establishes the pairing.
