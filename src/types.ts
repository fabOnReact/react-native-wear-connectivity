// Subscriptions
export type EventType = 'message';
type UnsubscribeFn = Function;
type CallbackFunction = (event: any) => void;
export type AddListener = (
  event: EventType,
  cb: CallbackFunction,
  reply: unknown
) => UnsubscribeFn;

export type WatchEvents = {
  addListener: AddListener;
  on: AddListener;
};
