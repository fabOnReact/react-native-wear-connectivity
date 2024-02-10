import React, { useEffect } from 'react';
import { View, StyleSheet, Text, Button } from 'react-native';
import { sendMessage, watchEvents } from 'react-native-wear-connectivity';

function WearCounter() {
  const [count, setCount] = React.useState(0);

  useEffect(() => {
    const unsubscribe = watchEvents.on('message', (message: Function) => {
      console.log('received message from watch', message);
      setCount((prevCount) => prevCount + 1);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const onPressHandler = async () => {
    const json = {
      text: 'hello',
      event: 'message',
    };
    const result = await sendMessage(json);
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

export default WearCounter;
