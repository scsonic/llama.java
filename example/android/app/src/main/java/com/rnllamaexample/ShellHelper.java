package com.rnllamaexample;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellHelper {
  static public String TAG = "ShellHelper";

  public static String executeCommand(String command) {
    StringBuilder output = new StringBuilder();


    try {
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        // 命令執行失敗，打印錯誤信息
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
          output.append(errorLine).append("\n");
        }
      }

    } catch (IOException | InterruptedException e) {
      Log.e(TAG, "Run Command Fail", e);
    }

    return output.toString();
  }

}
