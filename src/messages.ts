import type { SendMessage } from './types';
import { defaultReplyCb, defaultErrCb } from './NativeWearConnectivity';
import { WearConnectivity } from './index';
import { LIBRARY_NAME, IOS_NOT_SUPPORTED_WARNING } from './constants';

const sendMessage: SendMessage = (message, cb, errCb) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.sendMessage(
    message,
    callbackWithDefault,
    errCbWithDefault
  );
};

const sendMessageMock: SendMessage = () =>
  console.warn(LIBRARY_NAME + 'message' + IOS_NOT_SUPPORTED_WARNING);

export { sendMessage, sendMessageMock };
