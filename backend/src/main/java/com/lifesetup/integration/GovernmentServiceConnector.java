package com.lifesetup.integration;
import com.lifesetup.domain.*;
public interface GovernmentServiceConnector {
 record ServiceInfo(String mode,String status,String officialUrl,ServiceEstimate serviceEstimate) {}
 default ServiceInfo information(TaskDefinition task) { return information(task,null); }
 ServiceInfo information(TaskDefinition task,TaskStatus planStatus);
}
