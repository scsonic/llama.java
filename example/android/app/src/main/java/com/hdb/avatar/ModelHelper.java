package com.hdb.avatar;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModelHelper {

  static public String TAG = "ModelHelper";


  public static List<String> getGlb(Context context) {
    List<String> glbFiles = new ArrayList<>();

    // 檢查外部存儲是否可用
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {

      // 取得Download目錄
      File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

      // 檢查目錄是否存在
      if (downloadDir.exists() && downloadDir.isDirectory()) {

        // 取得目錄下的所有檔案
        File[] files = downloadDir.listFiles();

        // 遍歷檔案列表
        if (files != null) {
          for (File file : files) {
            // 檢查檔案是否是.glb檔案
            if (file.isFile() && file.getName().toLowerCase().endsWith(".glb")) {
              glbFiles.add(file.getName());
              Log.e(TAG, "Get GLB:" + file.getName());
            }
          }
        }
      }
    }

    return glbFiles;
  }
}
