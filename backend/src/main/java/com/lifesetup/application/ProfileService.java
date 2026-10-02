package com.lifesetup.application;
import com.lifesetup.domain.*;
import com.lifesetup.integration.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
@Service
public class ProfileService {
 private final Map<String,ProfileSession> sessions=new ConcurrentHashMap<>();
 private final LifePlanEngine engine=new LifePlanEngine();
 private final GovernmentServiceConnector services;
 @org.springframework.beans.factory.annotation.Autowired
 public ProfileService() { this(new MockGovernmentServiceConnector()); }
 public ProfileService(GovernmentServiceConnector services) { this.services=Objects.requireNonNull(services); }
 public record Snapshot(String profileId,UserProfile profile,LifePlan plan,List<String> newlyReadyTaskIds) {
  public Snapshot { newlyReadyTaskIds=List.copyOf(newlyReadyTaskIds); }
 }
 public Snapshot create(UserProfile p) {
  String id=UUID.randomUUID().toString();
  var s=new ProfileSession(id,p,Set.of(),Set.of(),Set.of()); sessions.put(id,s); return snapshot(s,List.of());
 }
 public Snapshot get(String id) { return snapshot(session(id),List.of()); }
 private ProfileSession session(String id) {
  var s=sessions.get(id); if(s==null) throw new NoSuchElementException("Your prototype session has expired. Start a new plan.");return s;
 }
 private Snapshot snapshot(ProfileSession s,List<String> unlocked) {
  var calculated=engine.calculate(s);var p=s.profile();
  var enriched=calculated.tasks().stream().map(t->new LifeTask(t.definition(),t.status(),t.waitingFor(),services.information(t.definition(),t.status()).serviceEstimate())).toList();
  var plan=new LifePlan(enriched,calculated.nextBestAction(),calculated.nextBestTaskId(),calculated.nextBestUnlockTaskIds(),calculated.remaining(),calculated.ready(),calculated.completed());
  var done=plan.tasks().stream().filter(t->t.status()==TaskStatus.DONE).map(t->t.definition().id()).toList();
  var effective=new UserProfile(p.nationality(),p.movingFrom(),p.alreadyInUae(),p.movingWithFamily(),p.movingWithChildren(),done.contains("residence")?UserProfile.ResidenceStatus.COMPLETE:p.residenceStatus(),done.contains("emirates-id"),done.contains("housing"),p.wantsToDrive(),p.licenceCountry(),p.bringingPet());
  return new Snapshot(s.id(),effective,plan,unlocked);
 }
 public Snapshot update(String id,UserProfile p) {
  var holder=new ProfileSession[1];
  sessions.compute(id,(key,old)->{
   if(old==null) throw new NoSuchElementException("Session expired.");
   var effective=snapshot(old,List.of()).profile();var reset=new HashSet<String>();
   if(effective.hasEmiratesId()&&!p.hasEmiratesId())reset.add("emirates-id");
   if(effective.hasHousing()&&!p.hasHousing())reset.add("housing");
   if(effective.residenceStatus()==UserProfile.ResidenceStatus.COMPLETE&&p.residenceStatus()!=UserProfile.ResidenceStatus.COMPLETE)reset.add("residence");
   addDescendants(reset);
   var done=new HashSet<>(old.completedTasks());done.removeAll(reset);
   var started=new HashSet<>(old.startedTasks());started.removeAll(reset);
   // Facts explicitly confirmed in onboarding become profile facts, not undoable task events.
   if(p.hasEmiratesId())done.remove("emirates-id");
   if(p.hasHousing())done.remove("housing");
   if(p.residenceStatus()==UserProfile.ResidenceStatus.COMPLETE)done.remove("residence");
   holder[0]=new ProfileSession(id,p,done,started,old.deferredTasks());return holder[0];
  });
  return snapshot(holder[0],List.of());
 }
 private void addDescendants(Set<String> affected) {
  boolean changed;
  do { changed=false; for(var d:TaskCatalogue.all()) if(!affected.contains(d.id()) && d.dependencies().stream().anyMatch(affected::contains)) { affected.add(d.id());changed=true; } } while(changed);
 }
 public Snapshot action(String id,String taskId,String action) {
  var result=new Snapshot[1];
  sessions.compute(id,(key,old)->{
   if(old==null) throw new NoSuchElementException("Session expired.");
   var before=engine.calculate(old);
   var task=before.tasks().stream().filter(t->t.definition().id().equals(taskId)).findFirst().orElseThrow(()->new IllegalArgumentException("That task is not in your plan."));
   var done=new HashSet<>(old.completedTasks());var started=new HashSet<>(old.startedTasks());var later=new HashSet<>(old.deferredTasks());
   switch(action) {
    case "complete" -> { if(task.status()==TaskStatus.BLOCKED || task.status()==TaskStatus.LATER) throw new IllegalArgumentException("Finish the steps before this one first.");done.add(taskId);started.remove(taskId);later.remove(taskId); }
    case "start" -> { if(task.status()!=TaskStatus.READY) throw new IllegalArgumentException("This step is not ready to start.");started.add(taskId); }
    case "defer" -> { if(task.status()==TaskStatus.DONE) throw new IllegalArgumentException("This step is already done.");later.add(taskId);started.remove(taskId); }
    case "resume" -> later.remove(taskId);
    case "undo" -> {
     if(!done.contains(taskId)) throw new IllegalArgumentException("This step was already done when you created your profile. Edit your answers to change it.");
     done.remove(taskId);
     var affected=new HashSet<String>();affected.add(taskId);
     addDescendants(affected);
     done.removeAll(affected);started.removeAll(affected);

    }
    default -> throw new IllegalArgumentException("Unknown task action.");
   }
   var updated=new ProfileSession(id,old.profile(),done,started,later);
   var after=engine.calculate(updated);
   var readyBefore=before.tasks().stream().filter(t->t.status()==TaskStatus.READY || t.status()==TaskStatus.IN_PROGRESS).map(t->t.definition().id()).toList();
   var newly=after.tasks().stream().filter(t->t.status()==TaskStatus.READY && !readyBefore.contains(t.definition().id())).map(t->t.definition().id()).toList();
   result[0]=snapshot(updated,newly);return updated;
  });return result[0];
 }
}
