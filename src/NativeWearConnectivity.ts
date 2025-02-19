import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

// Messages
export type Payload = {};
export type ReplyCallback = (reply: Payload) => void;
export type ErrorCallback = (err: string) => void;

export type SendMessage = (
  message: Payload,
  cb: ReplyCallback,
  errCb: ErrorCallback
) => void;

export type SendFile = (file: string) => Promise<any>;

export interface Spec extends TurboModule {
  sendMessage: SendMessage;
  sendFile: SendFile;
}

export default TurboModuleRegistry.getEnforcing<Spec>('WearConnectivity');
