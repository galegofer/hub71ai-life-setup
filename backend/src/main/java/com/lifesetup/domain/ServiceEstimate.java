package com.lifesetup.domain;
public record ServiceEstimate(GovernmentServiceStatus status,Estimate duration,Estimate cost,String officialActionUrl) {
 public ServiceEstimate {
  if(status==null)status=new GovernmentServiceStatus(GovernmentServiceStatus.ConnectionMode.LINK_ONLY,GovernmentServiceStatus.ServiceState.UNKNOWN,"We can’t check this status yet.",null);
  if(duration==null)duration=Estimate.unknown("Not enough verified information yet.","No verified time estimate is available.");
  if(cost==null)cost=Estimate.unknown("Not enough verified information yet.","No verified cost estimate is available.");
 }
}
