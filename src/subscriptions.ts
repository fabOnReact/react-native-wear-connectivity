import type { AddListener, Listen } from './NativeWearConnectivity';

const watchEvents = {
  addListener,
  on: addListener,
};

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

/**
 * Hook up to native message event
 */
function _subscribeNativeMessageEvent(cb, addListener) {
  return addListener(WatchEvent.EVENT_RECEIVE_MESSAGE, (payload) => {
    const messageId = payload.id;

    const replyHandler = messageId
      ? (resp: MessageToWatch) =>
          NativeModule.replyToMessageWithId(messageId, resp)
      : null;

    cb(payload || null, replyHandler);
  });
}

export function _addListener(event, cb) {
  // Type the event name
  if (!event) {
    throw new Error('Must pass event');
  }

  const sub = nativeWatchEventEmitter.addListener(event, cb);
  return () => sub.remove();
}

const nativeWatchEventEmitter = new NativeEventEmitter(NativeModule);

export { watchEvents };
