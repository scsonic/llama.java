package com.rnllama;

//import com.facebook.react.bridge.Arguments;
//import com.facebook.react.bridge.WritableArray;
//import com.facebook.react.bridge.WritableMap;
//import com.facebook.react.bridge.ReadableMap;
//import com.facebook.react.bridge.ReadableArray;
// import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.Build;
import android.content.res.AssetManager;

import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LlamaContext {
  public static final String NAME = "RNLlamaContext";
  private static final String NOTIFI_PART = "com.rnllama.send";

  private int id;
  // private ReactApplicationContext reactContext;
  // static public ReactApplicationContext sharedContext = null ;
  static public Context ctx ;// ANdroid default ctx
  private long context;
  private int jobId = -1;
  static public String BROADCAST_NAME = "LlamaJavaToken" ;



  public LlamaContext(int id, Bundle params) {
    if (LlamaContext.isArm64V8a() == false && LlamaContext.isX86_64() == false) {
      throw new IllegalStateException("Only 64-bit architectures are supported");
    }
    if (!params.containsKey("model")) {
      throw new IllegalArgumentException("Missing required parameter: model");
    }

    this.id = id;
    this.context = initContext(
      // String model,
      params.getString("model"),
      // boolean embedding,
      params.containsKey("embedding") ? params.getBoolean("embedding") : false,
      // int n_ctx,
      params.containsKey("n_ctx") ? params.getInt("n_ctx") : 2048,
      // int n_batch,
      params.containsKey("n_batch") ? params.getInt("n_batch") : 256,
      // int n_threads,
      params.containsKey("n_threads") ? params.getInt("n_threads") : 4,
      // int n_gpu_layers, // TODO: Support this
      params.containsKey("n_gpu_layers") ? params.getInt("n_gpu_layers") : 0,
      // boolean use_mlock,
      params.containsKey("use_mlock") ? params.getBoolean("use_mlock") : true,
      // boolean use_mmap,
      params.containsKey("use_mmap") ? params.getBoolean("use_mmap") : true,
      // String lora,
      params.containsKey("lora") ? params.getString("lora") : "",
      // float lora_scaled,
      params.containsKey("lora_scaled") ? (float) params.getDouble("lora_scaled") : 1.0f,
      // String lora_base,
      params.containsKey("lora_base") ? params.getString("lora_base") : "",
      // float rope_freq_base,
      params.containsKey("rope_freq_base") ? (float) params.getDouble("rope_freq_base") : 0.0f,
      // float rope_freq_scale
      params.containsKey("rope_freq_scale") ? (float) params.getDouble("rope_freq_scale") : 0.0f
    );
  }

  public long getContext() {
    return context;
  }

  private void emitPartialCompletion(Bundle tokenResult) {
    Bundle event = new Bundle() ; // Arguments.createMap();
    event.putInt("contextId", LlamaContext.this.id);
    event.putBundle("tokenResult", tokenResult);
  }

  private static class PartialCompletionCallback {
    LlamaContext context;
    boolean emitNeeded;

    public PartialCompletionCallback(LlamaContext context, boolean emitNeeded) {
      this.context = context;
      this.emitNeeded = emitNeeded;
    }

    void onPartialCompletion(Bundle tokenResult) {
      Log.d("@@", "@@ TokenResult:" + tokenResult.toString());

      notifyPartString(tokenResult.getString("token"));
    }
  }

  public Bundle loadSession(String path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("File path is empty");
    }
    File file = new File(path);
    if (!file.exists()) {
      throw new IllegalArgumentException("File does not exist: " + path);
    }
    Bundle result = loadSession(this.context, path);
    if (result.containsKey("error")) {
      throw new IllegalStateException(result.getString("error"));
    }
    return result;
  }

  public int saveSession(String path, int size) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("File path is empty");
    }
    return saveSession(this.context, path, size);
  }

  public Bundle completion(Bundle params) {
    if (!params.containsKey("prompt")) {
      throw new IllegalArgumentException("Missing required parameter: prompt");
    }

    double[][] logit_bias = new double[0][0];
//    if (params.containsKey("logit_bias")) {
//      //ArrayList<Double[]> logitBiasArrayList = params.getParcelableArrayList("logit_bias");
//      ArrayList<ArrayList<Double>> logitBiasArrayList = params.getParcelableArrayList("logit_bias");
//      logit_bias = new double[logitBiasArrayList.size()][];
//      for (int i = 0; i < logitBiasArrayList.size(); i++) {
//        Double[] logitBiasRow = logitBiasArrayList.get(i);
//        logit_bias[i] = new double[logitBiasRow.length];
//        for (int j = 0; j < logitBiasRow.length; j++) {
//          logit_bias[i][j] = logitBiasRow[j];
//        }
//      }
//    }

    String[] stops = {"</s>", "<|end|>"};

    return doCompletion(
      this.context,
      // String prompt,
      params.getString("prompt"),
      // String grammar,
      params.containsKey("grammar") ? params.getString("grammar") : "",
      // float temperature,
      params.containsKey("temperature") ? params.getFloat("temperature") : 0.7f,
      // int n_threads,
      params.containsKey("n_threads") ? params.getInt("n_threads") : 4,
      // int n_predict,
      params.containsKey("n_predict") ? params.getInt("n_predict") : 128,
      // int n_probs,
      params.containsKey("n_probs") ? params.getInt("n_probs") : 0,
      // int penalty_last_n,
      params.containsKey("penalty_last_n") ? params.getInt("penalty_last_n") : 64,
      // float penalty_repeat,
      params.containsKey("penalty_repeat") ? params.getFloat("penalty_repeat") : 1.30f,
      // float penalty_freq,
      params.containsKey("penalty_freq") ? params.getFloat("penalty_freq") : 0.00f,
      // float penalty_present,
      params.containsKey("penalty_present") ? params.getFloat("penalty_present") : 0.00f,
      // float mirostat,
      params.containsKey("mirostat") ? params.getFloat("mirostat") : 0.00f,
      // float mirostat_tau,
      params.containsKey("mirostat_tau") ? params.getFloat("mirostat_tau") : 5.00f,
      // float mirostat_eta,
      params.containsKey("mirostat_eta") ? params.getFloat("mirostat_eta") : 0.10f,
      // boolean penalize_nl,
      params.containsKey("penalize_nl") ? params.getBoolean("penalize_nl") : true,
      // int top_k,
      params.containsKey("top_k") ? params.getInt("top_k") : 40,
      // float top_p,
      params.containsKey("top_p") ? params.getFloat("top_p") : 0.80f,
      // float min_p,
      params.containsKey("min_p") ? params.getFloat("min_p") : 0.05f,
      // float tfs_z,
      params.containsKey("tfs_z") ? params.getFloat("tfs_z") : 1.00f,
      // float typical_p,
      params.containsKey("typical_p") ? params.getFloat("typical_p") : 1.00f,
      // int seed,
      params.containsKey("seed") ? params.getInt("seed") : -1,
      // String[] stop,
      params.containsKey("stop") ? params.getStringArray("stop") : stops,
      // boolean ignore_eos,
      params.containsKey("ignore_eos") ? params.getBoolean("ignore_eos") : false,
      // double[][] logit_bias,
      logit_bias,
      // PartialCompletionCallback partial_completion_callback
      new PartialCompletionCallback(
        this,
        params.containsKey("emit_partial_completion") ? params.getBoolean("emit_partial_completion") : true
      )
    );
  }

  public static void notifyPartString(String part){
    Intent intent = new Intent(NOTIFI_PART);    //action: "msg"
    intent.putExtra("token", part);

    if (ctx != null){
      //Log.d("@@", "action has send:" + part);
      ctx.sendBroadcast(intent);
    }
  }

  public void stopCompletion() {
    stopCompletion(this.context);
  }

  public boolean isPredicting() {
    return isPredicting(this.context);
  }

  public Bundle tokenize(String text) {
    Bundle result = new Bundle() ; // Arguments.createMap();
    //result.putBundle("tokens", tokenize(this.context, text));
    return result;
  }

  public String detokenize(ArrayList<Double> tokens) {
    int[] toks = new int[tokens.size()];
    for (int i = 0; i < tokens.size(); i++) {
      toks[i] = (int) ((double)tokens.get(i));
    }
    return detokenize(this.context, toks);
  }

  public Bundle embedding(String text) {
    if (isEmbeddingEnabled(this.context) == false) {
      throw new IllegalStateException("Embedding is not enabled");
    }
    Bundle result =  new Bundle() ; //.createMap();
    //result.putBundle("embedding", embedding(this.context, text));
    return result;
  }

  public String bench(int pp, int tg, int pl, int nr) {
    return bench(this.context, pp, tg, pl, nr);
  }

  public void release() {
    freeContext(context);
  }

  static {
    Log.d(NAME, "Primary ABI: " + Build.SUPPORTED_ABIS[0]);
    if (LlamaContext.isArm64V8a()) {
      boolean loadV8fp16 = false;
      if (LlamaContext.isArm64V8a()) {
        // ARMv8.2a needs runtime detection support
        String cpuInfo = LlamaContext.cpuInfo();
        if (cpuInfo != null) {
          Log.e(NAME, "CPU info: " + cpuInfo);
          if (cpuInfo.contains("fphp")) {
            Log.e(NAME, "CPU supports fp16 arithmetic");
            loadV8fp16 = true;
          }
        }
      }

      if (loadV8fp16) {
        Log.d(NAME, "Loading librnllama_v8fp16_va.so");
        System.loadLibrary("rnllama_v8fp16_va");
      } else {
        Log.d(NAME, "Loading librnllama.so");
        System.loadLibrary("rnllama");
      }
    } else if (LlamaContext.isX86_64()) {
      Log.d(NAME, "Loading librnllama.so");
      System.loadLibrary("rnllama");
    }
  }

  private static boolean isArm64V8a() {
    return Build.SUPPORTED_ABIS[0].equals("arm64-v8a");
  }

  private static boolean isX86_64() {
    return Build.SUPPORTED_ABIS[0].equals("x86_64");
  }

  private static String cpuInfo() {
    File file = new File("/proc/cpuinfo");
    StringBuilder stringBuilder = new StringBuilder();
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
          stringBuilder.append(line);
      }
      bufferedReader.close();
      return stringBuilder.toString();
    } catch (IOException e) {
      Log.w(NAME, "Couldn't read /proc/cpuinfo", e);
      return null;
    }
  }

  protected static native long initContext(
    String model,
    boolean embedding,
    int n_ctx,
    int n_batch,
    int n_threads,
    int n_gpu_layers, // TODO: Support this
    boolean use_mlock,
    boolean use_mmap,
    String lora,
    float lora_scaled,
    String lora_base,
    float rope_freq_base,
    float rope_freq_scale
  );
  protected static native Bundle loadSession(
    long contextPtr,
    String path
  );
  protected static native int saveSession(
    long contextPtr,
    String path,
    int size
  );
  protected static native Bundle doCompletion(
    long context_ptr,
    String prompt,
    String grammar,
    float temperature,
    int n_threads,
    int n_predict,
    int n_probs,
    int penalty_last_n,
    float penalty_repeat,
    float penalty_freq,
    float penalty_present,
    float mirostat,
    float mirostat_tau,
    float mirostat_eta,
    boolean penalize_nl,
    int top_k,
    float top_p,
    float min_p,
    float tfs_z,
    float typical_p,
    int seed,
    String[] stop,
    boolean ignore_eos,
    double[][] logit_bias,
    PartialCompletionCallback partial_completion_callback
  );
  protected static native void stopCompletion(long contextPtr);
  protected static native boolean isPredicting(long contextPtr);
  //protected static native WritableArray tokenize(long contextPtr, String text);
  protected static native String detokenize(long contextPtr, int[] tokens);
  protected static native boolean isEmbeddingEnabled(long contextPtr);
  //protected static native WritableArray embedding(long contextPtr, String text);
  protected static native String bench(long contextPtr, int pp, int tg, int pl, int nr);
  protected static native void freeContext(long contextPtr);
}
