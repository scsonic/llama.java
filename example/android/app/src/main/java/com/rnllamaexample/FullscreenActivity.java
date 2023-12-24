package com.rnllamaexample;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.rnllama.LlamaContext;
import java.nio.charset.StandardCharsets;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
  static public String TAG = "FSA" ;
    private final Handler mHandler = new Handler(Looper.myLooper());
    Button btnSubmit ;
    TextView tvResponse ;
    EditText etInput ;

    BroadcastReceiver receiver ;

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
      String text = etInput.getText().toString();
      //boolean ret = LlamaHelper.shared.talk(text);
      TalkTask task = new TalkTask();
      task.execute(text);
    }
  };

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
       btnSubmit  = findViewById(R.id.btnSubmit);
       tvResponse = findViewById(R.id.tvResponse);
       etInput = findViewById(R.id.etInput);

       btnSubmit.setOnClickListener(onSubmit);


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

//         receiver = new LlamaReceiver();
//          IntentFilter reg = new IntentFilter("com.rnllama.send");
//         registerReceiver(receiver, reg);

    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("token")) {
          Log.e(TAG, "On Receive v2:" + intent.getStringExtra("token"));
        }
      }
    };
      IntentFilter reg = new IntentFilter("com.rnllama.send");
         registerReceiver(receiver, reg);

      }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //unregisterReceiver(receiver);
  }

  enum State {
    TALKING, INIT, END
  }
  public void toggleState(State state){
    if (state == State.TALKING){
      btnSubmit.setEnabled(false);
    }
    else if (state == State.END){
      btnSubmit.setEnabled(true);
    }
  }

  class TalkTask extends AsyncTask<String, Void, Void> {

    boolean ret = false ;
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      toggleState(State.TALKING);
    }

    protected Void doInBackground(String... lines) {

      ret = LlamaHelper.shared.talk(lines[0]);
      return null;
    }
    protected void onPostExecute(Long result) {
      toggleState(State.END);
      if ( ret == true){
        etInput.getText().clear();
      }
    }
  }
}
