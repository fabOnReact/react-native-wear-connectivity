import React, { useEffect, useState } from 'react';

import { StyleSheet, View, Text, TouchableOpacity, Button } from 'react-native';
import { sendMessage, watchEvents } from 'react-native-wear-connectivity';
import type {
  ReplyCallback,
  ErrorCallback,
} from 'react-native-wear-connectivity';

export default function App() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const unsubscribe = watchEvents.on('message', () => {
      setCount((prevCount) => prevCount + 1);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const onSuccess: ReplyCallback = (result) => console.log(result);
  const onError: ErrorCallback = (error) => console.log(error);
  const sendMessageToPhone = () => {
    const json = { text: 'hello' };
    sendMessage(json, onSuccess, onError);
  };

  return (
    <View style={styles.container}>
      <View style={styles.textBackground}>
        <Text style={styles.count}>The count is {count}</Text>
      </View>
      <View style={styles.buttonContainer}>
        <Button title="press" color="#B4B1B3" onPress={sendMessageToPhone} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FDFDFD',
  },
  buttonContainer: {
    marginTop: 30,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  count: {
    borderRadius: 3,
    padding: 5,
    backgroundColor: '#9C9A9D',
    textAlign: 'center',
    textAlignVertical: 'center',
    marginTop: 20,
    color: 'white',
    fontSize: 20,
    fontWeight: '500',
  },
  plusIcon: {
    flex: 1,
    color: 'white',
    textAlign: 'center',
    textAlignVertical: 'center',
    fontSize: 30,
    fontWeight: '400',
  },
});
