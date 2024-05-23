import React, { useEffect, useState } from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import {
  sendMessage,
  sendGenuineMessage,
  watchEvents,
} from 'react-native-wear-connectivity';
import type {
  ReplyCallback,
  ErrorCallback,
} from 'react-native-wear-connectivity';

export default function App() {
  const [count, setCount] = useState(0);
  const [genuineCount, setGenuineCount] = useState(0);

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

  const onSuccess: ReplyCallback = (result) => console.log(result);
  const onError: ErrorCallback = (error) => console.log(error);
  const sendMessageToPhone = () => {
    const json = { text: 'hello' };
    sendMessage(json, onSuccess, onError);
  };
  const sendGenuineMessageToPhone = () => {
    sendGenuineMessage('/increment', onSuccess, onError);
  };

  return (
    <View style={styles.container}>
      <Text>count is: {count}</Text>
      <TouchableOpacity onPress={sendMessageToPhone} style={styles.button} />
      <Text>genuine count is: {genuineCount}</Text>
      <TouchableOpacity
        onPress={sendGenuineMessageToPhone}
        style={styles.button}
      />
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
    marginVertical: 10,
  },
  button: {
    marginTop: 2,
    height: 20,
    width: 20,
    backgroundColor: 'red',
    borderRadius: 50,
  },
});
