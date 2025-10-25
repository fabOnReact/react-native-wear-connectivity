import { Platform } from 'react-native';
import type { SendMessage, Payload } from './NativeWearConnectivity';
import { WearConnectivity } from './index';
import { appleWatchConnector } from './applewatch/AppleWatchConnector';

const UNHANDLED_CALLBACK =
  'The sendMessage function was called without a callback function. ';
const UNHANDLED_CALLBACK_REPLY =
  'The callback function was invoked with the payload: ';
const UNHANDLED_CALLBACK_ERROR =
  'The callback function was invoked with the error: ';

const defaultReplyCb = (reply: Payload) => {
  console.log(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_REPLY, reply);
};
const defaultErrCb = (err: string) => {
  console.warn(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_ERROR, err);
};

const sendMessageAndroid: SendMessage = (message, cb, errCb) => {
  const json: Payload = { ...message, event: 'message' };
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.sendMessage(
    json,
    callbackWithDefault,
    errCbWithDefault
  );
};

const sendMessageIOS: SendMessage = (message, cb, errCb) => {
  const json: Payload = { ...message, event: 'message' };
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return appleWatchConnector.sendMessage(
    json,
    callbackWithDefault,
    errCbWithDefault
  );
};

let sendMessageExport: SendMessage = sendMessageAndroid;
if (Platform.OS === 'ios') {
  sendMessageExport = sendMessageIOS;
}

export { sendMessageExport as sendMessage };
