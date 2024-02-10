import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type { SendMessage } from './NativeWearConnectivity';
import { defaultReplyCb, defaultErrCb } from './NativeWearConnectivity';
import { WearConnectivity } from './index';

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
