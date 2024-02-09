import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import LoginScreen from './LoginScreen';
import WearScreen from './WearScreen';
import { GoogleSignin } from '@react-native-google-signin/google-signin';
import { multiply } from 'react-native-wear-connectivity';

const App = () => {
  const [result, setResult] = React.useState<number | undefined>();
  React.useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);
  return (
    <>
      <LoginScreen />
      <WearScreen />
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});

export default App;
