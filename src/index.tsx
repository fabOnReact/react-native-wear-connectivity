import { NativeModules, Platform } from 'react-native';
import { watchEvents } from './subscriptions';
import { sendMessage } from './messages';
import type { ReplyCallback, ErrorCallback } from './NativeWearConnectivity.ts';

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

let sendMessageExport;
let watchEventsExport;
// let WearConnectivity;

const LIBRARY_NAME = 'react-native-wear-connectivity ';
const IOS_NOT_SUPPORTED_WARNING =
  ' does not support iOS. Please use react-native-watch-connectivity library for iOS.';
const iosFunctionMock = (methodName: String) => () =>
  console.warn(LIBRARY_NAME + methodName + IOS_NOT_SUPPORTED_WARNING);

if (Platform.OS === 'ios') {
  sendMessageExport = iosFunctionMock('sendMessage');
  watchEventsExport = {
    addListener: iosFunctionMock('addListener'),
    on: iosFunctionMock('watchEvents'),
  };
} else {
  sendMessageExport = sendMessage;
  watchEventsExport = watchEvents;
}

export {
  sendMessageExport as sendMessage,
  watchEventsExport as watchEvents,
  WearConnectivity,
};
export type { ReplyCallback, ErrorCallback };
