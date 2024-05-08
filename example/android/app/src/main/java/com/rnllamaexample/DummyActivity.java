package com.rnllamaexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DummyActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    LlamaHelper.model_file_name = "zephyr-7b-alpha.Q2_K.gguf" ;
    Log.e("@@", "has Set Model File Name" + LlamaHelper.model_file_name) ;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dummy);
    Intent intent = new Intent(this, FullscreenActivity.class);
    startActivity(intent);
  }
}
