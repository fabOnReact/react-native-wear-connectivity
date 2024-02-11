import React, { useEffect } from 'react';
import { View, StyleSheet, Text, Button } from 'react-native';
import { sendMessage, watchEvents } from '../../../src/index';
import type { ReplyCallback, ErrorCallback } from '../../../src/index';

function CounterScreen() {
  const [disabled, setDisabled] = React.useState(false);
  const [count, setCount] = React.useState(0);

  useEffect(() => {
    const unsubscribe = watchEvents.on('message', () => {
      setCount((prevCount) => prevCount + 1);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const onSuccess: ReplyCallback = (result) => {
    setDisabled(false);
    console.log(result);
  };
  const onError: ErrorCallback = (error) => console.log(error);

  const sendMessageToWear = () => {
    setDisabled(true);
    const json = { text: 'hello', event: 'message' };
    sendMessage(json, onSuccess, onError);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.counter}>{count}</Text>
      <Button
        disabled={disabled}
        title="increase counter"
        onPress={sendMessageToWear}
      />
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

export default CounterScreen;
