import React, {useEffect} from 'react';
import {
  DeviceEventEmitter,
  NativeEventEmitter,
  View,
  StyleSheet,
  Text,
  NativeModules,
  Button,
} from 'react-native';

const INCREASE_COUNTER_EVENT = 'increaseCounter';

export default function WearCounter() {
  const [count, setCount] = React.useState(0);
  const increaseWearCounter = () => {
    NativeModules.AndroidWearCommunication.increaseWearCounter();
  };

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(
      NativeModules.AndroidWearCommunication,
    );
    let eventListener = eventEmitter.addListener(INCREASE_COUNTER_EVENT, () => {
      setCount(prevCount => prevCount + 1);
    });

    return () => {
      eventListener.remove();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.counter}>{count}</Text>
      <Button title="increase counter" onPress={increaseWearCounter} />
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
