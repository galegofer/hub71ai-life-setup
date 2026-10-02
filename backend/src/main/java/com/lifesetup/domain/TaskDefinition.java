package com.lifesetup.domain;
import java.util.List;
public record TaskDefinition(String id, String title, String summary, List<String> dependencies,
 List<String> requirements, OfficialSource officialSource, String nextAction, String note) {
 public TaskDefinition { dependencies=List.copyOf(dependencies); requirements=List.copyOf(requirements); }
}
