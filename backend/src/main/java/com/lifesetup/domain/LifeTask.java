package com.lifesetup.domain;
import java.util.List;
public record LifeTask(TaskDefinition definition, TaskStatus status, List<String> waitingFor) {
 public LifeTask { waitingFor=List.copyOf(waitingFor); }
}
