// Messages
export type Payload = {};
export type ReplyCallback = (reply: Payload) => void;
export type ErrorCallback = (err: string) => void;

export type SendMessage = (
  message: Payload,
  cb: ReplyCallback,
  errCb: ErrorCallback
) => void;

// Subscriptions
export type EventType = 'message';
type UnsubscribeFn = Function;
type CallbackFunction = (event: any) => void;
export type AddListener = (
  event: EventType,
  cb: CallbackFunction
) => UnsubscribeFn;

export type WatchEvents = {
  addListener: AddListener;
  on: AddListener;
};
