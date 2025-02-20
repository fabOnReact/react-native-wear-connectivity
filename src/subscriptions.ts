import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import type { AddListener, WatchEvents } from './types';
import { LIBRARY_NAME, IOS_NOT_SUPPORTED_WARNING } from './constants';

const _addListener: AddListener = (event, cb, reply) => {
  const nativeWatchEventEmitter = new NativeEventEmitter(
    NativeModules.AndroidWearCommunication
  );
  if (!event) {
    throw new Error('Must pass event');
  }

  switch (event) {
    case 'message':
      break;
    default:
      throw new Error(`Unknown watch event "${event}"`);
  }

  const sub = nativeWatchEventEmitter.addListener(event, cb);
  return () => sub.remove();
};

const _addListenerMock: AddListener = () => {
  console.warn(LIBRARY_NAME + 'watchEvents' + IOS_NOT_SUPPORTED_WARNING);
  return () => {};
};

let watchEvents: WatchEvents = {
  addListener: _addListenerMock,
  on: _addListenerMock,
};

if (Platform.OS !== 'ios') {
  watchEvents = {
    addListener: _addListener,
    on: _addListener,
  };
}

export { watchEvents };
