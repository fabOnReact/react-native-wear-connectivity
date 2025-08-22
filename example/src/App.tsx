import React from 'react';
import { Platform, PermissionsAndroid } from 'react-native';
import { CounterScreen } from './CounterScreen';

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

  return <CounterScreen />;
};

export default App;
