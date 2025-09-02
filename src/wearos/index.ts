import type { IWatchConnector } from '../core';

export class WearOSConnector implements IWatchConnector {
  async connect(): Promise<void> {
    // TODO: implement Wear OS-specific connection logic
  }

  async disconnect(): Promise<void> {
    // TODO: implement Wear OS-specific disconnection logic
  }

  isConnected(): boolean {
    return false;
  }
}

export default WearOSConnector;
