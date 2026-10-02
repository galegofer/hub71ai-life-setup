package com.lifesetup.application;
import com.lifesetup.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class LifePlanEngineTest {
 @Test void recommendationIdentifiesAnActionableTaskAndChangesWhenDeferred() {
  var service=new ProfileService();var initial=service.create(UserProfile.demo());
  assertEquals("emirates-id",initial.plan().nextBestTaskId());
  assertEquals(List.of("bank","driving"),initial.plan().nextBestUnlockTaskIds());
  var updated=service.action(initial.profileId(),"emirates-id","defer");
  assertEquals("housing",updated.plan().nextBestTaskId());
  assertEquals("Find a home",updated.plan().nextBestAction());
 }
 @Test void deferredAndBlockedStepsAreNotMistakenForACompletedPlan() {
  var a=new TaskDefinition("a","A","",List.of(),List.of(),null,"","");
  var b=new TaskDefinition("b","B","",List.of("a"),List.of(),null,"","");
  var engine=new LifePlanEngine(List.of(a,b));
  var plan=engine.calculate(new ProfileSession("test",UserProfile.demo(),Set.of(),Set.of(),Set.of("a")));
  assertNull(plan.nextBestTaskId());assertEquals(2,plan.remaining());assertEquals(0,plan.ready());
  assertTrue(plan.nextBestAction().contains("saved for later"));
  assertFalse(plan.nextBestAction().contains("all set"));
  var complete=engine.calculate(new ProfileSession("test",UserProfile.demo(),Set.of("a","b"),Set.of(),Set.of()));
  assertNull(complete.nextBestTaskId());assertEquals(0,complete.remaining());assertEquals("You’re all set for now.",complete.nextBestAction());
 }
 private TaskStatus status(ProfileService.Snapshot s,String id) { return s.plan().tasks().stream().filter(t->t.definition().id().equals(id)).findFirst().orElseThrow().status(); }
 @Test void demoUnlocksExactlyTwoTasksAndSupportsUndo() {
  var service=new ProfileService();var initial=service.create(UserProfile.demo());
  assertEquals(TaskStatus.BLOCKED,status(initial,"bank"));assertEquals(TaskStatus.BLOCKED,status(initial,"driving"));
  assertEquals(TaskStatus.IN_PROGRESS,status(initial,"emirates-id"));
  var updated=service.action(initial.profileId(),"emirates-id","complete");
  assertEquals(Set.of("bank","driving"),new HashSet<>(updated.newlyReadyTaskIds()));
  assertEquals(TaskStatus.DONE,status(updated,"residence"));
  var repeat=service.action(initial.profileId(),"emirates-id","complete");assertTrue(repeat.newlyReadyTaskIds().isEmpty());
  service.action(initial.profileId(),"bank","complete");
  var undone=service.action(initial.profileId(),"emirates-id","undo");
  assertEquals(TaskStatus.BLOCKED,status(undone,"bank"));assertEquals(TaskStatus.BLOCKED,status(undone,"driving"));
 }
 @Test void blocksPrematureCompletion() { var s=new ProfileService();var p=s.create(UserProfile.demo());assertThrows(IllegalArgumentException.class,()->s.action(p.profileId(),"driving","complete")); }
 @Test void cascadesUndoThroughHousingBranch() {
  var s=new ProfileService();var p=s.create(UserProfile.demo());s.action(p.profileId(),"housing","complete");s.action(p.profileId(),"tawtheeq","complete");s.action(p.profileId(),"utilities","complete");
  var undone=s.action(p.profileId(),"housing","undo");assertEquals(TaskStatus.BLOCKED,status(undone,"tawtheeq"));assertEquals(TaskStatus.BLOCKED,status(undone,"utilities"));
 }
 @Test void filtersConditionalTasks() {
  var p=new UserProfile("Spanish","Spain",false,false,false,UserProfile.ResidenceStatus.NOT_STARTED,false,false,false,"",false);
  var tasks=new ProfileService().create(p).plan().tasks();assertTrue(tasks.stream().noneMatch(t->Set.of("driving","family","school","pet").contains(t.definition().id())));
 }
 @Test void honoursAlreadyCompletedFacts() {
  var p=new UserProfile("Spanish","Spain",true,false,false,UserProfile.ResidenceStatus.COMPLETE,true,true,true,"Spain",false);
  var s=new ProfileService().create(p);assertEquals(TaskStatus.DONE,status(s,"emirates-id"));assertEquals(TaskStatus.READY,status(s,"bank"));assertEquals(TaskStatus.READY,status(s,"tawtheeq"));
 }
 @Test void defersAndResumes() { var service=new ProfileService();var s=service.create(UserProfile.demo());assertEquals(TaskStatus.LATER,status(service.action(s.profileId(),"housing","defer"),"housing"));assertEquals(TaskStatus.READY,status(service.action(s.profileId(),"housing","resume"),"housing")); }
 @Test void profileEditsRecalculateDependentProgress() {
  var service=new ProfileService();var s=service.create(UserProfile.demo());
  service.action(s.profileId(),"emirates-id","complete");var completed=service.action(s.profileId(),"bank","complete");
  assertTrue(completed.profile().hasEmiratesId());
  var p=completed.profile();var changed=new UserProfile(p.nationality(),p.movingFrom(),p.alreadyInUae(),p.movingWithFamily(),p.movingWithChildren(),UserProfile.ResidenceStatus.COMPLETE,false,p.hasHousing(),p.wantsToDrive(),p.licenceCountry(),p.bringingPet());
  var updated=service.update(s.profileId(),changed);assertEquals(TaskStatus.BLOCKED,status(updated,"bank"));assertEquals(TaskStatus.READY,status(updated,"emirates-id"));
 }
 @Test void idCollectionCanBeDeferred() { var service=new ProfileService();var s=service.create(UserProfile.demo());assertEquals(TaskStatus.LATER,status(service.action(s.profileId(),"emirates-id","defer"),"emirates-id")); }
 @Test void missingSessionIsExplicit() { assertThrows(NoSuchElementException.class,()->new ProfileService().get("missing")); }
 @Test void rejectsInvalidProfile() { assertThrows(IllegalArgumentException.class,()->new UserProfile("Spanish","Spain",true,false,false,UserProfile.ResidenceStatus.IN_PROGRESS,true,false,false,"",false)); }
 @Test void rejectsDependencyCyclesAndMissingIds() {
  var a=new TaskDefinition("a","A","",List.of("b"),List.of(),null,"","");var b=new TaskDefinition("b","B","",List.of("a"),List.of(),null,"","");
  assertThrows(IllegalArgumentException.class,()->new LifePlanEngine(List.of(a,b)));assertThrows(IllegalArgumentException.class,()->new LifePlanEngine(List.of(a)));
 }
 @Test void modelInterpretationCannotUnlockTasksAndFailuresFallBack() {
  var s=new ProfileService();var p=s.create(UserProfile.demo());
  var a=new LifeSetupAssistant(s,(question,ids)->new com.lifesetup.integration.OpenAiClient.Intent("driving","STATUS"));
  assertTrue(a.ask(p.profileId(),"May I swap my foreign permit?",null).answer().startsWith("Not yet."));
  var unavailable=new LifeSetupAssistant(s,(question,ids)->{throw new IllegalStateException("offline");});
  assertEquals("PLAN_RULES_FALLBACK",unavailable.ask(p.profileId(),"Can I exchange my driving licence now?",null).mode());
 }
 @Test void assistantReflectsNewStateAndDoesNotInventCosts() {
  var s=new ProfileService();var p=s.create(UserProfile.demo());var a=new LifeSetupAssistant(s,null);
  assertTrue(a.ask(p.profileId(),"Can I exchange my driving licence now?",null).answer().startsWith("Not yet."));
  s.action(p.profileId(),"emirates-id","complete");assertTrue(a.ask(p.profileId(),"Can I exchange my driving licence now?",null).answer().startsWith("Yes."));
  assertTrue(a.ask(p.profileId(),"How much does a driving licence cost?",null).answer().contains("verified current cost"));
 }
}
