import { AppRegistry } from 'react-native';
import { NativeModules, Platform } from 'react-native';
import { watchEvents } from './subscriptions';
import { sendMessage } from './messages';
import type { ReplyCallback, ErrorCallback } from './NativeWearConnectivity';
import { DeviceEventEmitter } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-wear-connectivity' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const WearConnectivityModule = isTurboModuleEnabled
  ? require('./NativeWearConnectivity').default
  : NativeModules.WearConnectivity;

const WearConnectivity = WearConnectivityModule
  ? WearConnectivityModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export { sendMessage, watchEvents, WearConnectivity };
export type { ReplyCallback, ErrorCallback };

// Define the headless task
const SomeTaskName = async (taskData) => {
  // Emit an event or process the message as needed
  DeviceEventEmitter.emit('message', taskData);
};

// Register the headless task with React Native
AppRegistry.registerHeadlessTask('SomeTaskName', () => SomeTaskName);
