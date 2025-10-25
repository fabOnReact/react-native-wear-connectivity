import { NativeEventEmitter } from 'react-native';
import GarminConnect from 'react-native-garmin-connect';

/**
 * Provides a small wrapper around the Garmin Connect SDK.
 * The connector exposes a simple API used by the library to
 * communicate with Garmin wearables.
 */
class GarminConnector {
  private emitter: NativeEventEmitter;

  constructor() {
    // GarminConnect is a native module; wrap it with an event emitter so we
    // can subscribe to SDK events from JavaScript.
    this.emitter = new NativeEventEmitter(GarminConnect as any);
  }

  /**
   * Initializes the Garmin SDK.
   */
  initialize(): Promise<void> {
    // The SDK exposes an initialize call which prepares the connection layer.
    return GarminConnect.initialize();
  }

  /**
   * Sends a payload to a paired Garmin device.
   */
  sendMessage(message: Record<string, unknown>): Promise<void> {
    return GarminConnect.sendMessage(message);
  }

  /**
   * Adds a listener for incoming messages from the Garmin device.
   * Returns an unsubscribe function to remove the listener.
   */
  onMessage(callback: (message: any) => void): () => void {
    const subscription = this.emitter.addListener('message', callback);
    return () => subscription.remove();
  }
}

export default new GarminConnector();

