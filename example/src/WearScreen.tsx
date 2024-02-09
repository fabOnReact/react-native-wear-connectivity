import React, { useEffect } from 'react';
import {
  DeviceEventEmitter,
  NativeEventEmitter,
  View,
  StyleSheet,
  Text,
  NativeModules,
  Button,
} from 'react-native';
import { multiply, sendMessage } from 'react-native-wear-connectivity';

const INCREASE_PHONE_COUNTER_EVENT = 'increase_phone_counter';
const INCREASE_WEAR_COUNTER_EVENT = 'increase_wear_counter';

export default function WearCounter() {
  const [count, setCount] = React.useState(0);

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(
      NativeModules.AndroidWearCommunication
    );
    let eventListener = eventEmitter.addListener(
      INCREASE_PHONE_COUNTER_EVENT,
      (event) => {
        console.log('event' ,event)
        setCount((prevCount) => prevCount + 1);
      }
    );

    return () => {
      eventListener.remove();
    };
  }, []);

  const onPressHandler = async () => {
    const result = await sendMessage({
      text: 'hello',
      event: INCREASE_WEAR_COUNTER_EVENT,
    });
    console.log(result);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.counter}>{count}</Text>
      <Button title="increase counter" onPress={onPressHandler} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 8,
    backgroundColor: 'yellow',
  },
  counter: {
    height: 100,
    width: 400,
    textAlign: 'center',
    fontSize: 50,
    fontWeight: 'bold',
  },
});
