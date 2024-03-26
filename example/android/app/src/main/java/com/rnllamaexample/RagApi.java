package com.rnllamaexample;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RagApi {

  // 定义回调接口
  public interface RagCallback {
    void onSuccess(String response);
    void onError(String errorMessage);
  }

  // 定义异步任务类来执行网络请求
  private static class RagTask extends AsyncTask<String, Void, String> {

    private RagCallback callback;

    static public String url = "172.20.10.3:10080";

    public RagTask(RagCallback callback) {
      this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
      String query = params[0];
      try {
        // 构建 API 请求 URL
        String apiUrl = "http://" + url + "/query?query=" + query;
        URL url = new URL(apiUrl);

        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 读取 API 响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        reader.close();

        return response.toString();
      } catch (IOException e) {
        Log.e("ApiCaller", "Error while calling API", e);
        return null;
      }
    }

    @Override
    protected void onPostExecute(String response) {
      if (response != null) {
        callback.onSuccess(response);
      } else {
        callback.onError("Failed to get API response");
      }
    }
  }

  // 函数用于调用 API
  public static void callApi(String query, RagCallback callback) {
    // 创建并执行异步任务
    new RagTask(callback).execute(query);
  }
}
