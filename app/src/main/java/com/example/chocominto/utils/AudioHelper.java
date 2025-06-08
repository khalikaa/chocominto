package com.example.chocominto.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AudioHelper {
    private static final String TAG = "AudioHelper";
    private static AudioHelper instance;
    private MediaPlayer mediaPlayer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context context;

    private AudioHelper() {
    }

    public static synchronized AudioHelper getInstance() {
        if (instance == null) {
            instance = new AudioHelper();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void playAudio(String url) {
        Log.d(TAG, "Attempting to play audio from URL: " + url);

        releaseMediaPlayer();

        try {
            mediaPlayer = new MediaPlayer();

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);

            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared, starting playback");
                mp.start();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                showToast("Error playing audio: " + what);
                releaseMediaPlayer();
                return true;
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Audio playback completed");
                releaseMediaPlayer();
            });

            mediaPlayer.setDataSource(url);
            Log.d(TAG, "Starting async preparation of MediaPlayer");
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + e.getMessage(), e);
            showToast("Error loading audio: " + e.getMessage());
            releaseMediaPlayer();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
            showToast("Error: " + e.getMessage());
            releaseMediaPlayer();
        }
    }

    private void showToast(String message) {
        if (context != null) {
            mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void release() {
        releaseMediaPlayer();
    }
}