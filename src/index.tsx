import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type { SendMessage } from './NativeWearConnectivity';
import { defaultReplyCb, defaultErrCb } from './NativeWearConnectivity';
import { watchEvents } from './subscriptions';

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

const sendMessage: SendMessage = (message, cb, errCb) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.sendMessage(
    message,
    callbackWithDefault,
    errCbWithDefault
  );
};

export { sendMessage };

export function multiply(a: number, b: number): Promise<number> {
  return WearConnectivity.multiply(a, b);
}
