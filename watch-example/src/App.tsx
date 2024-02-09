import React, { useEffect, useState } from 'react';

import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  NativeEventEmitter,
  NativeModules,
} from 'react-native';
import { multiply, sendMessage } from 'react-native-wear-connectivity';

const INCREASE_WEAR_COUNTER_EVENT = 'increase_wear_counter';
const INCREASE_PHONE_COUNTER_EVENT = 'increase_phone_counter';

export default function App() {
  const [result, setResult] = useState<number | undefined>();
  const [count, setCount] = useState(0);

  useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(
      NativeModules.AndroidWearCommunication
    );
    let eventListener = eventEmitter.addListener(
      INCREASE_WEAR_COUNTER_EVENT,
      (event) => {
        console.log('event', event);
        setCount((prevCount) => prevCount + 1);
      }
    );

    return () => {
      eventListener.remove();
    };
  }, []);

  const sendMessageToPhone = async () => {
    const json = { text: 'hello', event: INCREASE_PHONE_COUNTER_EVENT };
    const result = await sendMessage(json);
    console.log(result);
  };

  return (
    <View style={styles.container}>
      <Text>count is: {count}</Text>
      <TouchableOpacity onPress={sendMessageToPhone} style={styles.button} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'yellow',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  button: {
    marginTop: 50,
    height: 50,
    width: 50,
    backgroundColor: 'red',
    borderRadius: 50,
  },
});
