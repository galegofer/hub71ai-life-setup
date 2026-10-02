package com.lifesetup.integration;
import com.lifesetup.domain.TaskDefinition;
public interface GovernmentServiceConnector {
 record ServiceInfo(String mode,String status,String officialUrl) {}
 ServiceInfo information(TaskDefinition task);
}
