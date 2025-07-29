import React from 'react';
import { Platform, PermissionsAndroid } from 'react-native';
import { default as CounterScreenAndroid } from './CounterScreen/index.android';
import { default as CounterScreenIos } from './CounterScreen/index.ios';

async function requestBluetoothPermissions() {
  if (Platform.OS === 'android' && Platform.Version >= 31) {
    const granted = await PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
    ]);

    console.log('Bluetooth permissions:', granted);
  }
}

const App = () => {
  React.useEffect(() => {
    requestBluetoothPermissions();
  }, []);

  const CounterScreen =
    Platform.OS === 'ios' ? CounterScreenIos : CounterScreenAndroid;
  return <CounterScreen />;
};

export default App;
