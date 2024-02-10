import { NativeModules, Platform } from 'react-native';
import type { SendMessage } from './NativeWearConnectivity';
import { defaultReplyCb, defaultErrCb } from './NativeWearConnectivity';

const LINKING_ERROR =
  `The package 'react-native-wear-connectivity' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const WearConnectivityModule = isTurboModuleEnabled
  ? require('./NativeWearConnectivity').default
  : NativeModules.WearConnectivity;

const WearConnectivity = WearConnectivityModule
  ? WearConnectivityModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return WearConnectivity.multiply(a, b);
}

const sendMessage: SendMessage = (message, cb, errCb) => {
  const callbackWithDefault = cb ?? defaultReplyCb;
  const errCbWithDefault = errCb ?? defaultErrCb;
  return WearConnectivity.sendMessage(
    message,
    callbackWithDefault,
    errCbWithDefault
  );
};

/*
export function sendMessage<
  MessageFromWatch extends WatchPayload = WatchPayload,
  MessageToWatch extends WatchPayload = WatchPayload
>(
  message: MessageToWatch,
  replyCb?: SendMessageReplyCallback<MessageFromWatch>,
  errCb?: SendMessageErrorCallback,
) {
  NativeModule.sendMessage<MessageToWatch, MessageFromWatch>(
    message,
    replyCb ||
      ((reply: MessageFromWatch) => {
        console.warn('Unhandled watch reply', reply);
      }),
    errCb ||
      ((err) => {
        console.warn('Unhandled sendMessage error', err);
      }),
  );
}

function _addListener<E extends WatchEvent, Payload = EventPayloads[E]>(
  event: E,
  cb: (payload: Payload) => void
) {
  // Type the event name
  if (!event) {
    throw new Error('Must pass event');
  }

  const sub = nativeWatchEventEmitter.addListener(event, cb);
  return () => sub.remove();
}

// https://github.com/mtford90/react-native-watch-connectivity/blob/89e1b53dcfe443791fabb4ca08a1c6149a238e13/lib/events/index.ts#L320

const watchEvents = {
  addListener,
  on: addListener,
  once,
};
  */

export { sendMessage };
