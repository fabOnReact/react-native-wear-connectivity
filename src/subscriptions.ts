import { NativeModules, NativeEventEmitter } from 'react-native';

type EventType = 'message';
type AddListener = (event: EventType, cb: Function) => UnsubscribeFn;

const _addListener: AddListener = (event, cb) => {
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

const nativeWatchEventEmitter = new NativeEventEmitter(
  NativeModules.AndroidWearCommunication
);

export const watchEvents = {
  addListener: _addListener,
  on: _addListener,
};
