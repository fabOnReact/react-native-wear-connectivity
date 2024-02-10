// https://github.com/mtford90/react-native-watch-connectivity/blob/89e1b53dcfe443791fabb4ca08a1c6149a238e13/lib/native-module.ts#L4

export enum WatchEvent {
  EVENT_ACTIVATION_ERROR = 'WatchActivationError',
  EVENT_APPLICATION_CONTEXT_RECEIVED = 'WatchApplicationContextReceived',
  EVENT_APPLICATION_CONTEXT_RECEIVED_ERROR = 'WatchApplicationContextReceivedError',
  EVENT_FILE_TRANSFER = 'WatchFileTransfer',
  EVENT_INSTALL_STATUS_CHANGED = 'WatchInstallStatusChanged',
  EVENT_PAIR_STATUS_CHANGED = 'WatchPairStatusChanged',
  EVENT_RECEIVE_MESSAGE = 'WatchReceiveMessage',
  EVENT_SESSION_BECAME_INACTIVE = 'WatchSessionBecameInactive',
  EVENT_SESSION_DID_DEACTIVATE = 'WatchSessionDidDeactivate',
  EVENT_WATCH_APPLICATION_CONTEXT_ERROR = 'WatchApplicationContextError',
  EVENT_WATCH_FILE_ERROR = 'WatchFileError',
  EVENT_WATCH_FILE_RECEIVED = 'WatchFileReceived',
  EVENT_WATCH_REACHABILITY_CHANGED = 'WatchReachabilityChanged',
  EVENT_WATCH_STATE_CHANGED = 'WatchStateChanged',
  EVENT_WATCH_USER_INFO_ERROR = 'WatchUserInfoError',
  EVENT_WATCH_USER_INFO_RECEIVED = 'WatchUserInfoReceived',
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

