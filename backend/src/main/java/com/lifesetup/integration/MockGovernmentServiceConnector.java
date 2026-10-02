package com.lifesetup.integration;
import com.lifesetup.domain.*;
import static com.lifesetup.domain.GovernmentServiceStatus.ConnectionMode.*;
import static com.lifesetup.domain.GovernmentServiceStatus.ServiceState.*;
public final class MockGovernmentServiceConnector implements GovernmentServiceConnector {
 public ServiceInfo information(TaskDefinition task,TaskStatus planStatus) {
  boolean simulated=task.id().equals("residence")||task.id().equals("emirates-id");
  var state=UNKNOWN;String text="We can’t check this status yet.";
  if(simulated&&planStatus!=null)switch(planStatus) {
   case IN_PROGRESS -> {state=IN_PROGRESS;text=task.id().equals("emirates-id")?"Your application is being processed.":"Your residence process is in progress.";}
   case DONE -> {state=COMPLETED;text="Recorded as completed in your plan. No official status has been checked.";}
   case READY -> {state=NOT_STARTED;text="No progress has been recorded for this step.";}
   case BLOCKED,LATER -> text="No current application status is recorded in this prototype.";
  }
  if(simulated&&planStatus==null)text="No current application status is recorded in this prototype.";
  String url=task.officialSource()==null?null:task.officialSource().url();
  boolean provider=java.util.Set.of("housing","bank","school","insurance").contains(task.id());
  var duration=task.reviewedDuration()!=null?task.reviewedDuration():Estimate.unknown("Not enough verified information yet.","The project sources do not establish a verified time estimate for this step. Confirm it with the service or provider.");
  var cost=task.reviewedCost()!=null?task.reviewedCost():Estimate.unknown(provider?"Varies by provider.":"Depends on your application.","The project sources do not establish a verified cost for this step. Confirm your own application or provider’s charges.");
  var estimate=new ServiceEstimate(new GovernmentServiceStatus(simulated?MOCK:LINK_ONLY,state,text,null),duration,cost,url);
  return new ServiceInfo("PROTOTYPE","No live status check is connected.",url,estimate);
 }
}
