package com.lifesetup.domain;
import java.util.List;
public record LifeTask(TaskDefinition definition, TaskStatus status, List<String> waitingFor,ServiceEstimate serviceEstimate) {
 public LifeTask(TaskDefinition definition,TaskStatus status,List<String> waitingFor) { this(definition,status,waitingFor,null); }
 public LifeTask { waitingFor=List.copyOf(waitingFor); }
}
