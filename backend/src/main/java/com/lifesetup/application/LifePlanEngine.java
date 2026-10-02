package com.lifesetup.application;
import com.lifesetup.domain.*;
import java.util.*;
import static com.lifesetup.domain.TaskStatus.*;
public final class LifePlanEngine {
 private final List<TaskDefinition> catalogue;
 public LifePlanEngine() { this(TaskCatalogue.all()); }
 public LifePlanEngine(List<TaskDefinition> catalogue) { this.catalogue=List.copyOf(catalogue); validate(); }
 private void validate() {
  Map<String,TaskDefinition> byId=new HashMap<>();
  for(var t:catalogue) if(byId.put(t.id(),t)!=null) throw new IllegalArgumentException("Duplicate task ID");
  for(var t:catalogue) for(var d:t.dependencies()) if(!byId.containsKey(d)) throw new IllegalArgumentException("Missing dependency");
  for(var t:catalogue) visit(t.id(),byId,new HashSet<>(),new HashSet<>());
 }
 private void visit(String id,Map<String,TaskDefinition> tasks,Set<String> visiting,Set<String> visited) {
  if(visited.contains(id)) return;
  if(!visiting.add(id)) throw new IllegalArgumentException("Dependency cycle");
  tasks.get(id).dependencies().forEach(d->visit(d,tasks,visiting,visited));
  visiting.remove(id); visited.add(id);
 }
 public boolean relevant(TaskDefinition t,UserProfile p) {
  return switch(t.id()) { case "driving" -> p.wantsToDrive(); case "family" -> p.movingWithFamily();
   case "school" -> p.movingWithChildren(); case "pet" -> p.bringingPet(); default -> true; };
 }
 public LifePlan calculate(ProfileSession session) {
  var p=session.profile(); Set<String> done=new HashSet<>(session.completedTasks());
  if(p.residenceStatus()==UserProfile.ResidenceStatus.COMPLETE) done.add("residence");
  if(p.hasEmiratesId()) done.add("emirates-id");
  if(p.hasHousing()) done.add("housing");
  // Receipt of the ID confirms the related residence milestone in this prototype.
  if(done.contains("emirates-id")) done.add("residence");
  var tasks=catalogue.stream().filter(t->relevant(t,p)).map(t->{
   var waiting=t.dependencies().stream().filter(d->!done.contains(d)).toList();
   boolean collectingId=t.id().equals("emirates-id") && p.residenceStatus()==UserProfile.ResidenceStatus.IN_PROGRESS;
   if(collectingId)waiting=List.of();
   TaskStatus status;
   if(done.contains(t.id())) status=DONE;
   else if(!waiting.isEmpty()) status=BLOCKED;
   else if(session.deferredTasks().contains(t.id())) status=LATER;
   else if(collectingId || session.startedTasks().contains(t.id()) || t.id().equals("residence") && p.residenceStatus()==UserProfile.ResidenceStatus.IN_PROGRESS) status=IN_PROGRESS;
   else status=READY;

   return new LifeTask(t,status,waiting);
  }).toList();
  long completed=tasks.stream().filter(t->t.status()==DONE).count();
  long ready=tasks.stream().filter(t->t.status()==READY || t.status()==IN_PROGRESS).count();
  var next=tasks.stream().filter(t->t.status()==READY || t.status()==IN_PROGRESS).findFirst();
  String nextAction=next.map(t->t.definition().title()).orElseGet(()->{
   if(completed==tasks.size()) return "You’re all set for now.";
   return tasks.stream().anyMatch(t->t.status()==LATER)
    ? "Your steps are waiting or saved for later. Resume a step when you’re ready."
    : "Your steps are waiting. Open Coming next to see what you need first.";
  });
  return new LifePlan(tasks,nextAction,next.map(t->t.definition().id()).orElse(null),tasks.size()-completed,ready,completed);
 }
}
