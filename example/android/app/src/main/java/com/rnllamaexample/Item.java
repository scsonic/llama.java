package com.rnllamaexample;

public class Item {
  private String title;
  private String subtitle;

  public Item(String title, String subtitle) {
    this.title = title;
    this.subtitle = subtitle;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }
}
