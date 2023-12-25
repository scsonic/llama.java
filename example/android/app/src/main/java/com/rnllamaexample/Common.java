package com.rnllamaexample;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;

public class Common {
  static public Activity act ;
  static public void Toast(String message){
    if ( act != null){
      act.runOnUiThread(()->{
        Toast.makeText(act, message, Toast.LENGTH_SHORT).show();
      });

    }

  }
}
