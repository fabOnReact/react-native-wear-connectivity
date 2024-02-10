import React from 'react';
import { View, StyleSheet, Text } from 'react-native';

function CounterScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.counter}>We don't support iOS</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 8,
    backgroundColor: 'yellow',
  },
  counter: {
    height: 100,
    width: 400,
    textAlign: 'center',
    fontSize: 50,
    fontWeight: 'bold',
  },
});

export default CounterScreen;
