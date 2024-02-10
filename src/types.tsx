// https://github.com/mtford90/react-native-watch-connectivity/blob/89e1b53dcfe443791fabb4ca08a1c6149a238e13/lib/native-module.ts#L4

/*
export enum WatchEvent {
  EVENT_RECEIVE_MESSAGE = 'WatchReceiveMessage',
}

export interface EventPayloads {
  [WatchEvent.EVENT_FILE_TRANSFER]: NativeFileTransferEvent;
  [WatchEvent.EVENT_RECEIVE_MESSAGE]: WatchPayload & {id?: string};
  [WatchEvent.EVENT_WATCH_STATE_CHANGED]: {
    state:
      | 'WCSessionActivationStateNotActivated'
      | 'WCSessionActivationStateInactive'
      | 'WCSessionActivationStateActivated';
  };
  [WatchEvent.EVENT_WATCH_REACHABILITY_CHANGED]: {
    reachability: boolean;
  };
  [WatchEvent.EVENT_WATCH_FILE_RECEIVED]: QueuedFile;
  [WatchEvent.EVENT_WATCH_USER_INFO_RECEIVED]: QueuedUserInfo<WatchPayload>;
  [WatchEvent.EVENT_APPLICATION_CONTEXT_RECEIVED]: WatchPayload | null;
  [WatchEvent.EVENT_PAIR_STATUS_CHANGED]: {
    paired: boolean;
  };
  [WatchEvent.EVENT_INSTALL_STATUS_CHANGED]: {
    installed: boolean;
  };
  [WatchEvent.EVENT_WATCH_APPLICATION_CONTEXT_ERROR]: Error;
  [WatchEvent.EVENT_WATCH_USER_INFO_ERROR]: Error;
  [WatchEvent.EVENT_WATCH_FILE_ERROR]: Error;
  [WatchEvent.EVENT_ACTIVATION_ERROR]: Error;
  [WatchEvent.EVENT_SESSION_BECAME_INACTIVE]: Error;
  [WatchEvent.EVENT_SESSION_DID_DEACTIVATE]: Error;
  [WatchEvent.EVENT_APPLICATION_CONTEXT_RECEIVED_ERROR]: Error;
}
*/
