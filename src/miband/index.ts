import type { IWatchConnector } from '../core';

export class MiBandConnector implements IWatchConnector {
  async connect(): Promise<void> {
    // TODO: implement Mi Band-specific connection logic
  }

  async disconnect(): Promise<void> {
    // TODO: implement Mi Band-specific disconnection logic
  }

  isConnected(): boolean {
    return false;
  }
}

export default MiBandConnector;
