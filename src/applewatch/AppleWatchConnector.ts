import Watch, { watchEvents } from 'react-native-watch-connectivity';
import type {
  Payload,
  ReplyCallback,
  ErrorCallback,
} from '../NativeWearConnectivity';

/**
 * Connector responsible for activating the WatchConnectivity session and
 * forwarding messages between the iOS app and the paired Apple Watch.
 */
class AppleWatchConnector {
  private activated = false;

  /**
   * Ensures that the underlying WCSession is activated before any
   * communication attempts.
   */
  activateSession() {
    if (this.activated) {
      return;
    }
    try {
      Watch.activateSession();
      this.activated = true;
    } catch (err) {
      console.warn('Failed to activate Apple Watch session', err);
    }
  }

  /**
   * Sends a message to the paired Apple Watch device.
   */
  sendMessage(
    message: Payload,
    cb?: ReplyCallback,
    errCb?: ErrorCallback
  ) {
    this.activateSession();
    Watch.sendMessage(message, cb, errCb);
  }
}

const appleWatchConnector = new AppleWatchConnector();

export { appleWatchConnector, watchEvents as appleWatchEvents };
export default appleWatchConnector;
