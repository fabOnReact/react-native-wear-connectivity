import type { IWatchConnector } from '../core';

export class AppleWatchConnector implements IWatchConnector {
  async connect(): Promise<void> {
    // TODO: implement Apple Watch-specific connection logic
  }

  async disconnect(): Promise<void> {
    // TODO: implement Apple Watch-specific disconnection logic
  }

  isConnected(): boolean {
    return false;
  }
}

export default AppleWatchConnector;
