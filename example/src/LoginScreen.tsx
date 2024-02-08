import React from 'react';
import {
  View,
  StyleSheet,
  Text,
  TextInput,
  NativeModules,
  Button,
} from 'react-native';
import {GoogleSignin} from '@react-native-google-signin/google-signin';
import Config from 'react-native-config';

export default function LoginScreen() {
  const signIn = async () => {
    try {
      GoogleSignin.configure({
        androidClientId: Config.GOOGLE_ANDROID_CLIENT_ID,
      });
      const userInfo = await GoogleSignin.signIn();
      console.log(JSON.stringify(userInfo));
    } catch (error) {
      console.log('ERROR IS: ' + JSON.stringify(error));
    }
  };

  return (
    <View>
      <Button title={'Sign in with Google'} onPress={signIn} />
    </View>
  );
}
