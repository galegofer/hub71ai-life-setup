package com.lifesetup.integration;

import com.lifesetup.domain.*;

import static com.lifesetup.domain.GovernmentServiceStatus.ConnectionMode.*;

import static com.lifesetup.domain.GovernmentServiceStatus.ServiceState.*;

public final class MockGovernmentServiceConnector implements GovernmentServiceConnector {

 public ServiceInfo information(TaskDefinition task,TaskStatus planStatus) {

  boolean simulated=java.util.Set.of("residence","emirates-id").contains(task.id());

  var state=UNKNOWN;String text="We can't check this status yet.";

  if(simulated&&planStatus!=null)switch(planStatus) {

   case IN_PROGRESS -> {state=IN_PROGRESS;text=task.id().equals("emirates-id")?"Your application is being processed.":"Application in progress";}

   case DONE -> {state=COMPLETED;text="Recorded as completed in your plan. No official status has been checked.";}

   case READY -> {state=NOT_STARTED;text=switch(task.id()) {case "housing" -> "Looking for a home";case "driving" -> "Ready to check your options";case "insurance" -> "Ready to review";case "family","school","pet" -> "Planning";default -> "Ready to start";};}

   case BLOCKED -> text="Waiting for an earlier step in your plan";

   case LATER -> text="Saved for later in your plan";

  }

  if(simulated&&planStatus==null)text="No current application status is recorded in this prototype.";

  String url=task.officialSource()==null?null:task.officialSource().url();

  boolean provider=java.util.Set.of("housing","bank","school","insurance").contains(task.id());

  var duration=task.reviewedDuration()!=null?task.reviewedDuration():Estimate.unknown("Timing varies by case.","The project sources do not establish a verified time estimate for this step. Confirm it with the service or provider.");

  var cost=task.reviewedCost()!=null?task.reviewedCost():Estimate.unknown(provider?"Cost varies by provider.":"Cost depends on your application.","The project sources do not establish a verified cost for this step. Confirm your own application or provider’s charges.");

  var estimate=new ServiceEstimate(new GovernmentServiceStatus(simulated?MOCK:LINK_ONLY,state,text,null),duration,cost,url);

  return new ServiceInfo("PROTOTYPE","No live status check is connected.",url,estimate);

 }

}

