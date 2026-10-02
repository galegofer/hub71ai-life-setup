package com.lifesetup.domain;
import java.util.List;
public record LifePlan(List<LifeTask> tasks, String nextBestAction, String nextBestTaskId,List<String> nextBestUnlockTaskIds, long remaining, long ready, long completed) {
 public LifePlan { tasks=List.copyOf(tasks);nextBestUnlockTaskIds=List.copyOf(nextBestUnlockTaskIds); }
}
