package com.rnllamaexample;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.rnllama.LlamaContext;
import com.hdb.avatar.AvatarPlayer;
import com.hdb.avatar.EmotionType;
import com.hdb.avatar.IAvatarPlayerEvents;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements IAvatarPlayerEvents {
  static public String TAG = "FSA" ;
    private final Handler mHandler = new Handler(Looper.myLooper());
    ImageButton btnSubmit ;
    ImageButton btnStop ;
    ImageView ivBackground;
    TextView tvResponse ;
    EditText etInput ;

    BroadcastReceiver receiver ;
    TtsHelper ttsHelper;

    private AvatarPlayer mAvatarPlayer;
    boolean isAutoScroll = true ;
    String bufferMessage = "" ;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
//            if (Build.VERSION.SDK_INT >= 30) {
//                getWindowInsetsController().hide(
//                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
//            } else {
//                // Note that some of these constants are new as of API 16 (Jelly Bean)
//                // and API 19 (KitKat). It is safe to use them, as they are inlined
//                // at compile-time and do nothing on earlier devices.
//                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//            }
        }
    };
  private View.OnClickListener onSubmit = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      isAutoScroll = true ;
      bufferMessage = "" ;

      String text = etInput.getText().toString();
      //boolean ret = LlamaHelper.shared.talk(text);

      if (BuildConfig.DEBUG){
        if (text.length() == 0){
          int a = (int)(System.currentTimeMillis() % 10);
          int b = (int)((System.currentTimeMillis()*12345) % 10);
          text = String.format("%d + %d = ", a, b);
          etInput.setText(text, null);
        }
      }
      TalkTask task = new TalkTask();
      task.execute(text);
    }
  };
  private View.OnClickListener onStop = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      LlamaHelper.shared.lctx.stopCompletion();
      toggleState(State.FREE);
    }
  };

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Common.act = this;
        setContentView(R.layout.activity_fullscreen);
        ttsHelper = new TtsHelper(this);

        FrameLayout avatarLayout = findViewById(R.id.AvatarLayout);
      mAvatarPlayer = new AvatarPlayer(this, avatarLayout, this);
        mAvatarPlayer.loadAvatar("https://models.readyplayer.me/656ee050869b42cd909818a8.glb");


       btnSubmit  = findViewById(R.id.btnSubmit);
       btnStop = findViewById(R.id.btnStop);
       ivBackground = findViewById(R.id.ivBackground);
       tvResponse = findViewById(R.id.tvResponse);
      tvResponse.setMovementMethod(new ScrollingMovementMethod());
      tvResponse.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          isAutoScroll = false;
          return false;
        }
      });

       etInput = findViewById(R.id.etInput);
       btnSubmit.setOnClickListener(onSubmit);
        btnStop.setOnClickListener(onStop);

      btnSubmit.setEnabled(false);
         new Thread(){
           @Override
           public void run() {
             super.run();
             LlamaHelper.init(FullscreenActivity.this);
             Log.e(TAG, "Module Init Success") ;
             mHandler.post(()->{
               btnSubmit.setEnabled(true);
             });
           }
         }.start();


    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("token")) {
          String text = intent.getStringExtra("token");
          bufferMessage += text ;
          mHandler.post(()->{
            processSplit();
          });
          tvResponse.append(text);
          if ( isAutoScroll) {
            final int scrollAmount = tvResponse.getLayout().getLineTop(tvResponse.getLineCount()) - tvResponse.getHeight();
            if (scrollAmount > 0)
              tvResponse.scrollTo(0, scrollAmount + 50);
            else
              tvResponse.scrollTo(0, 0);
          }
        }
      }
    };
      IntentFilter reg = new IntentFilter("com.rnllama.send");
         registerReceiver(receiver, reg);

      }

      public void processSplit(){
        boolean hasDot = bufferMessage.contains(".") || bufferMessage.contains(",") || bufferMessage.contains("\n");
        if (hasDot){
          processTTSAsync(bufferMessage);
          bufferMessage = "" ;
        }
      }
      public void processTTSAsync(String msg){
        new Thread(){
          @Override
          public void run() {
            processTTS(msg);
          }
        }.start();
      }
      public void processTTS(String msg){
        if (msg.trim().length() == 0) {
          return ;
        }
        Log.e(TAG, "processTTS: " + msg);
        ttsHelper.convertTextToSpeechAndSaveToFile(msg);
      }
  @Override
  protected void onDestroy() {
    mAvatarPlayer.destroy();
    super.onDestroy();
    //unregisterReceiver(receiver);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mAvatarPlayer.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAvatarPlayer.resume();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    mAvatarPlayer.windowFocusChanged(hasFocus);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mAvatarPlayer.configurationChanged(newConfig);
  }

  @Override
  public void onLoadAvatarComplete(boolean success) {
    Log.e(TAG, "Load = " + success) ;
//    if (success) {
//          mAvatarPlayer.speak("https://unity-chan.com/sounds/voice/kohaku01.mp3", EmotionType.sad, true);
//          mAvatarPlayer.speak("https://unity-chan.com/sounds/voice/kohaku02.mp3", EmotionType.angry, true);
//          mAvatarPlayer.speak("https://audio-samples.github.io/samples/mp3/blizzard_tts_unbiased/sample-0/real.mp3", EmotionType.happy, true);
//          mAvatarPlayer.speak("https://audio-samples.github.io/samples/mp3/blizzard_tts_unbiased/sample-3/real.mp3", EmotionType.surprised, true);
//    }
  }

  enum State {
    TALKING, INIT, FREE
  }
  public void toggleState(State state){
    if (state == State.TALKING){
      btnSubmit.setVisibility(View.GONE);
      btnStop.setVisibility(View.VISIBLE);
    }
    else if (state == State.FREE){
      btnSubmit.setVisibility(View.VISIBLE);
      btnStop.setVisibility(View.GONE);
    }
  }

  class TalkTask extends AsyncTask<String, Void, Void> {

    boolean ret = false ;
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      Log.e(TAG, "Task Start");
      tvResponse.setText("");
      toggleState(State.TALKING);
    }

    protected Void doInBackground(String... lines) {
      ret = LlamaHelper.shared.talk(lines[0]);
      return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
      super.onPostExecute(unused);
      Log.e(TAG, "Task End, FREE");
      toggleState(State.FREE);
      if ( ret == true){
        etInput.getText().clear();
      }

      // talk the last
      processTTSAsync(bufferMessage);
      bufferMessage = "" ;
    }
  }
}
