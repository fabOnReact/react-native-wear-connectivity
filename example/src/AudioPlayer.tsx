import React, { useEffect, useState, useRef } from 'react';
import { View, Button, Text, StyleSheet } from 'react-native';
import Video, { type VideoRef } from 'react-native-video';
import { monitorFileTransfers } from 'react-native-wear-connectivity';

const AudioPlayer = () => {
  const [isPlaying, setIsPlaying] = useState(false);
  const playerRef = useRef<VideoRef>(null);
  const [fileUri, setFileUri] = useState<string | null>(null);
  useEffect(() => {
    const cancel = monitorFileTransfers((transferInfo) => {
      if (transferInfo.type === 'finished' && transferInfo.url) {
        setFileUri(transferInfo.url);
      }
    });

    return () => cancel(); // Cleanup listener
  }, []);

  return (
    <View>
      <Text>
        {fileUri ? 'Audio file received' : 'Waiting for audio file...'}
      </Text>
      {fileUri && (
        <>
          <Video
            ref={playerRef}
            source={{ uri: fileUri }}
            paused={!isPlaying}
            onEnd={() => setIsPlaying(false)}
            onError={(e) => console.log('Audio error:', e)}
            style={styles.videoStyle} // Hide video UI
          />
          <Button
            title={isPlaying ? 'Pause' : 'Play'}
            onPress={() => setIsPlaying(!isPlaying)}
          />
        </>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  videoStyle: {
    height: 0,
  },
});

export default AudioPlayer;
