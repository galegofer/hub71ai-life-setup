package com.lifesetup.application;
import com.lifesetup.domain.*;
import com.lifesetup.integration.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.lifesetup.domain.GovernmentServiceStatus.ConnectionMode.*;
import static com.lifesetup.domain.GovernmentServiceStatus.ServiceState.*;
class ServiceEstimateTest {
 @Test void allTasksHaveTruthfulModeAndUnknownEstimates() {
  var snapshot=new ProfileService().create(UserProfile.demo());assertEquals(11,snapshot.plan().tasks().size());
  for(var task:snapshot.plan().tasks()) {
   var service=task.serviceEstimate();assertNotNull(service);
   assertEquals(List.of("residence","emirates-id").contains(task.definition().id())?MOCK:LINK_ONLY,service.status().connectionMode());
   assertNull(service.status().checkedAt());assertEquals(Estimate.Confidence.UNKNOWN,service.duration().confidence());assertEquals(Estimate.Confidence.UNKNOWN,service.cost().confidence());
   assertFalse(service.duration().basis().isBlank());assertFalse(service.cost().basis().isBlank());
   assertEquals(task.definition().officialSource()==null?null:task.definition().officialSource().url(),service.officialActionUrl());
  }
  var id=snapshot.plan().tasks().stream().filter(t->t.definition().id().equals("emirates-id")).findFirst().orElseThrow().serviceEstimate();
  assertEquals(IN_PROGRESS,id.status().state());assertEquals("Your application is being processed.",id.status().plainLanguageStatus());
 }
 @Test void modesSerializeAndLiveRequiresActualCheckTime() {
  var mapper=JsonMapper.builder().build();
  for(var mode:GovernmentServiceStatus.ConnectionMode.values()) {
   var status=new GovernmentServiceStatus(mode,UNKNOWN,"Test status",mode==LIVE?Instant.parse("2026-10-02T10:00:00Z"):null);
   var json=mapper.readTree(mapper.writeValueAsString(status));assertEquals(mode.name(),json.path("connectionMode").asText());
  }
  assertThrows(IllegalArgumentException.class,()->new GovernmentServiceStatus(LIVE,UNKNOWN,"Test status",null));
 }
 @Test void missingEstimatesBecomeExplicitUnknownAndOfficialRequiresSource() {
  var missing=new ServiceEstimate(null,null,null,null);assertEquals(LINK_ONLY,missing.status().connectionMode());assertEquals(Estimate.Confidence.UNKNOWN,missing.duration().confidence());assertEquals(Estimate.Confidence.UNKNOWN,missing.cost().confidence());
  assertThrows(IllegalArgumentException.class,()->new Estimate("Test fixture",Estimate.Confidence.OFFICIAL,"Test basis",null));
  assertThrows(IllegalArgumentException.class,()->new Estimate("Test fixture",Estimate.Confidence.UNKNOWN,"",null));
  var estimate=new Estimate("Test fixture",Estimate.Confidence.OFFICIAL,"Rendering fixture; no government fee claim.",TaskCatalogue.find("emirates-id").officialSource().url());
  assertEquals(Estimate.Confidence.OFFICIAL,estimate.confidence());assertNotNull(estimate.sourceUrl());
 }
 @Test void prototypeCompletionPreservesExactUnlockAndAssistant() {
  var profiles=new ProfileService();var snapshot=profiles.create(UserProfile.demo());var updated=profiles.action(snapshot.profileId(),"emirates-id","complete");
  assertEquals(List.of("bank","driving"),updated.newlyReadyTaskIds());
  var status=updated.plan().tasks().stream().filter(t->t.definition().id().equals("emirates-id")).findFirst().orElseThrow().serviceEstimate().status();
  assertEquals(MOCK,status.connectionMode());assertEquals(COMPLETED,status.state());assertTrue(status.plainLanguageStatus().contains("No official status"));
  var answer=new LifeSetupAssistant(profiles,null).ask(snapshot.profileId(),"Can I exchange my driving licence now?",null);
  assertTrue(answer.answer().startsWith("Yes."));assertTrue(answer.answer().contains("confirm whether you qualify"));
  profiles.action(snapshot.profileId(),"emirates-id","undo");assertTrue(new LifeSetupAssistant(profiles,null).ask(snapshot.profileId(),"Can I exchange my driving licence now?",null).answer().startsWith("Not yet."));
 }
 @Test void serviceEndpointRetainsExistingFieldsAndDoesNotInventProgress() {
  var result=new MockGovernmentServiceConnector().information(TaskCatalogue.find("emirates-id"));
  assertEquals("PROTOTYPE",result.mode());assertEquals("No live status check is connected.",result.status());assertEquals(TaskCatalogue.find("emirates-id").officialSource().url(),result.officialUrl());
  assertEquals(UNKNOWN,result.serviceEstimate().status().state());
 }
}
