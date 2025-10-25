import type { Payload, ReplyCallback, ErrorCallback } from '../NativeWearConnectivity';
import { sendMessage } from '../messages';
import { watchEvents } from '../subscriptions';

/**
 * Simple helper class that forwards messages between the React Native app and the Wear OS app.
 *
 * It wraps the existing `sendMessage` API and exposes a convenient method to
 * subscribe to incoming messages coming from the Wear OS companion application.
 */
class WearOSConnector {
  private unsubscribe?: () => void;

  /**
     * Register a listener for messages coming from the Wear OS app.
     *
     * @param listener Callback invoked every time a message is received.
     * @returns Function to remove the registered listener.
     */
  connect(listener: (message: Payload) => void) {
    this.unsubscribe = watchEvents.on('message', listener);
    return this.unsubscribe;
  }

  /**
     * Remove the current message listener, if any.
     */
  disconnect() {
    if (this.unsubscribe) {
      this.unsubscribe();
      this.unsubscribe = undefined;
    }
  }

  /**
     * Send a message to the Wear OS app.
     *
     * @param message Message payload to send.
     * @param cb Optional callback invoked on success.
     * @param errCb Optional callback invoked on error.
     */
  send(message: Payload, cb?: ReplyCallback, errCb?: ErrorCallback) {
    sendMessage(message, cb, errCb);
  }
}

const connector = new WearOSConnector();
export default connector;
export type { Payload };
