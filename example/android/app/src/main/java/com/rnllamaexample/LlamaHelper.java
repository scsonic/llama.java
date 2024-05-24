package com.rnllamaexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

//import com.facebook.react.bridge.Arguments;
//import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.hdb.avatar.EmotionType;
import com.hdb.avatar.ModelHelper;
import com.rnllama.LlamaContext;
//import com.rnllama.LlamaContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LlamaHelper {
  static public String TAG = "LlamaHelper";
  static public LlamaHelper shared;

  static public String model_file_name = "" ;

  static public String prePrompt = null ;
  static public String KEY_LAST_PROMPT = "KEY_LAST_PROMPT";

  static public String getPrePrompt(){
    if (prePrompt == null){
      prePrompt = Common.readSharePerf(KEY_LAST_PROMPT);
    }
    if (prePrompt == null){
      prePrompt = "You are an assistance at a DIY store, please help the customers at the best with your knowledge" ;
    }
    return prePrompt ;
  }
  ArrayList<LlamaRecord> talkHistory = new ArrayList<>();

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
      String [] gguf_list = new String[] {model_file_name, "Phi-3-mini-4k-instruct-q4.gguf", "phi-2-super.Q4_K_M.gguf","zephyr-7b-beta.Q4_0.gguf","zephyr-7b-alpha.Q2_K.gguf"};
      //String [] gguf_list = new String[] {"rocket-3b.Q4_0.gguf", "rocket-3b.Q2_K.gguf", "zephyr-7b-beta.Q4_0.gguf", "zephyr-7b-alpha.Q2_K.gguf",};
      String dir = "/sdcard/Download/" ;
      for (String file : gguf_list) {
        String path = dir + file;
        File f = new File(path);
        if (f.exists() && f.isFile()) {
          params.putString("model", path);
          model_file_name = file;
          Log.e(TAG, "Using model:" + path);
          //Common.Toast("Using Model: " + file);
          break;
        }
        else {
          Log.e(TAG, "Model file:" + file + " is not exist");
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

    String preprompt = getPrePrompt() ;

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

  public boolean talkContinue(String line){
    if (lctx.isPredicting()){
      Log.e(TAG, "is isPredicting, cancel");
      return false;
    }
    Bundle data = new Bundle() ; // Arguments.createMap();
    LlamaRecord rec = new LlamaRecord(LlamaRecord.USER, line);

    if (talkHistory.size() == 0 || !talkHistory.get(0).role.equalsIgnoreCase(LlamaRecord.SYSTEM)){
      talkHistory.add(0, new LlamaRecord(LlamaRecord.SYSTEM, getPrePrompt()));
    }
    talkHistory.add(rec) ;
    String prompt = "" ;
    if (model_file_name.contains("Phi")){
      prompt = LlamaRecord.toPromptPHI3(talkHistory);
      prompt += "<|" + LlamaRecord.ASSISTANT.toLowerCase() + "|>" ;
      Log.e(TAG, "toPromptPHI3=" + prompt);
    }
    else if (model_file_name.contains("Llama-3")){
      prompt = LlamaRecord.toPromptLlama3(talkHistory);
      prompt += "<|start_header_id|>assistant<|end_header_id|>\n\n";
      Log.e(TAG, "toPromptLlama3=" + prompt);
    }
    else {
      // zephyr
      prompt = LlamaRecord.toPromptChatML(talkHistory);
      prompt += "<|" + LlamaRecord.ASSISTANT.toLowerCase() + "|>" ;
      Log.e(TAG, "toPromptChatML=" + prompt);
    }


    Log.e(TAG, "talk:" + prompt);
    data.putString(toUtf8("prompt"), toUtf8(prompt));
    lctx.completion(data);

    return true ;
  }

  public boolean cleanTalk(){
    talkHistory.clear();
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
    Bundle bundle = lctx.completion(data);
    Log.e("@@", "Will not catch this, Bundle = " + bundle) ;

    if (bundle != null) {
      for (String key : bundle.keySet()) {
        Object value = bundle.get(key);
        Log.e("@@", key + " : " + value.toString());
      }
    }
    return true ;
  }


  static public void UIShowPrePromptSelection(Activity act){


    ArrayList<String> promptList = new ArrayList<>();
    promptList.add("Fill new one");
    promptList.add("You are an assistance at a DIY store, please help the customers at the best with your knowledge");
    promptList.add("you are a cashier in McDonald's, you will continue to serve until he places his order, you can make recommendations while he is making his order. ");

    AlertDialog.Builder builder = new AlertDialog.Builder(act);
    builder.setTitle("Select Prompt(keep in app storage)");

    // 设置列表适配器
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(act, android.R.layout.simple_list_item_1, promptList);
    builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 处理选项点击事件
        String newPrompt = promptList.get(which);
        if (which == 0 ){
          // show edittext
          UIShowPrePromptInput(act);
        }
        else{
          prePrompt = newPrompt;
          Common.writeSharePerf(KEY_LAST_PROMPT, prePrompt);
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
  };

  static public void UIShowPrePromptInput(Activity act){
    AlertDialog.Builder builder = new AlertDialog.Builder(act);
    builder.setTitle("Enter new PrePrompt");

    final EditText editText = new EditText(act);
    editText.setText(getPrePrompt());
    builder.setView(editText);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // 当用户点击“OK”按钮时的处理逻辑
        String inputText = editText.getText().toString();
        prePrompt = inputText;
      }
    });

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    AlertDialog dialog = builder.create();
    dialog.show();
  }
}
