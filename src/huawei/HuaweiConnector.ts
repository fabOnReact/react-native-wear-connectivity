import { DeviceEventEmitter } from 'react-native';
import type { Payload } from '../NativeWearConnectivity';

let WearEngine: any;
try {
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  WearEngine = require('react-native-wear-engine');
} catch (e) {
  // Module might not be available in development environments.
  WearEngine = null;
}

/**
 * Maps Huawei Wear Engine callbacks to the library unified events.
 * Call this once when the app starts to bridge incoming messages.
 */
export function registerHuaweiListeners() {
  if (!WearEngine || !WearEngine.addListener) {
    return;
  }

  WearEngine.addListener('message', (payload: Payload) => {
    DeviceEventEmitter.emit('message', payload);
  });

  WearEngine.addListener('fileTransfer', (event: any) => {
    DeviceEventEmitter.emit('FileTransferEvent', event);
  });
}

/**
 * Sends a message to the connected Huawei wearable using Wear Engine.
 */
export function sendHuaweiMessage(message: Payload) {
  if (!WearEngine || !WearEngine.sendMessage) {
    return Promise.reject('react-native-wear-engine not available');
  }

  return WearEngine.sendMessage(message);
}
