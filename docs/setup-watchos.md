# watchOS setup

These steps show how to link a React Native app with an Apple Watch companion using [`react-native-watch-connectivity`](https://github.com/mtford90/react-native-watch-connectivity).

1. **Install the library**

   ```sh
   yarn add react-native-watch-connectivity
   ```

2. **Create a watch target**

   In Xcode add a watchOS app or extension to your project and enable Watch Connectivity in the capabilities tab.

3. **Pair the devices**

   Use the iOS Simulator with a paired watch or a physical iPhone and Apple Watch.

4. **Run the apps**

   Build the iOS app and watch extension. Use the snippet in `examples/ios-message.ts` to send a test message between the devices.
