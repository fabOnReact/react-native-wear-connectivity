export interface IWatchConnector {
  connect(): Promise<void>;
  disconnect(): Promise<void>;
  isConnected(): boolean;
}

export interface IDataSync {
  syncData(data: unknown): Promise<void>;
  fetchData(): Promise<unknown>;
}

export interface IWatchEventHandlers {
  onConnect?(): void;
  onDisconnect?(): void;
  onMessage?(data: unknown): void;
  onError?(error: Error): void;
}
