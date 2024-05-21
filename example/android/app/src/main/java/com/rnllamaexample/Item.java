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

  public void appendSubTitle(String str){
    if (subtitle == null){
      subtitle = "" ;
    }
    subtitle += str ;
  }

  public String getSubtitle() {
    return subtitle;
  }
}
