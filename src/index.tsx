import { NativeModules, Platform } from 'react-native';
import { watchEvents, watchEventsMock } from './subscriptions';
import { sendMessage, sendMessageMock } from './messages';
import type {
  ReplyCallback,
  ErrorCallback,
  SendMessage,
  WatchEvents,
} from './types';

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

let sendMessageExport: SendMessage;
let watchEventsExport: WatchEvents;

if (Platform.OS === 'ios') {
  sendMessageExport = sendMessageMock;
  watchEventsExport = watchEventsMock;
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
