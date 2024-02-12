# react-native-wear-connectivity

- Create a [Wear OS][1] app using react-native
- Connect two react-native apps (Wear OS and Android mobile app)
- **Both apps are written in react-native**



https://github.com/fabOnReact/react-native-wear-connectivity/assets/24992535/415fab47-7d76-4c72-80b9-c0d19ec25a49


**Note**: This library allows you to write your Android WearOS and Mobile apps in React Native, refer to [react-native-watch-connectivity][2] for Apple Watch development.

[1]: https://wearos.google.com
[2]: https://github.com/mtford90/react-native-watch-connectivity

# Table of Contents

- [Installation](#installation)
- [Example of implementation](#example-of-implementation)
- [How to create a WearOS app using react-native](#how-to-create-a-wearos-app-using-react-native)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)

## Installation

```sh
yarn add react-native-wear-connectivity
```

or

```sh
npm install react-native-wear-connectivity
```

## Example of implementation

Implementation of the above counter application.

```js
import React, { useEffect, useState } from 'react';
import { View, Text, Button } from 'react-native';
import { sendMessage, watchEvents } from 'react-native-wear-connectivity';

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
    <View>
      <Text>{count}</Text>
      <Button title="increase counter" onPress={sendMessageToWear} />
    </View>
  );
}
```

## How to create a WearOS app using react-native

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

[20]: https://reactnative.dev/docs/next/signed-apk-android
[21]: https://developer.android.com/training/wearables/get-started/connect-phone
[22]: https://gist.github.com/assets/24992535/f6cb9f84-dc50-492b-963d-6d9e9396f451 'wear os large round'
[23]: https://reactnative.dev/docs/debugging

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

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

Feature requests are discussed in the [issue tracker][40].

[40]: https://github.com/fabOnReact/react-native-wear-connectivity/issues

## License

MIT
