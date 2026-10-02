package com.lifesetup.domain;
import java.util.List;
public record TaskDefinition(String id, String title, String summary, List<String> dependencies,
 List<String> requirements, OfficialSource officialSource, String nextAction, String note,
 String category,String applicability,int priority,String completionLabel,Estimate reviewedDuration,Estimate reviewedCost) {
 public TaskDefinition(String id,String title,String summary,List<String> dependencies,List<String> requirements,OfficialSource officialSource,String nextAction,String note,String category,String applicability,int priority,String completionLabel) {
  this(id,title,summary,dependencies,requirements,officialSource,nextAction,note,category,applicability,priority,completionLabel,null,null);
 }
 public TaskDefinition(String id,String title,String summary,List<String> dependencies,List<String> requirements,OfficialSource officialSource,String nextAction,String note) {
  this(id,title,summary,dependencies,requirements,officialSource,nextAction,note,"GENERAL","ALWAYS",0,"Mark this step done");
 }
 public TaskDefinition {
  dependencies=List.copyOf(dependencies);requirements=List.copyOf(requirements);
  for(var estimate:new Estimate[]{reviewedDuration,reviewedCost})if(estimate!=null&&(estimate.confidence()!=Estimate.Confidence.OFFICIAL||officialSource==null||!officialSource.url().equals(estimate.sourceUrl())))throw new IllegalArgumentException("A reviewed estimate must cite this task’s registered official source.");
 }
}
