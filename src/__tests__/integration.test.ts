jest.mock('react-native', () => {
  const nativeEventEmitterAddListener = jest.fn(
    (event: string, cb: (...args: any[]) => void) => {
      const subscription = {
        remove: jest.fn(),
      };
      (nativeEventEmitterAddListener as any).subscriptions.push({
        event,
        callback: cb,
        subscription,
      });
      return subscription;
    }
  );
  (nativeEventEmitterAddListener as any).subscriptions = [];

  function MockNativeEventEmitter(this: any) {
    return {
      addListener: nativeEventEmitterAddListener,
    };
  }

  const deviceEventSubscriptions: Array<{
    event: string;
    callback: (...args: any[]) => void;
    subscription: { remove: jest.Mock };
  }> = [];

  const DeviceEventEmitter = {
    addListener: jest.fn((event: string, cb: (...args: any[]) => void) => {
      const subscription = {
        remove: jest.fn(() => {
          const index = deviceEventSubscriptions.findIndex(
            (entry) => entry.subscription === subscription
          );
          if (index >= 0) {
            deviceEventSubscriptions.splice(index, 1);
          }
        }),
      };
      deviceEventSubscriptions.push({ event, callback: cb, subscription });
      return subscription;
    }),
    emit: jest.fn((event: string, payload: unknown) => {
      deviceEventSubscriptions
        .filter((entry) => entry.event === event)
        .forEach((entry) => entry.callback(payload));
    }),
  };

  const Platform = {
    OS: 'android',
    select: jest.fn((options: Record<string, any>) => {
      if (Platform.OS in options) {
        return options[Platform.OS];
      }
      return options.default;
    }),
  };

  const AppRegistry = {
    registerHeadlessTask: jest.fn(),
  };

  const TurboModuleRegistry = {
    getEnforcing: jest.fn(() => ({
      sendMessage: jest.fn(),
      sendFile: jest.fn(),
    })),
  };

  const NativeModules: any = {
    WearConnectivity: {
      sendMessage: jest.fn(),
      sendFile: jest.fn(),
    },
    AndroidWearCommunication: {},
  };

  return {
    AppRegistry,
    DeviceEventEmitter,
    NativeModules,
    NativeEventEmitter: jest.fn(MockNativeEventEmitter),
    Platform,
    TurboModuleRegistry,
    __nativeEventEmitterAddListener: nativeEventEmitterAddListener,
    __deviceEventSubscriptions: deviceEventSubscriptions,
  };
});

describe('react-native-wear-connectivity integration', () => {
  afterEach(() => {
    jest.restoreAllMocks();
    delete (global as any).__turboModuleProxy;
  });

  const setup = (options: { platform?: 'android' | 'ios'; wearConnectivity?: any; turboModule?: any } = {}) => {
    jest.resetModules();

    const reactNative = require('react-native');
    const {
      Platform,
      NativeModules,
      DeviceEventEmitter,
      AppRegistry,
      NativeEventEmitter,
      TurboModuleRegistry,
    } = reactNative;

    Platform.OS = options.platform ?? 'android';
    Platform.select.mockImplementation((spec: Record<string, any>) => {
      if (Platform.OS in spec) {
        return spec[Platform.OS];
      }
      return spec.default;
    });

    if (options.wearConnectivity === null) {
      delete NativeModules.WearConnectivity;
    } else {
      NativeModules.WearConnectivity =
        options.wearConnectivity ?? {
          sendMessage: jest.fn(),
          sendFile: jest.fn(),
        };
    }

    NativeModules.AndroidWearCommunication = {};

    const nativeEventEmitterAddListener =
      reactNative.__nativeEventEmitterAddListener as jest.Mock;
    nativeEventEmitterAddListener.mockClear();
    (nativeEventEmitterAddListener as any).subscriptions = [];

    const deviceEventSubscriptions =
      reactNative.__deviceEventSubscriptions as Array<{
        event: string;
        callback: (...args: any[]) => void;
        subscription: { remove: jest.Mock };
      }>;
    deviceEventSubscriptions.length = 0;

    DeviceEventEmitter.addListener.mockClear();
    DeviceEventEmitter.emit.mockClear();
    AppRegistry.registerHeadlessTask.mockClear();

    TurboModuleRegistry.getEnforcing.mockClear();
    if (options.turboModule) {
      TurboModuleRegistry.getEnforcing.mockReturnValue(options.turboModule);
    } else {
      TurboModuleRegistry.getEnforcing.mockReturnValue({
        sendMessage: jest.fn(),
        sendFile: jest.fn(),
      });
    }

    return {
      reactNative,
      Platform,
      NativeModules,
      DeviceEventEmitter,
      AppRegistry,
      NativeEventEmitter,
      TurboModuleRegistry,
      nativeEventEmitterAddListener,
      deviceEventSubscriptions,
    };
  };

  test('startFileTransfer proxies to native sendFile implementation', async () => {
    const { NativeModules } = setup();
    const sendFileMock = NativeModules.WearConnectivity
      .sendFile as jest.Mock;
    sendFileMock.mockResolvedValue('ok');

    const { startFileTransfer } = require('../index');
    await expect(startFileTransfer('file:///test.txt', { foo: 'bar' })).resolves.toBe(
      'ok'
    );
    expect(sendFileMock).toHaveBeenCalledWith('file:///test.txt', {
      foo: 'bar',
    });
  });

  test('monitorFileTransfers registers and cleans up DeviceEventEmitter listener', () => {
    const { DeviceEventEmitter } = setup();
    const callback = jest.fn();

    const { monitorFileTransfers } = require('../index');
    const unsubscribe = monitorFileTransfers(callback);

    expect(DeviceEventEmitter.addListener).toHaveBeenCalledWith(
      'FileTransferEvent',
      callback
    );

    const subscription =
      (DeviceEventEmitter.addListener as jest.Mock).mock.results[0].value;
    expect(subscription.remove).not.toHaveBeenCalled();

    unsubscribe();
    expect(subscription.remove).toHaveBeenCalledTimes(1);

    const payload = { bytesTransferred: 42 };
    monitorFileTransfers(callback);
    DeviceEventEmitter.emit('FileTransferEvent', payload);
    expect(callback).toHaveBeenCalledWith(payload);
  });

  test('registers WearConnectivity headless task that emits messages', async () => {
    const { AppRegistry, DeviceEventEmitter } = setup();
    require('../index');

    expect(AppRegistry.registerHeadlessTask).toHaveBeenCalledWith(
      'WearConnectivityTask',
      expect.any(Function)
    );

    const [, taskProvider] = AppRegistry.registerHeadlessTask.mock.calls[0];
    const task = taskProvider();
    const payload = { event: 'message', text: 'Hello watch' };

    await task(payload);
    expect(DeviceEventEmitter.emit).toHaveBeenCalledWith('message', payload);
  });

  test('sendMessage delegates to native implementation with custom callbacks', () => {
    const { NativeModules } = setup();
    const sendMessageNative = NativeModules.WearConnectivity
      .sendMessage as jest.Mock;
    sendMessageNative.mockReturnValue('native response');

    const { sendMessage } = require('../messages');

    const message = { text: 'hello' };
    const reply = jest.fn();
    const error = jest.fn();

    const result = sendMessage(message, reply, error);
    expect(sendMessageNative).toHaveBeenCalledWith(
      { text: 'hello', event: 'message' },
      reply,
      error
    );
    expect(result).toBe('native response');
  });
  test('sendMessage uses default callbacks when none provided', () => {
    const { NativeModules } = setup();
    const sendMessageNative = NativeModules.WearConnectivity
      .sendMessage as jest.Mock;

    const logSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    sendMessageNative.mockImplementation(
      (
        _payload: unknown,
        replyCb: (payload: unknown) => void,
        errorCb: (err: string) => void
      ) => {
        replyCb({ acknowledged: true });
        errorCb('fatal');
      }
    );

    const { sendMessage } = require('../messages');
    sendMessage({ id: '123' });

    expect(sendMessageNative).toHaveBeenCalledTimes(1);
    expect(logSpy).toHaveBeenCalledWith(
      'The sendMessage function was called without a callback function. The callback function was invoked with the payload: ',
      { acknowledged: true }
    );
    expect(warnSpy).toHaveBeenCalledWith(
      'The sendMessage function was called without a callback function. The callback function was invoked with the error: ',
      'fatal'
    );
  });

  test('sendMessage warns when invoked on iOS', () => {
    const { NativeModules } = setup({ platform: 'ios' });
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    const { sendMessage } = require('../messages');
    sendMessage({ text: 'hi' });

    expect(NativeModules.WearConnectivity.sendMessage).not.toHaveBeenCalled();
    expect(warnSpy).toHaveBeenCalledWith(
      'react-native-wear-connectivity message does not support iOS. Please use react-native-watch-connectivity library for iOS.'
    );
  });

  test('watchEvents registers listeners for supported events', () => {
    const { nativeEventEmitterAddListener } = setup();
    const { watchEvents } = require('../subscriptions');

    const callback = jest.fn();
    const unsubscribe = watchEvents.addListener('message', callback);

    expect(nativeEventEmitterAddListener).toHaveBeenCalledWith('message', callback);

    const subscription = (nativeEventEmitterAddListener as any).subscriptions[0]
      .subscription;
    expect(subscription.remove).not.toHaveBeenCalled();

    unsubscribe();
    expect(subscription.remove).toHaveBeenCalledTimes(1);
  });

  test('watchEvents.on delegates to addListener', () => {
    const { nativeEventEmitterAddListener } = setup();
    const { watchEvents } = require('../subscriptions');

    const callback = jest.fn();
    watchEvents.on('message', callback);

    expect(nativeEventEmitterAddListener).toHaveBeenCalledWith('message', callback);
  });

  test('watchEvents validates inputs and throws on unsupported events', () => {
    setup();
    const { watchEvents } = require('../subscriptions');

    expect(() => watchEvents.addListener('', jest.fn())).toThrow('Must pass event');
    expect(() => watchEvents.addListener('unknown', jest.fn())).toThrow(
      'Unknown watch event "unknown"'
    );
  });

  test('watchEvents warns and no-ops on iOS', () => {
    setup({ platform: 'ios' });
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    const { watchEvents } = require('../subscriptions');
    const unsubscribe = watchEvents.addListener('message', jest.fn());
    expect(typeof unsubscribe).toBe('function');
    unsubscribe();

    expect(warnSpy).toHaveBeenCalledWith(
      'react-native-wear-connectivity watchEvents does not support iOS. Please use react-native-watch-connectivity library for iOS.'
    );
  });

  test('WearConnectivity proxy throws descriptive error when module missing', () => {
    setup({ wearConnectivity: null });

    const { WearConnectivity } = require('../index');
    expect(() => (WearConnectivity as any).sendFile('path', {})).toThrow(
      "The package 'react-native-wear-connectivity' doesn't seem to be linked."
    );
  });

  test('prefers TurboModule implementation when available', () => {
    const turboModule = {
      sendMessage: jest.fn(),
      sendFile: jest.fn(),
    };
    (global as any).__turboModuleProxy = {};

    const { TurboModuleRegistry } = setup({ turboModule });

    const { startFileTransfer, sendMessage } = require('../index');

    startFileTransfer('file:///turbo.bin', {});
    sendMessage({ text: 'from turbo' });

    expect(TurboModuleRegistry.getEnforcing).toHaveBeenCalledWith(
      'WearConnectivity'
    );
    expect(turboModule.sendFile).toHaveBeenCalledWith('file:///turbo.bin', {});
    expect(turboModule.sendMessage).toHaveBeenCalledWith(
      { text: 'from turbo', event: 'message' },
      expect.any(Function),
      expect.any(Function)
    );
  });
});
