package com.rnllamaexample;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.rnllama.LlamaContext;

import java.nio.charset.StandardCharsets;

public class LlamaHelper {
  static public String TAG = "LlamaHelper";
  static public LlamaHelper shared;

  LlamaContext lctx ;
  static public void init() {
    if (shared == null){
      shared = new LlamaHelper();
      shared.init() ;
    }
  }

  public LlamaHelper(){
    if (lctx == null) {
      WritableMap params = Arguments.createMap();
      params.putString("model", "/data/user/0/com.rnllamaexample/cache/models/1000001958.gguf");
      params.putString("model", "/data/user/0/com.rnllamaexample/cache/models/msf%3A1000001958.gguf");

      params.putString("model", "/sdcard/Download/rocket-3b.Q2_K.gguf");


      lctx = new LlamaContext(54321, null, params);
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
    WritableMap data = Arguments.createMap();
    Log.e(TAG, "talk:" + line);
    data.putString("prompt", toUtf8(line));
    lctx.completion(data);
    return true ;
  }


}
