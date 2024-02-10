import { NativeModules, NativeEventEmitter } from 'react-native';
import type { AddListener, Listen } from './NativeWearConnectivity';

const addListener: AddListener = (event, cb) => {
  return listen(event, cb, _addListener);
};

const listen: Listen = (event, cb, listener) => {
  switch (event) {
    case 'message':
      return _subscribeNativeMessageEvent(cb, listener);
    default:
      throw new Error(`Unknown watch event "${event}"`);
  }
};

enum WatchEvent {
  EVENT_RECEIVE_MESSAGE = 'WatchReceiveMessage',
}

/**
 * Hook up to native message event
 */
function _subscribeNativeMessageEvent(cb, addListener) {
  return addListener(WatchEvent.EVENT_RECEIVE_MESSAGE, (payload) => {
    const messageId = payload.id;

    const replyHandler = messageId
      ? (resp) => NativeModule.replyToMessageWithId(messageId, resp)
      : null;

    cb(payload || null, replyHandler);
  });
}

type Event = 'message';

export function _addListener(event: Event, cb) {
  if (!event) {
    throw new Error('Must pass event');
  }

  const sub = nativeWatchEventEmitter.addListener(event, cb);
  return () => sub.remove();
}

const nativeWatchEventEmitter = new NativeEventEmitter(
  NativeModules.AndroidWearCommunication
);

export const watchEvents = {
  addListener: _addListener,
  on: _addListener,
};
