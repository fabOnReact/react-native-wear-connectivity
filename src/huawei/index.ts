import type { IWatchConnector } from '../core';

export class HuaweiConnector implements IWatchConnector {
  async connect(): Promise<void> {
    // TODO: implement Huawei-specific connection logic
  }

  async disconnect(): Promise<void> {
    // TODO: implement Huawei-specific disconnection logic
  }

  isConnected(): boolean {
    return false;
  }
}

export default HuaweiConnector;
