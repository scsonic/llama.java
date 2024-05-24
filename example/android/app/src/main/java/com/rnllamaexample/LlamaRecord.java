package com.rnllamaexample;

import java.util.ArrayList;

public class LlamaRecord {

  static public String USER = "user" ;
  static public String SYSTEM = "system" ;
  static public String ASSISTANT = "assistant" ;

  String role = "" ;
  String content = "" ;

  LlamaRecord(String role, String content) {
    this.role = role ;
    this.content = content ;
  }


  static public String toPromptPHI3(ArrayList<LlamaRecord> recordList){
    String prompt = "" ;
    String end = "<|end|>\n" ;
    for (LlamaRecord record: recordList){
      if (record.role.equalsIgnoreCase(USER)){
        prompt += "<|user|>\n" + record.content + end ;
      }
      else if (record.role.equalsIgnoreCase(SYSTEM) && record.content.length() > 0){
        prompt += "<|system|>\n" + record.content + end ;
      }
      else if (record.role.equalsIgnoreCase(ASSISTANT)){
        prompt += "<|assistant|>\n" + record.content + end ;
      }
      else {
        //prompt += "<|error:" + record.role + "|>\n" + record.content + end ;
      }
    }
    return prompt ;
  }

  public static String toPromptChatML(ArrayList<LlamaRecord> recordList) {
    String prompt = "" ;
    String end = "</s>\n" ;
    for (LlamaRecord record: recordList){
      if (record.role.equalsIgnoreCase(USER) || record.role.equalsIgnoreCase(SYSTEM)){
        prompt += "<|user|>\n" + record.content + end ;
      }
      else if (record.role.equalsIgnoreCase(ASSISTANT)){
        prompt += "<|assistant|>\n" + record.content + end ;
      }
      else {
        prompt += "<|error:" + record.role + "|>\n" + record.content + end ;
      }
    }
    return prompt ;
  }

  static public String toPromptLlama3(ArrayList<LlamaRecord> recordList){

//
//    <|start_header_id|>system<|end_header_id|>
//    {system_prompt}<|eot_id|>
//
//    <|start_header_id|>user<|end_header_id|>
//    {prompt}<|eot_id|>
//
//    <|start_header_id|>assistant<|end_header_id|>

    String prompt = "<|begin_of_text|>" ;
    String end = "<|eot_id|>\n" ;
    for (LlamaRecord record: recordList){
      if (record.role.equalsIgnoreCase(USER)){
        prompt += "<|start_header_id|>user<|end_header_id|>\n\n" + record.content + end ;
      }
      else if (record.role.equalsIgnoreCase(SYSTEM) && record.content.length() > 0){
        prompt += "<|start_header_id|>system<|end_header_id|>\n\n" + record.content + end ;
      }
      else if (record.role.equalsIgnoreCase(ASSISTANT)){
        prompt += "<|start_header_id|>assistant<|end_header_id|>\n\n" + record.content + end ;
      }
      else {
        //prompt += "<|error:" + record.role + "|>\n" + record.content + end ;
      }
    }
    return prompt ;
  }
}

