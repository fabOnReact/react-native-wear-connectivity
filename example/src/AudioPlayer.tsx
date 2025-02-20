import React, { useEffect, useState, useRef } from 'react';
import { View, Button, Text } from 'react-native';
import Video from 'react-native-video';
import { monitorFileTransfers } from 'react-native-wear-connectivity';

const AudioPlayer = () => {
  const [isPlaying, setIsPlaying] = useState(false);
  const playerRef = useRef<Video>(null);
  const [fileUri, setFileUri] = useState<string | null>(null);

  useEffect(() => {
    const cancel = monitorFileTransfers((transferInfo) => {
      console.log('File transfer event:', transferInfo);

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
            audioOnly
            paused={!isPlaying}
            onEnd={() => setIsPlaying(false)}
            onError={(e) => console.log('Audio error:', e)}
            style={{ height: 0 }} // Hide video UI
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

export default AudioPlayer;
