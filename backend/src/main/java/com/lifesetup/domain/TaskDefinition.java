package com.lifesetup.domain;
import java.util.List;
public record TaskDefinition(String id, String title, String summary, List<String> dependencies,
 List<String> requirements, OfficialSource officialSource, String nextAction, String note,
 String category,String applicability,int priority,String completionLabel) {
 public TaskDefinition(String id,String title,String summary,List<String> dependencies,List<String> requirements,OfficialSource officialSource,String nextAction,String note) {
  this(id,title,summary,dependencies,requirements,officialSource,nextAction,note,"GENERAL","ALWAYS",0,"Mark this step done");
 }
 public TaskDefinition { dependencies=List.copyOf(dependencies); requirements=List.copyOf(requirements); }
}
