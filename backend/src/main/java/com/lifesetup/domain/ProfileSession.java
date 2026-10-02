package com.lifesetup.domain;
import java.util.Set;
public record ProfileSession(String id, UserProfile profile, Set<String> completedTasks, Set<String> startedTasks, Set<String> deferredTasks) {
 public ProfileSession { completedTasks=Set.copyOf(completedTasks); startedTasks=Set.copyOf(startedTasks); deferredTasks=Set.copyOf(deferredTasks); }
}
