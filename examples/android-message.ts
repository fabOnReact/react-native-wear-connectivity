import { sendMessage, watchEvents } from 'react-native-wear-connectivity';

// Send a message to the WearOS device
sendMessage({ text: 'Hello watch!' }, (reply) => {
  console.log('Reply from watch:', reply);
});

// Listen for messages from the WearOS device
const unsubscribe = watchEvents.on('message', (message) => {
  console.log('Received message from watch', message);
});

// Call unsubscribe() when no longer needed
