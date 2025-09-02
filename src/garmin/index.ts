import type { IWatchConnector } from '../core';

export class GarminConnector implements IWatchConnector {
  async connect(): Promise<void> {
    // TODO: implement Garmin-specific connection logic
  }

  async disconnect(): Promise<void> {
    // TODO: implement Garmin-specific disconnection logic
  }

  isConnected(): boolean {
    return false;
  }
}

export default GarminConnector;
