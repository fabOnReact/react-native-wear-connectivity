import React, { useEffect } from 'react';
import { View, StyleSheet, Text, Button } from 'react-native';
import {
  sendMessage,
  sendGenuineMessage,
  watchEvents,
} from '../../../src/index';
import type { ReplyCallback, ErrorCallback } from '../../../src/index';

function CounterScreen() {
  useEffect(() => {
    const unsubscribe = watchEvents.on('message', () => {
      console.log('do nothing');
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const onSuccess: ReplyCallback = (result) => console.log(result);
  const onError: ErrorCallback = (error) => console.log(error);

  const sendMessageToWear = () => {
    const json = { text: 'hello', event: 'message' };
    sendMessage(json, onSuccess, onError);
  };
  const sendGenuineMessageToWear = () => {
    sendGenuineMessage('/increment', onSuccess, onError);
  };

  return (
    <View style={styles.container}>
      <Button title="Send Message" onPress={sendMessageToWear} />
      <Text style={styles.text}>We don't support iOS</Text>
      <Button title="Send Genuine Message" onPress={sendGenuineMessageToWear} />
      <Text style={styles.text}>We don't support iOS</Text>
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
  text: {
    height: 100,
    width: 400,
    textAlign: 'center',
    fontSize: 25,
    fontWeight: 'bold',
  },
});

export default CounterScreen;
