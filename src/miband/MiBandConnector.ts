import { EventEmitter } from 'events';
import type { AddListener, WatchEvents } from '../types';
// The rn-miband-connector library exposes an EventEmitter that
// delivers raw Mi Band events. We translate those events to the
// shared `watchEvents` interface used by this project.
//
// We keep the dependency typed as `any` to avoid requiring its
// TypeScript definitions.
// eslint-disable-next-line @typescript-eslint/no-var-requires
const MiBand: any = require('rn-miband-connector');

const miband = new MiBand();
const emitter = new EventEmitter();

// Forward any event from the Mi Band library as a `message` event so
// that consumers can subscribe via `watchEvents.on('message', cb)`.
miband.on('data', (payload: unknown) => {
  emitter.emit('message', payload);
});

// Some implementations might emit a generic `event` callback instead
// of `data`. Handle that as well.
miband.on('event', (payload: unknown) => {
  emitter.emit('message', payload);
});

const addListener: AddListener = (event, cb) => {
  if (event !== 'message') {
    throw new Error(`Unknown watch event "${event}"`);
  }

  emitter.on(event, cb);
  return () => {
    emitter.removeListener(event, cb);
  };
};

export const mibandEvents: WatchEvents = {
  addListener,
  on: addListener,
};

export default mibandEvents;
