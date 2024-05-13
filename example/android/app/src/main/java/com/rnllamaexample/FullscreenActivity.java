package com.rnllamaexample;

import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.hdb.avatar.ModelHelper;
import com.hdb.avatar.AvatarPlayer;
import com.hdb.avatar.EmotionType;
import com.hdb.avatar.IAvatarPlayerEvents;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements IAvatarPlayerEvents, TtsHelper.onDoneCallback {
  static public String TAG = "FSA" ;


  boolean testToggle = false;
  boolean isForceLanguage = true ;
    private final Handler mHandler = new Handler(Looper.myLooper());
    ImageButton btnSubmit ;
    ImageButton btnStop ;
    ImageView ivBackground;
    TextView tvResponse ;
    EditText etInput ;
    ProgressBar pbLoading;
  ImageButton btnMore;

  MaterialButtonToggleGroup mbtgLang;
  MaterialButton btnLang0 ;
  MaterialButton btnLang1 ;
  MaterialButton btnLang2 ;
  MaterialButton btnLang3 ;
  MaterialButton btnLangMore ;

    BroadcastReceiver receiver ;
    TtsHelper ttsHelper;
    String lastRagContent = "" ;

    private AvatarPlayer mAvatarPlayer;
    boolean isAutoScroll = true ;
    String bufferMessage = "" ; // for buffered to TTS
    String responseMessage = "" ; // for catch all response to
    Button btnRag ;
    Button btnChatControl ;
    Button btnNewSession;

    boolean isProcessing = false ;


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
      Log.i(TAG, "Press OnSubmit");

      if (isProcessing){
        Log.i(TAG, "Running a chat before");
        return ;
      }
      isProcessing = true;

      String text = etInput.getText().toString();
      if (BuildConfig.DEBUG) {
        if (text.length() == 0) {

          if (testToggle) {
            text = "I will like to build a 20 sqrt ft balcony, can you give me a list of materials and the price estimation ?";
          }
          else {
            text = "What kind of drill bits I need to drill a bathroom tile ?" ;
          }
          testToggle = !testToggle ;
        }
        etInput.setText(text, null);
      }
      if ( RagApi.RagEnable ) {
        String finalText = text;
        RagApi.callApi(text, new RagApi.RagCallback() {
          @Override
          public void onSuccess(String response) {
            Log.e(TAG, "Rag Result=" + response);
            lastRagContent = RagApi.RAG_PREPROMPT +  response ;
            tvResponse.setText("RAG Response:\n" + response);
            TalkTask task = new TalkTask();
            task.execute(finalText, response);
          }

          @Override
          public void onError(String errorMessage) {
            lastRagContent = "Init Rag Query Fail, please start Rag Service Again" ;
            Log.e(TAG, "onError=" + errorMessage);
            isProcessing = false ;
            tvResponse.setText(lastRagContent);
          }
        });
      }
      else {
        TalkTask task = new TalkTask();
        task.execute(text);
      }
    }
  };
  private View.OnClickListener onStop = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      LlamaHelper.shared.lctx.stopCompletion();
      toggleState(State.FREE);
      isProcessing = false ;
    }
  };

  private Intent mManageExternalStorageIntent;
  ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
    new ActivityResultCallback<ActivityResult>() {
      @Override
      public void onActivityResult(ActivityResult result) {
        if (!Environment.isExternalStorageManager()) {
          Log.d(TAG, "無存取外部檔案權限, 強制停留在要權限畫面");
          mStartForResult.launch(mManageExternalStorageIntent);
        }
      }
    });


  public void setupTTSButton(){

    btnLang0 = findViewById(R.id.btnLang0);
    btnLang1 = findViewById(R.id.btnLang1);
    btnLang2 = findViewById(R.id.btnLang2);
    btnLang3 = findViewById(R.id.btnLang3);
    btnLangMore = findViewById(R.id.btnLangMore);


    btnLang0.setOnClickListener((v)->{
      ttsHelper.textToSpeech.setLanguage(Locale.ENGLISH);
    });

    btnLang1.setOnClickListener((v)->{
      ttsHelper.textToSpeech.setLanguage(Locale.CHINESE);
    });

    btnLang2.setOnClickListener((v)->{
      ttsHelper.textToSpeech.setLanguage(Locale.FRANCE);
    });

    btnLang3.setOnClickListener((v)->{
      ttsHelper.textToSpeech.setLanguage(Locale.GERMANY);
    });

    btnLangMore.setOnClickListener((v)->{
      ttsHelper.UIShowLanguageSelection(FullscreenActivity.this);
    });

  }



  private View.OnClickListener onMorePress = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      ArrayList<String> itemList = new ArrayList<>();
      itemList.add("Select Avatar");
      itemList.add("Select Background");
      itemList.add("Toggle Rag Mode, current=" + RagApi.RagEnable);

      itemList.add("Test Play Audio(Avatar)");
      itemList.add("Test Play Audio(MediaPlayer)");
      itemList.add("Select TTS Language");

      // 创建AlertDialog.Builder
      AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
      builder.setTitle("Select Function");

      String testWav = ModelHelper.copyRawFileToCache(getApplicationContext(), R.raw.beep, "beep.wav");
      // 设置列表适配器
      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(FullscreenActivity.this, android.R.layout.simple_list_item_1, itemList);
      builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          // 处理选项点击事件
          String selectedItem = itemList.get(which);
          int raw_id = 0;
          if (which == 0 ){
            UIShowModelSelection();
          }
          else if (which == 1 ){
            UIShowBackgroundSelection();
          }
          else if (which == 2 ){
            RagApi.RagEnable = !RagApi.RagEnable;
//            Intent i = new Intent(FullscreenActivity.this, ShellActivity.class);
//            startActivity(i);
          }
          else if (which == 3 ){
            mAvatarPlayer.speak("file://" + testWav, EmotionType.happy, false);
          }
          else if (which == 4 ) {
            ttsHelper.playStoredWavFile(testWav);
          }
          else if (which == 5) {
            ttsHelper.UIShowLanguageSelection(FullscreenActivity.this);
          }
        }
      });

      builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
      });

      AlertDialog alertDialog = builder.create();
      alertDialog.show();
    }
  };

  public void UIShowModelSelection(){
    ArrayList<String> itemList = new ArrayList<>();
    itemList.add("james.glb");
    itemList.add("james2.glb");
    itemList.add("engineer.glb");
    itemList.add("david.glb");
    itemList.add("david_formal.glb");
    itemList.add("mcdonald.glb");

    // 创建AlertDialog.Builder
    AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
    builder.setTitle("Select Model");

    // 设置列表适配器
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(FullscreenActivity.this, android.R.layout.simple_list_item_1, itemList);
    builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 处理选项点击事件
        String selectedItem = itemList.get(which);
        int raw_id = 0;
        if (which == 0 ){
          raw_id = R.raw.james;
        }
        else if (which == 1 ){
          raw_id = R.raw.james2;
        }
        else if (which == 2 ){
          raw_id = R.raw.engineer;
        }
        else if (which == 3 ){
          raw_id = R.raw.david;
        }
        else if (which == 4 ){
          raw_id = R.raw.david_formal;
        }
        else if (which == 5 ){
          raw_id = R.raw.mcdonald;
        }
        String path = ModelHelper.copyRawFileToCache(FullscreenActivity.this, raw_id, selectedItem);
        mAvatarPlayer.loadAvatar("file://" + path);
      }
    });

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }


  public void UIShowChatControl(){
    ArrayList<String> itemList = new ArrayList<>();
    itemList.add("Chat History");

    // 创建AlertDialog.Builder
    AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
    builder.setTitle("Select Model");

    // 设置列表适配器
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(FullscreenActivity.this, android.R.layout.simple_list_item_1, itemList);
    builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 处理选项点击事件
        String selectedItem = itemList.get(which);
        int raw_id = 0;
        if (which == 0 ){
          raw_id = R.raw.james;
        }
        else if (which == 1 ){
          raw_id = R.raw.james2;
        }
        else if (which == 2 ){
          raw_id = R.raw.engineer;
        }
        else if (which == 3 ){
          raw_id = R.raw.david;
        }
        else if (which == 4 ){
          raw_id = R.raw.david_formal;
        }
        else if (which == 5 ){
          raw_id = R.raw.mcdonald;
        }
        String path = ModelHelper.copyRawFileToCache(FullscreenActivity.this, raw_id, selectedItem);
        mAvatarPlayer.loadAvatar("file://" + path);
      }
    });

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  public void UIShowBackgroundSelection() {

    ArrayList<Integer> itemDrawableList = new ArrayList<>();
    itemDrawableList.add(R.drawable.back1);
    itemDrawableList.add(R.drawable.back2);
    itemDrawableList.add(R.drawable.back3);
    itemDrawableList.add(R.drawable.back4);
    itemDrawableList.add(R.drawable.airport);
    itemDrawableList.add(R.drawable.clinic);
    itemDrawableList.add(R.drawable.mcdonald);
    itemDrawableList.add(R.drawable.paris_airport);

    ArrayList<String> itemList = new ArrayList<>();
    itemList.add(getResources().getResourceName(R.drawable.back1));
    itemList.add(getResources().getResourceName(R.drawable.back2));
    itemList.add(getResources().getResourceName(R.drawable.back3));
    itemList.add(getResources().getResourceName(R.drawable.back4));
    itemList.add(getResources().getResourceName(R.drawable.airport));
    itemList.add(getResources().getResourceName(R.drawable.clinic));
    itemList.add(getResources().getResourceName(R.drawable.mcdonald));
    itemList.add(getResources().getResourceName(R.drawable.paris_airport));

    // 创建AlertDialog.Builder
    AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
    builder.setTitle("Select Background");

    // 设置列表适配器
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(FullscreenActivity.this, android.R.layout.simple_list_item_1, itemList);
    builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 处理选项点击事件
        int raw_id = itemDrawableList.get(which);
        ivBackground.setImageResource(raw_id);
      }
    });

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Environment.isExternalStorageManager()) {
          mManageExternalStorageIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
          mStartForResult.launch(mManageExternalStorageIntent);
        }

        String path = ModelHelper.copyRawFileToCache(this, R.raw.james, "james.glb");
        Log.e(TAG, "get cache path:" + path) ;
        Common.act = this;
        setContentView(R.layout.activity_fullscreen);
        ttsHelper = new TtsHelper(this);
        ttsHelper.cb = this;


        FrameLayout avatarLayout = findViewById(R.id.AvatarLayout);
      mAvatarPlayer = new AvatarPlayer(this, avatarLayout, this);
      ttsHelper.player = mAvatarPlayer;

        //mAvatarPlayer.loadAvatar("https://models.readyplayer.me/656ee050869b42cd909818a8.glb");

    ModelHelper.getGlb(this);
    mAvatarPlayer.loadAvatar("file://" + path);

      pbLoading = findViewById(R.id.pbLoading);
       btnSubmit  = findViewById(R.id.btnSubmit);
       btnStop = findViewById(R.id.btnStop);
       ivBackground = findViewById(R.id.ivBackground);
    btnMore = findViewById(R.id.btnMore) ;
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

    etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
          // 在這裡執行您的操作
          if ( btnSubmit.isEnabled()) {
            btnSubmit.callOnClick();
          }
          return true;
        }
        return false;
      }
    });

      btnSubmit.setEnabled(false);

      btnNewSession = findViewById(R.id.btnNewSession);
    btnNewSession.setOnClickListener((v)->{
      LlamaHelper.shared.cleanTalk();
      tvResponse.setText("");
    });

    btnChatControl = findViewById(R.id.btnChatControl);
    btnChatControl.setOnClickListener((v)->{
      //display chat message
      String history = LlamaRecord.toPromptPHI3(LlamaHelper.shared.talkHistory);
       AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
        builder.setTitle("History");
        builder.setMessage(history) ;
        builder.setPositiveButton("OK", (d, i)->{
          d.dismiss();
        });
        builder.create().show();
    });

//      btnRag = findViewById(R.id.btnRag) ;
//      btnRag.setOnClickListener((v)->{
//        AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
//        builder.setTitle("RAG Prompt");
//        builder.setMessage(lastRagContent) ;
//        builder.setPositiveButton("OK", (d, i)->{
//          d.dismiss();
//        });
//        builder.create().show();
//      });

      btnMore.setOnClickListener( onMorePress );

         new Thread(){
           @Override
           public void run() {
             super.run();
             if (LlamaHelper.shared == null ) {
               LlamaHelper.init(FullscreenActivity.this);
               Log.e(TAG, "Module Init Success");
             }
             mHandler.post(()->{
               btnSubmit.setEnabled(true);
             });
           }
         }.start();
    setupTTSButton() ;

    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("token")) {
          String text = intent.getStringExtra("token");
          bufferMessage += text ;
          responseMessage += text ;

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

  private void test() {
    File f = new File("/sdcard/Download/james.glb");
    Log.e(TAG, "@@ " + f.getAbsolutePath() + " f exist:" + f.exists());
  }

  public void processSplit(){

    String chars = ".,\n?!():";
    boolean hasDot = false;
    for ( char c: chars.toCharArray()){
      hasDot = bufferMessage.contains(""+c);
      if (hasDot){
        break ;
      }
    }
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
        try {
          ttsHelper.convertTextToSpeechAndSaveToFile(msg);
        }
        catch (Exception ex){
          Log.e(TAG, "process fail", ex);
        }
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
      pbLoading.setVisibility(View.VISIBLE);
    }
    else if (state == State.FREE){
      btnSubmit.setVisibility(View.VISIBLE);
      btnStop.setVisibility(View.GONE);
      pbLoading.setVisibility(View.GONE);
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


      String question = lines[0] ;
      if (isForceLanguage){
        try {
          question += ", Please answer in " + ttsHelper.textToSpeech.getLanguage().getDisplayName();
        }
        catch ( Exception ex){
          Log.e(TAG, "Can't get cur lang") ;
        }
      }
      if (RagApi.RagEnable){
        Log.e(TAG, "Combine result=" + Arrays.toString(lines));
        ret = LlamaHelper.shared.talk(question, lines[1]);
      }
      else {
        //ret = LlamaHelper.shared.talk(question);
        ret = LlamaHelper.shared.talkContinue(question);
      }

      try {
        Thread.sleep(200) ;
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
      super.onPostExecute(unused);
      Log.e(TAG, "Task End, FREE");
      toggleState(State.FREE);
      isProcessing = false ;
      if ( ret == true){
        etInput.getText().clear();
      }

      // talk the last
      processTTSAsync(bufferMessage);
      bufferMessage = "" ;
      LlamaHelper.shared.talkHistory.add(new LlamaRecord(LlamaRecord.ASSISTANT, responseMessage));
      responseMessage = "";
    }
  }

  @Override
  public void onDone(String path) {
    int val = (int) (Math.random()*6);
    Log.e(TAG, "On Wav callback! wav=" + path) ;
    try {
      mAvatarPlayer.speak("file://" + path, EmotionType.happy, true);
    }
    catch (Exception ex){
      Log.e(TAG, "Speak got exception", ex) ;
    }
  }

  @Override
  public void onFail(String message) {
    Log.e(TAG, "cb onFail:" + message) ;
    mHandler.post(()->{
      Toast.makeText(FullscreenActivity.this, message, Toast.LENGTH_SHORT).show();
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    try {
      ttsHelper.shutdown();
    }
    catch (Exception ex){
      Log.e(TAG, "Shutdown fail") ;
    }
  }
}
