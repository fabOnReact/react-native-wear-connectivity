import { Platform } from 'react-native';
import type { SendMessage, SendGenuineMessage, Payload } from './NativeWearConnectivity';
import { WearConnectivity } from './index';
import { LIBRARY_NAME, IOS_NOT_SUPPORTED_WARNING } from './constants';

const UNHANDLED_CALLBACK =
  'The sendMessage function was called without a callback function. ';
const UNHANDLED_CALLBACK_REPLY =
  'The callback function was invoked with the payload: ';
const UNHANDLED_CALLBACK_ERROR =
  'The callback function was invoked with the error: ';

const defaultReplyCb = (reply: string) => {
  console.log(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_REPLY, reply);
};
const defaultErrCb = (err: string) => {
  console.warn(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_ERROR, err);
};

const sendMessage: SendMessage = (message, cb, errCb) => {
  const json: Payload = { ...message, event: 'message' };
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.sendMessage(
    json,
    callbackWithDefault,
    errCbWithDefault
  );
};

const sendGenuineMessage: SendMessage = (message, cb, errCb) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.sendGenuineMessage(
    message,
    callbackWithDefault,
    errCbWithDefault
  );
};

const sendMessageMock: SendMessage = () =>
  console.warn(LIBRARY_NAME + 'message' + IOS_NOT_SUPPORTED_WARNING);

let sendMessageExport: SendMessage = sendMessageMock;
if (Platform.OS !== 'ios') {
  sendMessageExport = sendMessage;
}

let sendGenuineMessageExport: SendGenuineMessage = sendMessageMock;
if (Platform.OS !== 'ios') {
  sendGenuineMessageExport = sendGenuineMessage;
}

export {
  sendMessageExport as sendMessage,
  sendGenuineMessageExport as sendGenuineMessage,
};
