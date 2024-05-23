import React, { useEffect } from 'react';
import { View, StyleSheet, Text, Button } from 'react-native';
import {
  sendMessage,
  sendGenuineMessage,
  watchEvents,
} from 'react-native-wear-connectivity';
import type {
  ReplyCallback,
  ErrorCallback,
} from 'react-native-wear-connectivity';

function CounterScreen() {
  const [disabled, setDisabled] = React.useState(false);
  const [count, setCount] = React.useState(0);
  const [genuineCount, setGenuineCount] = React.useState(0);

  useEffect(() => {
    const unsubscribe = watchEvents.on('message', (res) => {
      console.log(res);
      setCount((prevCount) => prevCount + 1);
    });
    const unsubscribeGenuine = watchEvents.on('genuineMessage', (res) => {
      console.log(res);
      setGenuineCount((prevCount) => prevCount + 1);
    });

    return () => {
      unsubscribe();
      unsubscribeGenuine();
    };
  }, []);

  const onSuccess: ReplyCallback = (result) => {
    setDisabled(false);
    console.log(result);
  };
  const onError: ErrorCallback = (error) => console.log(error);

  const sendMessageToWear = () => {
    setDisabled(true);
    const json = { text: 'hello' };
    sendMessage(json, onSuccess, onError);
  };
  const sendGenuineMessageToWear = () => {
    sendGenuineMessage('/increment', onSuccess, onError);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.counter}>{count}</Text>
      <Button
        disabled={disabled}
        title="increase counter"
        onPress={sendMessageToWear}
      />
      <Text style={styles.counter}>{genuineCount}</Text>
      <Button
        disabled={disabled}
        title="send genuine message"
        onPress={sendGenuineMessageToWear}
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
    paddingTop: 20,
  },
});

export default CounterScreen;
