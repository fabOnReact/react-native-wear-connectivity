import React, { useEffect } from 'react';
import {
  View,
  StyleSheet,
  Text,
  Button,
  DeviceEventEmitter,
} from 'react-native';
import { launchImageLibrary } from 'react-native-image-picker';
import {
  monitorFileTransfers,
  sendFile,
  sendMessage,
  watchEvents,
} from 'react-native-wear-connectivity';
import type {
  ReplyCallback,
  ErrorCallback,
} from 'react-native-wear-connectivity';

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

  useEffect(() => {
    const cancel = monitorFileTransfers((transferInfo) => {
      console.log('File transfer event:', transferInfo);
    });

    return () => cancel(); // Cleanup when component unmounts
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

  const sendFileToWear = async () => {
    try {
      // @ts-ignore
      const result = await launchImageLibrary();
      if (!result.assets || result.assets.length === 0) {
        console.log('No asset selected');
        return;
      }
      const asset = result.assets[0] || { uri: undefined };
      if (asset.uri) {
        const filePath = asset.uri.replace('file://', '');
        await sendFile(filePath);
      }
    } catch (error) {
      console.error('Error in sendFileToWear:', error);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.counter}>{count}</Text>
      <Button
        disabled={disabled}
        title="increase counter"
        onPress={sendMessageToWear}
      />
      <Button title="send file" onPress={sendFileToWear} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 8,
    backgroundColor: 'red',
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
