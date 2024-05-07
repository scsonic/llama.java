package com.rnllamaexample;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

//import com.facebook.react.bridge.Arguments;
//import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.rnllama.LlamaContext;
//import com.rnllama.LlamaContext;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class LlamaHelper {
  static public String TAG = "LlamaHelper";
  static public LlamaHelper shared;

  String model_file_name = "" ;

  LlamaContext lctx ;
  static public void init(Context c) {
    LlamaContext.ctx = c;
    if (shared == null){
      shared = new LlamaHelper();
    }
  }

  public LlamaHelper(){
    if (lctx == null) {
      Bundle params = new Bundle() ; // Arguments.createMap();

      //  "rocket-3b.Q4_0.gguf", "rocket-3b.Q2_K.gguf",
      String [] gguf_list = new String[] {"Phi-3-mini-4k-instruct-q4.gguf", "phi-2-super.Q4_K_M.gguf","zephyr-7b-beta.Q4_0.gguf","zephyr-7b-alpha.Q2_K.gguf"};
      //String [] gguf_list = new String[] {"rocket-3b.Q4_0.gguf", "rocket-3b.Q2_K.gguf", "zephyr-7b-beta.Q4_0.gguf", "zephyr-7b-alpha.Q2_K.gguf",};
      String dir = "/sdcard/Download/" ;

      for (String file : gguf_list){
        String path = dir + file ;
        File f = new File(path);
        if (f.exists()){
          params.putString("model", path);
          model_file_name = file ;
          Log.e(TAG, "Using model:" + path);
          //Common.Toast("Using Model: " + file);
          break;
        }
      }

      lctx = new LlamaContext(54321, params);
      //talk(prompt2);
    }

    if ( lctx != null){
//      WritableMap data = Arguments.createMap();
//      int a = (int) (System.currentTimeMillis()*1234) % 100;
//      int b = (int) (System.currentTimeMillis()*4321) % 100;
//      String p = String.format("%d + %d = ?", a, b);
//
//      Log.e(TAG, p);
//      data.putString("prompt", toUtf8(p));
//      lctx.completion(data);
    }
  }

  public String toUtf8(String input){
    byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public boolean talk(String line){
    if (lctx.isPredicting()){
      Log.e(TAG, "is isPredicting, cancel");
      return false;
    }
    Bundle data = new Bundle() ; // Arguments.createMap();
    line += "";

    String preprompt = "You are an assistance at a DIY store, please help the customers at the best with your knowledge" ;

    String template = "<|user|>" + preprompt + "\n" +
      "</s>\n" +
      "<|user|>\n" +
      line + "</s>\n" +
      "<|assistant|>";

    if (model_file_name.contains("Phi")){
      // change to phi3 format
      template = "<|system|>\n" + preprompt + "<|end|>\n<|user|>\n" + line + " <|end|>\n" +
        "<|assistant|>";
    }


    Log.e(TAG, "talk:" + template);
    data.putString(toUtf8("prompt"), toUtf8(template));
    Log.e(TAG, toUtf8(template));
    lctx.completion(data);

    return true ;
  }


  public boolean talk(String line, String rag){
    if (lctx.isPredicting()){
      Log.e(TAG, "is isPredicting, cancel");
      return false;
    }
    Bundle data = new Bundle() ; // Arguments.createMap();
    line += "";


    String preprompt = RagApi.RAG_PREPROMPT ;
    String template = "<|system|>" + preprompt + rag + "\n" +
      "</s>\n" +
      "<|user|>\n" +
      line + "</s>\n" +
      "<|assistant|>";

    Log.e(TAG, "talk:" + template);
    data.putString(toUtf8("prompt"), toUtf8(template));


    //ReadableArray stopString = Arguments.fromArray(new String[]{"</s>"});
    //data.putArray("stop", stopString);
    lctx.completion(data);

    return true ;
  }

}
