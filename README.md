# react-native-wear-connectivity

Create a [wearOS][1] app using react-native and connect it to your react-native phone android app.
Both phone and wearOS apps are built with react-native.

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
import { sendMessage } from 'react-native-wear-connectivity';

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
  const onSuccess: ReplyCallback = (result) => {
    console.log(result);
  };
  const onError: ErrorCallback = (error) => console.log(error);

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

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
