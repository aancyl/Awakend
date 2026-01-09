package game2D;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReverbAudioPlayer {
    private static final float DECAY = 0.5f;                                    // Controls echo fade out
    private static final int DELAY_MS = 100;                                    // Cooldown for the echo
    private static final int BUFFER_SIZE = 4096;                                // For a smoother playback

    public static void playReverbAudio(String filePath) {
        new Thread(() -> {
            try {

                // Load the audio file
                File audioFile = new File(filePath);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                AudioFormat format = audioInputStream.getFormat();

                // Change the format of the audio file
                if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                    format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(), 16,
                            format.getChannels(), format.getChannels() * 2,
                            format.getSampleRate(), false);
                    audioInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
                }

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
                sourceLine.open(format);
                sourceLine.start();

                // Delay buffer for reverb effect
                int delaySamples = (int) ((DELAY_MS / 1000.0) * format.getSampleRate());
                float[] delayBuffer = new float[delaySamples];
                int delayIndex = 0;

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                // Process the audio in chunks
                while ((bytesRead = audioInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead).order(ByteOrder.LITTLE_ENDIAN);
                    
                    for (int i = 0; i < bytesRead / 2; i++) {
                        // Convert bytes to sample value
                        short sample = byteBuffer.getShort(i * 2); 

                        // Add the reverb effect using the delay buffer
                        float delayedSample = delayBuffer[delayIndex] * DECAY;
                        float newSample = sample + delayedSample;
                        delayBuffer[delayIndex] = newSample;
                        delayIndex = (delayIndex + 1) % delaySamples; 

                        // Reduce distortion within valid range
                        short processedSample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int) newSample));

                        // Convert back to bytes
                        byteBuffer.putShort(i * 2, processedSample);
                    }

                    // Play the processed audio
                    sourceLine.write(buffer, 0, bytesRead);
                }

                // Clean up resources
                sourceLine.drain();
                sourceLine.close();
                audioInputStream.close();
                
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            }
        }).start();
    }
}
