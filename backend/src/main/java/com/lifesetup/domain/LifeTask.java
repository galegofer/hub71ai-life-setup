package com.lifesetup.domain;
import java.util.List;
public record LifeTask(TaskDefinition definition, TaskStatus status, List<String> waitingFor,ServiceEstimate serviceEstimate,
 List<String> immediateUnlockTaskIds,String planStatusSummary,String nextAction,String afterCompletion) {
 public LifeTask(TaskDefinition definition,TaskStatus status,List<String> waitingFor) { this(definition,status,waitingFor,null,List.of(),"","",""); }
 public LifeTask(TaskDefinition definition,TaskStatus status,List<String> waitingFor,ServiceEstimate serviceEstimate) { this(definition,status,waitingFor,serviceEstimate,List.of(),"","",""); }
 public LifeTask { waitingFor=List.copyOf(waitingFor);immediateUnlockTaskIds=List.copyOf(immediateUnlockTaskIds); }
}
