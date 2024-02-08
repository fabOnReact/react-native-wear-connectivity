import React from 'react';
import LoginScreen from './LoginScreen';
import WearScreen from './WearScreen';
import { GoogleSignin } from '@react-native-google-signin/google-signin';

const App = () => {
  return (
    <>
      <LoginScreen />
      <WearScreen />
    </>
  );
};

export default App;
