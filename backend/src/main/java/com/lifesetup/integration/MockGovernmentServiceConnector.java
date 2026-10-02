package com.lifesetup.integration;
import com.lifesetup.domain.TaskDefinition;
public final class MockGovernmentServiceConnector implements GovernmentServiceConnector {
 public ServiceInfo information(TaskDefinition task) {
  return new ServiceInfo("PROTOTYPE","No live status check is connected.",task.officialSource()==null?null:task.officialSource().url());
 }
}
