import { Platform } from 'react-native';
import type { GetNodes } from './NativeWearConnectivity';
import { WearConnectivity } from './index';
import { LIBRARY_NAME, IOS_NOT_SUPPORTED_WARNING } from './constants';

const UNHANDLED_CALLBACK =
  'The capability function was called without a callback function. ';
const UNHANDLED_CALLBACK_REPLY =
  'The callback function was invoked with the capability: ';
const UNHANDLED_CALLBACK_ERROR =
  'The callback function was invoked with the error: ';

const defaultReplyCb = (reply: string) => {
  console.log(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_REPLY, reply);
};
const defaultErrCb = (err: string) => {
  console.warn(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_ERROR, err);
};

const getCapableAndReachableNodes: GetNodes = (capability, cb, errCb) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.getCapableAndReachableNodes(
    capability,
    callbackWithDefault,
    errCbWithDefault
  );
};

const getNonCapableAndReachableNodes: GetNodes = (capability, cb, errCb) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.getNonCapableAndReachableNodes(
    capability,
    callbackWithDefault,
    errCbWithDefault
  );
};

const capabilityMock: GetNodes = () =>
  console.warn(LIBRARY_NAME + 'capability' + IOS_NOT_SUPPORTED_WARNING);

let getCapableAndReachableNodesExport: GetNodes = capabilityMock;
let getNonCapableAndReachableNodesExport: GetNodes = capabilityMock;

if (Platform.OS !== 'ios') {
  getCapableAndReachableNodesExport = getCapableAndReachableNodes;
  getNonCapableAndReachableNodesExport = getNonCapableAndReachableNodes;
}

export {
  getCapableAndReachableNodesExport as getCapableAndReachableNodes,
  getNonCapableAndReachableNodesExport as getNonCapableAndReachableNodes,
};
