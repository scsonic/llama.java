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

import com.hdb.avatar.AvatarPlayer;
import com.hdb.avatar.EmotionType;
import com.hdb.avatar.ModelHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TtsHelper implements Runnable{

  static public String TAG = "TtsHelper";

  private TextToSpeech textToSpeech;
  private Context context;
  String emptyPath = "" ;

  Thread thread ;
  boolean running = true ;
  boolean isPlaying = false;
  ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>() ;


  int fileCnt = 0;
  onDoneCallback cb ;
  public AvatarPlayer player ;


  public void sleep(int ms){
    try {
      Thread.sleep(ms) ;
    } catch (InterruptedException e) {
    }
  }

  @Override
  public void run() {
    while(running) {
      if (queue.size() == 0 ) {
        Log.e(TAG, "Queue is empty");
        sleep(1000) ;
      }
      else {
        if (!isPlaying) {
          String path = queue.poll();
          Log.e(TAG, "Queue Play :" + path) ;
          playStoredWavFile(path);
          if (player != null){
            Log.e(TAG, "Queue speak:" + emptyPath) ;
            player.speak("file://" + emptyPath, EmotionType.happy, false);
          }
        }
        else {
          Log.e(TAG, "Queue is not empty:" + queue.size() + " , but playing");
          sleep(500) ;
        }
      }
    }
  }

  interface onDoneCallback {
    default void onDone(String path){

    }
    void onFail(String message);
  }

  public TtsHelper(Context context) {
    this.context = context;
    initializeTextToSpeech();
    thread = new Thread(this) ;
    thread.start() ;
  }

  private void initializeTextToSpeech() {
    textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        emptyPath = ModelHelper.copyRawFileToCache(context, R.raw.empty, "empty.wav");

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
   // Log.e(TAG, "wav path=" + file.toString() + " size:" + file.length());
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
            //cb.onDone(wavFile.getAbsolutePath());
            //cb.onFail("Finish:" + utteranceId);
          }
          queue.add(wavFile.getAbsolutePath());

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

    isPlaying = true ;
    MediaPlayer mediaPlayer = new MediaPlayer();

    try {
      mediaPlayer.setDataSource(path);
      mediaPlayer.prepare();
      mediaPlayer.start();
      mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
          Log.i("MediaPlayer", "Playback completed:" + path);
          isPlaying = false;
        }
      });
    } catch (IOException e) {
      Log.e("MediaPlayer", "Error setting data source:"+path, e);
      isPlaying = false;
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
    running = false ;
  }
}
