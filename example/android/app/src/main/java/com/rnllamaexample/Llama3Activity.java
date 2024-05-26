package com.rnllamaexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Llama3Activity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    LlamaHelper.model_file_name = "Meta-Llama-3-8B-Instruct.Q2_K.gguf";
    Log.e("@@", "has Set Model File Name" + LlamaHelper.model_file_name) ;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dummy);
    Intent intent = new Intent(this, FullscreenActivity.class);
    startActivity(intent);
  }
}
