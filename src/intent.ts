import { Platform } from 'react-native';
import type { OpenRemoteURI } from './NativeWearConnectivity';
import { WearConnectivity } from './index';
import { LIBRARY_NAME, IOS_NOT_SUPPORTED_WARNING } from './constants';

const UNHANDLED_CALLBACK =
  'The openRemoteURI function was called without a callback function. ';
const UNHANDLED_CALLBACK_REPLY =
  'The callback function was invoked with the uri: ';
const UNHANDLED_CALLBACK_ERROR =
  'The callback function was invoked with the error: ';

const defaultReplyCb = (reply: string) => {
  console.log(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_REPLY, reply);
};
const defaultErrCb = (err: string) => {
  console.warn(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_ERROR, err);
};

const openRemoteURI: OpenRemoteURI = (
  uri: String,
  nodeId: String,
  cb,
  errCb
) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.openRemoteURI(
    uri,
    nodeId,
    callbackWithDefault,
    errCbWithDefault
  );
};

const openRemoteURIMock: OpenRemoteURI = () =>
  console.warn(LIBRARY_NAME + 'intent' + IOS_NOT_SUPPORTED_WARNING);
let openRemoteURIExport: OpenRemoteURI = openRemoteURIMock;

if (Platform.OS !== 'ios') {
  openRemoteURIExport = openRemoteURI;
}

export { openRemoteURIExport as openRemoteURI };
