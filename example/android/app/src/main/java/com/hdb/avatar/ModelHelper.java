package com.hdb.avatar;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.rnllamaexample.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

  public static String copyRawFileToCache(Context context, int raw_id, String rawFileName) {
    try {
      // 打开 raw 资源文件的输入流
      Resources resources = context.getResources();
      InputStream inputStream = resources.openRawResource(raw_id);

      // 创建缓存目录
      //File cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File cacheDir = context.getCacheDir();
      File outputFile = new File(cacheDir, rawFileName);

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
          outputStream.write(buffer, 0, length);
        }
      outputStream.close();


      File jsonFile = new File(cacheDir, rawFileName.replace(".glb", ".json"));

      if (jsonFile.getName().contains(".wav")){
        // do not output wav
      }
      else {
        FileOutputStream json_outputStream = new FileOutputStream(jsonFile);
        String json_content = "{\"bodyType\":\"fullbody\",\"outfitGender\":\"feminine\",\"outfitVersion\":2,\"skinTone\":\"#ffd4b5\",\"createdAt\":\"2023-12-05T08:34:51.651Z\",\"updatedAt\":\"2023-12-18T11:48:24.499Z\"}";
        byte[] jb = json_content.getBytes();
        json_outputStream.write(jb, 0, jb.length);
        json_outputStream.close();
      }

        return outputFile.getAbsolutePath();
    } catch (IOException e) {
      Log.e(TAG, "Copy file fail", e);
    }
    return null ;
  }
}
