import { Platform } from 'react-native';

/**
 * Device types supported by the library. "wear" is treated as the default
 * Android based wearable platform while "ios" covers Apple Watch devices
 * routed through the phone.
 */
export enum DeviceType {
  Wear = 'wear',
  Android = 'android',
  IOS = 'ios',
}

/**
 * Configuration passed to the connector.  Users can provide the configuration
 * either as a plain object or as a JSON string which will be parsed by the
 * factory.
 */
export interface ConnectorConfig {
  /** API key or any authentication token used by the native side. */
  apiKey?: string;
  /** Optional identifier of the device. */
  deviceId?: string;
  /**
   * Additional user supplied options.  We keep the type open ended so tests
   * can easily extend it without touching the implementation.
   */
  [key: string]: any; // eslint-disable-line @typescript-eslint/no-explicit-any
}

/**
 * Base interface implemented by all connectors.  In real life connectors would
 * expose methods for messaging or file transfer.  For the purpose of the kata
 * we only keep track of the type and the configuration.
 */
export interface Connector {
  readonly type: DeviceType;
  readonly config: ConnectorConfig;
}

/** Simple connector implementation used for wear / android devices. */
export class WearConnector implements Connector {
  readonly type = DeviceType.Wear;
  constructor(public readonly config: ConnectorConfig = {}) {}
}

/** Connector used when the host platform is a phone running Android. */
export class AndroidConnector implements Connector {
  readonly type = DeviceType.Android;
  constructor(public readonly config: ConnectorConfig = {}) {}
}

/** Connector used for iOS / watchOS devices. */
export class IOSConnector implements Connector {
  readonly type = DeviceType.IOS;
  constructor(public readonly config: ConnectorConfig = {}) {}
}

/**
 * Helper used to normalise the configuration parameter.  Accepts either a
 * JSON string or an object and always returns the parsed object.
 */
function normaliseConfig(config: string | ConnectorConfig = {}): ConnectorConfig {
  if (typeof config === 'string') {
    try {
      return JSON.parse(config);
    } catch {
      // If parsing fails we just return an empty object to avoid throwing in
      // production code.  Tests can still assert the behaviour with invalid JSON
      // if needed.
      return {};
    }
  }

  return config;
}

/**
 * Factory returning a connector instance based on the supplied device type.
 * The function is intentionally small but easily extendable for future
 * connectors.
 */
export function getConnector(
  type: DeviceType,
  config: string | ConnectorConfig = {}
): Connector {
  const normalised = normaliseConfig(config);

  switch (type) {
    case DeviceType.IOS:
      return new IOSConnector(normalised);
    case DeviceType.Android:
      return new AndroidConnector(normalised);
    case DeviceType.Wear:
    default:
      return new WearConnector(normalised);
  }
}

/**
 * Attempts to automatically detect the connector based on the host platform
 * (iOS or Android).  Additional configuration can optionally be passed in.
 */
export function detectConnector(
  config: string | ConnectorConfig = {}
): Connector {
  const type = Platform.OS === 'ios' ? DeviceType.IOS : DeviceType.Android;
  return getConnector(type, config);
}

export { normaliseConfig };

