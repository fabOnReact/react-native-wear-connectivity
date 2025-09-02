import { sendMessage, watchEvents } from 'react-native-watch-connectivity';

// Send a message to the Apple Watch
sendMessage({ text: 'Hello watch!' }, (reply) => {
  console.log('Reply from watch:', reply);
});

// Listen for messages from the Apple Watch
const unsubscribe = watchEvents.on('message', (message, reply) => {
  console.log('Received message from watch', message);
  reply({ text: 'Thanks watch!' });
});

// Call unsubscribe() when no longer needed
