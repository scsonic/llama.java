package com.rnllamaexample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
  static public boolean writeSharePerf(String tag, String data)
  {
    try {
      SharedPreferences pref = act.getSharedPreferences(tag, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = pref.edit() ;
      editor.putString(tag, data ) ;
      editor.commit() ;
      editor.apply() ;
      return true;
    }
    catch (Exception ex)
    {
      return false ;
    }
  }

  /**
   * 備份root路徑
   */
  static public String readSharePerf(String tag)
  {
    SharedPreferences pref = act.getSharedPreferences(tag, Context.MODE_PRIVATE);
    String data = pref.getString(tag, "") ;

    if ( data.equalsIgnoreCase(""))
      return null ;
    else
      return data ;
  }

}
