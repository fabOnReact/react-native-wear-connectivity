import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

type Payload = Record<string, unknown>;
type ReplyCallback = (reply: Payload) => void;
type ErrorCallback = (err: Error) => void;

export const defaultReplyCb = (reply: Payload) => {
  console.warn('Unhandled watch reply', reply);
};
export const defaultErrCb = (err: Error) => {
  console.warn('Unhandled sendMessage error', err);
};

export type SendMessageType = (
  message: Payload,
  cb: ReplyCallback,
  errCb: ErrorCallback
) => void;

export interface Spec extends TurboModule {
  multiply(a: number, b: number): Promise<number>;
  sendMessage: SendMessageType;
}

export default TurboModuleRegistry.getEnforcing<Spec>('WearConnectivity');
