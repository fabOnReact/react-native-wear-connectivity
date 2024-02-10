import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

type Payload = Record<string, unknown>;
type ReplyCallback = (reply: Payload) => void;
type ErrorCallback = (err: Error) => void;

const UNHANDLED_CALLBACK =
  'The sendMessage function was called without a callback function. ';
const UNHANDLED_CALLBACK_REPLY =
  'The callback function was invoked with the payload: ';
const UNHANDLED_CALLBACK_ERROR =
  'The callback function was invoked with the error: ';

export const defaultReplyCb = (reply: Payload) => {
  console.log(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_REPLY, reply);
};
export const defaultErrCb = (err: Error) => {
  console.warn(UNHANDLED_CALLBACK + UNHANDLED_CALLBACK_ERROR, err);
};

export type SendMessage = (
  message: Payload,
  cb: ReplyCallback,
  errCb: ErrorCallback
) => void;

export interface Spec extends TurboModule {
  multiply(a: number, b: number): Promise<number>;
  sendMessage: SendMessage;
}

export default TurboModuleRegistry.getEnforcing<Spec>('WearConnectivity');
