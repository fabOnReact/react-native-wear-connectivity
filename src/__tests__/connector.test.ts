import { Platform } from 'react-native';
import { detectConnector, getConnector, DeviceType } from '../utilities';

describe('connector factory', () => {
  it('creates wear connector from factory', () => {
    const connector = getConnector(DeviceType.Wear, { deviceId: 'abc' });
    expect(connector.type).toBe(DeviceType.Wear);
    expect(connector.config).toEqual({ deviceId: 'abc' });
  });

  it('parses configuration supplied as JSON', () => {
    const connector = getConnector(DeviceType.IOS, '{"apiKey":"123"}');
    expect(connector.type).toBe(DeviceType.IOS);
    expect(connector.config).toEqual({ apiKey: '123' });
  });

  it('detects connector based on Platform.OS', () => {
    const original = Platform.OS;

    (Platform as any).OS = 'ios';
    const ios = detectConnector();
    expect(ios.type).toBe(DeviceType.IOS);

    (Platform as any).OS = 'android';
    const android = detectConnector();
    expect(android.type).toBe(DeviceType.Android);

    // restore original platform
    (Platform as any).OS = original;
  });
});

