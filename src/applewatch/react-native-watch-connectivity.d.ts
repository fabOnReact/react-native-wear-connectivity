declare module 'react-native-watch-connectivity' {
  import { EmitterSubscription } from 'react-native';

  export interface WatchConnectivity {
    activateSession(): void;
    sendMessage(
      message: any,
      reply?: (data: any) => void,
      error?: (err: any) => void
    ): void;
  }

  export const watchEvents: {
    addListener: (
      event: string,
      listener: (...args: any[]) => void
    ) => EmitterSubscription;
    on: (
      event: string,
      listener: (...args: any[]) => void
    ) => EmitterSubscription;
  };

  const Watch: WatchConnectivity;
  export default Watch;
}
