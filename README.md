# react-native-wear-connectivity

- Create a [wearOS][1] app using react-native
- Connect two react-native apps (wearOS and android phone)
- **Both apps are writter in react-native**

[1]: https://wearos.google.com

## Installation

```sh
yarn add react-native-wear-connectivity
```

or

```sh
npm install react-native-wear-connectivity
```

## Usage

```js
import { sendMessage, watchEvents } from 'react-native-wear-connectivity';

function CounterScreen() {
  const [count, setCount] = React.useState(0);

  // listen for messages from wearOS/phone
  useEffect(() => {
    const unsubscribe = watchEvents.on('message', () => {
      setCount((prevCount) => prevCount + 1);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  // send a message from/to wearOS
  const onSuccess = (result) => console.log(result);
  const onError = (error) => console.log(error);

  const sendMessageToWear = () => {
    const json = { text: 'hello', event: 'message' };
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

## How to create WearOS app using react-native

Make a copy of your react-native project. For Example:

```bash
cp my-react-native-project my-react-native-wear-project
```

Add the following line to your new project AndroidManifest `my-react-native-wear-project/android/app/src/main/AndoridManifest.xml`

```xml
<uses-feature android:name="android.hardware.type.watch" />
```

- Pair the android emulator with the wearos emulator (instructions [here][21]). I suggest using the emulator `WearOS Large round`, as the other emulator have issues with react-native dev menu.
- Start metro server on port 8082 with `yarn start --port=8082`
- Open the `react native dev menu` and change the bundle location to `your-ip:8081` (for ex. `192.168.18.2:8082`).
- Repeat same steps for Android Phone Emulator and use a different port.
- **Important Note**: Before publishing to GooglePlay, make sure that both apps are signed using the same key (instructions [here][20])

You can now build the app from the root directory with `yarn android`. JS fastrefresh and the other metro functionalities work without problem (no need to build for js changes).

[20]: https://reactnative.dev/docs/next/signed-apk-android
[21]: https://developer.android.com/training/wearables/get-started/connect-phone

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
