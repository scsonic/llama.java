package com.rnllamaexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

  public ItemAdapter(Context context, List<Item> items) {
    super(context, 0, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // 獲取當前項目的數據
    Item item = getItem(position);

    // 檢查是否已有可重用的視圖，沒有則加載新的視圖
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
    }

    // 獲取佈局中的 TextView
    TextView titleTextView = convertView.findViewById(R.id.title);
    TextView subtitleTextView = convertView.findViewById(R.id.subtitle);

    // 將資料設置到視圖中
    titleTextView.setText(item.getTitle());
    subtitleTextView.setText(item.getSubtitle());

    // 返回已完成設置的視圖
    return convertView;
  }
}
