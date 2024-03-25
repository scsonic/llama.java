package com.rnllamaexample;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class TtsHelper {

  static public String TAG = "TtsHelper";

  private TextToSpeech textToSpeech;
  private Context context;

  int fileCnt = 0;
  onDoneCallback cb ;

  interface onDoneCallback {
    default void onDone(String path){

    }
    void onFail(String message);
  }

  public TtsHelper(Context context) {
    this.context = context;
    initializeTextToSpeech();
  }

  private void initializeTextToSpeech() {
    textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
//          int result = textToSpeech.setLanguage(Locale.US);
//
//          if (result == TextToSpeech.LANG_MISSING_DATA ||
//            result == TextToSpeech.LANG_NOT_SUPPORTED) {
//            Log.e("TtsHelper", "Language is not supported or missing data");
//            cb.onFail("TTS Fail: Not Support Lang");
//          }
        } else {
          Log.e("TtsHelper", "Initialization failed");
          cb.onFail("TTS INIT Fail:" + status);
        }
      }
    });
  }

  public String convertTextToSpeechAndSaveToFile(final String text) {
    fileCnt = (fileCnt+1)%10;
    final String fileName = "output" + fileCnt + ".wav";

    // Set up file path
    //final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
    final File file = new File(context.getCacheDir(), fileName);
    Log.e(TAG, "wav path=" + file.toString());
    // Set up TTS parameters
    //HashMap<String, String> params = new HashMap<>();
    //params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, fileName);
    Bundle myparams = new Bundle();
    myparams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, fileName);

    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
      @Override
      public void onStart(String utteranceId) {
        Log.i(TAG, "TTS Start:" + utteranceId);
      }

      @Override
      public void onDone(String utteranceId) {

        try {
          // Set up file path for WAV
          File wavFile = new File(context.getCacheDir(), utteranceId);
          Log.e(TAG, "TTS Done:" + utteranceId + " FileSIze=" + wavFile.length());
          //playStoredWavFile(wavFile.getAbsolutePath());
          if ( cb != null ) {
            cb.onDone(wavFile.getAbsolutePath());
            //cb.onFail("Finish:" + utteranceId);
          }

        } catch (Exception e) {
          Log.e(TAG, "Write file fail", e) ;
          cb.onFail("Write File Error:" + e.getMessage());
        }
      }

      @Override
      public void onError(String utteranceId) {
        Log.e("TtsHelper", "Text-to-Speech conversion failed:" + utteranceId);
        cb.onFail("TTS onError:" + utteranceId);
      }
    });




    // Convert text to speech
    //textToSpeech.synthesizeToFile(text, params, file.getAbsolutePath());


    textToSpeech.synthesizeToFile(text, myparams, file, fileName);
    return file.getAbsolutePath();
  }


  public void playStoredWavFile(String path){
    // 設定下載目錄下的 WAV 檔案路徑
    // 初始化 MediaPlayer
    MediaPlayer mediaPlayer = new MediaPlayer();

    try {
      mediaPlayer.setDataSource(path);
      mediaPlayer.prepare();
      mediaPlayer.start();
      mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
          Log.i("MediaPlayer", "Playback completed");
        }
      });
    } catch (IOException e) {
      Log.e("MediaPlayer", "Error setting data source", e);
    }
  }

  public void playStoredWavFile_old(String path) {
    final File file = new File(path);

    try (FileInputStream fis = new FileInputStream(file)) {
      int fileSize = (int) file.length();
      byte[] audioData = new byte[fileSize];

      fis.read(audioData, 0, fileSize);

      AudioTrack audioTrack = new AudioTrack.Builder()
        .setAudioAttributes(new AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build())
        .setAudioFormat(new AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(44100)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build())
        .setBufferSizeInBytes(fileSize)
        .build();

      audioTrack.play();
      audioTrack.write(audioData, 0, fileSize);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void shutdown() {
    if (textToSpeech != null) {
      textToSpeech.stop();
      textToSpeech.shutdown();
    }
  }
}
