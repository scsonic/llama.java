package com.rnllamaexample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VoskActivity extends Activity implements RecognitionListener {

  static public String TAG = "VoskView";

  static private final int STATE_START = 0;
  static private final int STATE_READY = 1;
  static private final int STATE_DONE = 2;
  static private final int STATE_FILE = 3;
  static private final int STATE_MIC = 4;

  /* Used to handle permission request */
  private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

  private Model model;
  private SpeechService speechService;
  private SpeechStreamService speechStreamService;
  private TextView resultView;

  Spinner spInput ;
  Spinner spOutput ;


  String audioText = "" ;
  String audioBuffer = "" ;

  String llmText = "" ;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_vosk);

    // Setup layout
    resultView = findViewById(R.id.result_text);
    setUiState(STATE_START);

    findViewById(R.id.recognize_mic).setOnClickListener((v) -> {
      audioBuffer = "" ;
      audioText = "" ;
      llmText = "" ;
      updateResult();
      recognizeMicrophone();

      });
    ((ToggleButton) findViewById(R.id.pause)).setOnCheckedChangeListener((view, isChecked) -> pause(isChecked));


    spInput = findViewById(R.id.spInput);
    spInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.e(TAG, "Change to lang:" + spInput.getSelectedItem().toString()) ;
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    spOutput = findViewById(R.id.spOutput);
    spOutput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.e(TAG, "Convert to lang:" + spInput.getSelectedItem().toString()) ;
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    LibVosk.setLogLevel(LogLevel.INFO);

    // Check if user has given permission to record audio, init the model after permission is granted
    int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
    } else {
      initModel();
    }
  }

  private static String readLine(InputStream is) throws IOException {
    return new BufferedReader(new InputStreamReader(is)).readLine();
  }
  private void initModel() {

    String sourcePath = "model-cn";
    String targetPath = "test" ;

    try {
      AssetManager assetManager = this.getAssets();
      File externalFilesDir = this.getExternalFilesDir(null);

      File targetDir = new File(externalFilesDir, targetPath);
      String resultPath = new File(targetDir, sourcePath).getAbsolutePath();
      String sourceUUID = readLine(assetManager.open(sourcePath + "/uuid"));
      Log.e(TAG, "UUID=" + sourceUUID);
    }catch (Exception ex){
      Log.e(TAG, ex.getMessage(), ex) ;
    }

    StorageService.unpack(this, sourcePath, targetPath,
      (model) -> {
        this.model = model;
        setUiState(STATE_READY);
      },
      (exception) -> {
      setErrorState("Failed to unpack the model" + exception.getMessage());
      Log.e("@@", exception.getMessage(), exception);
      });
  }


  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        initModel();
      } else {
        finish();
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (speechService != null) {
      speechService.stop();
      speechService.shutdown();
    }

    if (speechStreamService != null) {
      speechStreamService.stop();
    }
  }


  public void updateResult() {
    resultView.setText(audioText + audioBuffer) ;
  }
  @Override
  public void onResult(String hypothesis) {
    Log.e(TAG, "onResult=" + hypothesis);
    String text = Common.getResult(hypothesis);
    audioText += text;
    audioBuffer = "" ;
    updateResult() ;
  }

  @Override
  public void onFinalResult(String hypothesis) {
    Log.e(TAG, "onFinalResult=" + hypothesis);
    onResult(hypothesis);

    setUiState(STATE_DONE);
    if (speechStreamService != null) {
      speechStreamService = null;
    }
  }

  @Override
  public void onPartialResult(String hypothesis) {
    Log.e(TAG, "onPartialResult=" + hypothesis);

    String part = Common.getResult(hypothesis);
    //resultView.append(hypothesis + "\n");
    audioBuffer = part ;
    updateResult();
  }

  @Override
  public void onError(Exception e) {
    setErrorState(e.getMessage());
  }

  @Override
  public void onTimeout() {
    setUiState(STATE_DONE);
  }

  private void setUiState(int state) {
    switch (state) {
      case STATE_START:
        resultView.setText(R.string.preparing);
        resultView.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.recognize_mic).setEnabled(false);
        findViewById(R.id.pause).setEnabled((false));
        break;
      case STATE_READY:
        resultView.setText(R.string.ready);
        ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
        findViewById(R.id.recognize_mic).setEnabled(true);
        findViewById(R.id.pause).setEnabled((false));
        break;
      case STATE_DONE:
        ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
        findViewById(R.id.recognize_mic).setEnabled(true);
        findViewById(R.id.pause).setEnabled((false));
        ((ToggleButton) findViewById(R.id.pause)).setChecked(false);
        break;
      case STATE_FILE:
        resultView.setText(getString(R.string.starting));
        findViewById(R.id.recognize_mic).setEnabled(false);
        findViewById(R.id.pause).setEnabled((false));
        break;
      case STATE_MIC:
        ((Button) findViewById(R.id.recognize_mic)).setText(R.string.stop_microphone);
        resultView.setText(getString(R.string.say_something));
        findViewById(R.id.recognize_mic).setEnabled(true);
        findViewById(R.id.pause).setEnabled((true));
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + state);
    }
  }

  private void setErrorState(String message) {
    resultView.setText(message);
    ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
    findViewById(R.id.recognize_mic).setEnabled(false);
  }

  private void recognizeMicrophone() {
    if (speechService != null) {
      setUiState(STATE_DONE);
      speechService.stop();
      speechService = null;
    } else {
      setUiState(STATE_MIC);
      try {
        Recognizer rec = new Recognizer(model, 16000.0f);
        speechService = new SpeechService(rec, 16000.0f);
        speechService.startListening(this);
      } catch (IOException e) {
        setErrorState(e.getMessage());
      }
    }
  }


  private void pause(boolean checked) {
    if (speechService != null) {
      speechService.setPause(checked);
    }
  }

}
