# react-native-wear-connectivity

- Create a [Wear OS][1] app using react-native
- Connect two react-native apps (Wear OS and Android mobile app)
- **Both apps are written in react-native**

https://github.com/fabOnReact/react-native-wear-connectivity/assets/24992535/415fab47-7d76-4c72-80b9-c0d19ec25a49

**Note**: This library allows you to write your Android WearOS and Mobile apps in React Native, refer to [react-native-watch-connectivity][2] for Apple Watch development.

[1]: https://wearos.google.com
[2]: https://github.com/mtford90/react-native-watch-connectivity

# Table of Contents

- [Installation with renative](#installation-with-renative)
- [Installation with react-native](#installation-with-react-native)
- [Example of implementation](#example-of-implementation)
- [API Documentation](#api-documentation)
- [FAQ on Troubleshooting Errors](#faq-on-troubleshooting-errors)
- [Contributing](#contributing)

## Installation with renative

The app generated with this implementation is available [here](https://github.com/fabOnReact/react-native-wear-connectivity-renative-example).

Create a new renative app for android and wearos:

```sh
npx rnv new
```

Change folder to the newly created app and run yarn install:

```sh
cd YourFolder
yarn install
```

Run the app on the Android Emulator:

```sh
yarn rnv run -p android
```

Run the app on the WearOS Emulator:

```sh
yarn rnv run -p androidwear
```

Add the dependency `react-native-wear-connectivity` to your [renative.json](https://github.com/fabOnReact/react-native-wear-connectivity-renative-example/blob/main/renative.json):

```json
"plugins": {
  "react-native-wear-connectivity": {
    "version": "^0.1.9"
  }
}
```

- Pair the Android emulator with the Wear OS emulator ([instructions][21]).
- Implement the [example](#example-of-implementation) in [src/app/index.tsx](https://github.com/fabOnReact/react-native-wear-connectivity-renative-example/blob/main/src/app/index.tsx).

For more information refer to the official renative [documentation](https://next.renative.org) and [github repository](https://github.com/flexn-io/renative).

## Installation with React Native

```sh
yarn add react-native-wear-connectivity
```

or

```sh
npm install react-native-wear-connectivity
```

This is a detailed explanation on how to create a WearOS app using react-native:

- Create a new react-native app using the same name as your Mobile app.
  It is important to use the same name because both apps need to share the same package name (AndroidManifest, build.gradle, the project files) and applicationId (build.gradle).

```sh
npx react-native@latest init YourMobileAppName
```

- Add the following line to the new project AndroidManifest (file ):

```xml
<!-- this file is located at android/app/src/main/AndroidManifest.xml -->
<uses-feature android:name="android.hardware.type.watch" />
```

- Create a new emulator of type [WearOS Large round][22].
- Pair the Android emulator with the Wear OS emulator. Follow this [instructions][21].
- Start the metro server on port 8082 with `yarn start --port=8082`
- Build the project with `yarn android`, open the [react native dev menu][23] and change the bundle location to `your-ip:8082` (for ex. `192.168.18.2:8082`).
- Repeat the same steps for the Android Phone Emulator and use a different port (for ex. 8081).
- **Important Note**: Before publishing to Google Play, make sure that both apps are signed using the same key (instructions [here][20])

You can now build the app with `yarn android`. JS fast-refresh and the other metro functionalities work without problem.

You can find the instructions on how to build the example app for this project in the [CONTRIBUTING][43] section.

[43]: https://github.com/fabOnReact/react-native-wear-connectivity/blob/main/CONTRIBUTING.md
[20]: https://reactnative.dev/docs/next/signed-apk-android
[21]: https://developer.android.com/training/wearables/get-started/connect-phone
[22]: https://gist.github.com/assets/24992535/f6cb9f84-dc50-492b-963d-6d9e9396f451 'wear os large round'
[23]: https://reactnative.dev/docs/debugging

## Example of implementation

Example implementation of the above counter application for WearOS and Android Mobile.

```js
import React, { useEffect, useState } from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';
import { sendMessage, watchEvents } from 'react-native-wear-connectivity';

function App() {
  return <CounterScreen />;
}

function CounterScreen() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const unsubscribe = watchEvents.on('message', () => {
      setCount((prevCount) => prevCount + 1);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const onSuccess = (result) => console.log(result);
  const onError = (error) => console.log(error);

  const sendMessageToWear = () => {
    const json = { text: 'hello' };
    sendMessage(json, onSuccess, onError);
  };

  return (
    <View style={styles.container}>
      <Button title="increase counter" onPress={sendMessageToWear} />
      <Text style={styles.count}>The count is {count}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FDFDFD',
  },
  count: {
    borderRadius: 3,
    padding: 5,
    backgroundColor: '#9C9A9D',
    textAlign: 'center',
    textAlignVertical: 'center',
    marginTop: 20,
    color: 'white',
    fontSize: 20,
    fontWeight: '500',
  },
});

export default App;
```

## API Documentation

### Send Messages

```js
import { sendMessage } from 'react-native-wear-connectivity';

sendMessage({ text: 'Hello watch!' });
```

### Receive Messages

```js
import { watchEvents } from 'react-native-wear-connectivity';

const unsubscribe = watchEvents.on('message', (message) => {
  console.log('received message from watch', message);
});
```

## FAQ on Troubleshooting Errors

While some error messages are displayed on the metro server for the mobile or wearOS device (port 8082), other warnings are only available through logcat.
To display them you need to open the android logcat tool from within Android Studio, where you can select the emulator and filter the messages by package name (more info in this [screenshot][41]).

[41]: https://github.com/user-attachments/assets/87016f71-782d-4f28-88dc-2c5d013eae2f

#### Wearable App not installed on Mobile Device

The error displays on the Metro Server if the mobile device did not install the Wear App, which is used to pair mobile device with wearOS device.
The Wear app is installed from Google Play and allows to pair the Wear Device with the Android Phone. Follow this [instructions][21] to pair WearOS emulator with Android Phone.

```
The Android mobile phone needs to install the Google Play Wear app.
```

#### wearOS device too far for bluetooth connection

Logcat (wearOS) shows the following log message when sending messages via bluetooth to a mobile device too far from the watch. The message is not displayed on the Metro Server.

```
Pixel_8_Pro_API_35Device is too far for bluetooth connection.
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

Feature requests are discussed in the [issue tracker][40].

[40]: https://github.com/fabOnReact/react-native-wear-connectivity/issues

## License

MIT
