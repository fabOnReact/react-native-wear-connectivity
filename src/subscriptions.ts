import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import type { AddListener, WatchEvents } from './types';
import { appleWatchEvents } from './applewatch/AppleWatchConnector';

const androidAddListener: AddListener = (event, cb) => {
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

const iosAddListener: AddListener = (event, cb) => {
  if (!event) {
    throw new Error('Must pass event');
  }

  switch (event) {
    case 'message':
      break;
    default:
      throw new Error(`Unknown watch event "${event}"`);
  }

  const sub = appleWatchEvents.addListener(event, cb);
  return () => sub.remove();
};

let watchEvents: WatchEvents;

if (Platform.OS === 'ios') {
  watchEvents = {
    addListener: iosAddListener,
    on: iosAddListener,
  };
} else {
  watchEvents = {
    addListener: androidAddListener,
    on: androidAddListener,
  };
}

export { watchEvents };
