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
 private List<LifeTask> tasks(ProfileSession session,Set<String> hypothetical) {
  var p=session.profile(); Set<String> done=new HashSet<>(session.completedTasks());done.addAll(hypothetical);
  if(p.residenceStatus()==UserProfile.ResidenceStatus.COMPLETE) done.add("residence");
  if(p.hasEmiratesId()) done.add("emirates-id");
  if(p.hasHousing()) done.add("housing");
  // Receipt of the ID confirms the related residence milestone in this prototype.
  if(done.contains("emirates-id")) done.add("residence");
  return catalogue.stream().filter(t->relevant(t,p)).map(t->{
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
 }
 private List<String> unlocks(ProfileSession session,List<LifeTask> before,String candidate) {
  var blocked=before.stream().filter(t->t.status()==BLOCKED&&!session.deferredTasks().contains(t.definition().id())).map(t->t.definition().id()).collect(java.util.stream.Collectors.toSet());
  return tasks(session,Set.of(candidate)).stream().filter(t->blocked.contains(t.definition().id())&&(t.status()==READY||t.status()==IN_PROGRESS)).map(t->t.definition().id()).sorted().toList();
 }
 private String titles(List<LifeTask> tasks,List<String> ids) {
  return ids.stream().map(id->tasks.stream().filter(t->t.definition().id().equals(id)).findFirst().orElseThrow().definition().title()).reduce((a,b)->a+" · "+b).orElse("");
 }
 private String statusSummary(LifeTask task) {
  return switch(task.status()) {
   case READY -> "You can work on this step now.";
   case IN_PROGRESS -> task.definition().id().equals("emirates-id")?"Your Emirates ID step is underway.":task.definition().id().equals("residence")?"Your residence step is underway.":"You’ve started this step in your plan.";
   case BLOCKED -> "This step is waiting for an earlier step in your plan.";
   case DONE -> "You’ve completed this step in your plan.";
   case LATER -> "You’ve saved this step for later.";
  };
 }
 private String nextAction(LifeTask task,List<LifeTask> tasks) {
  return switch(task.status()) {
   case BLOCKED -> "First, "+titles(tasks,task.waitingFor()).toLowerCase(Locale.ROOT)+".";
   case LATER -> "Move this step back to your plan when you’re ready.";
   case DONE -> "Your plan has moved forward. Use undo if you need to change this step.";
   default -> String.join(" ",task.definition().requirements());
  };
 }
 private String afterCompletion(ProfileSession session,List<LifeTask> tasks,LifeTask task,List<String> immediate) {
  if(task.status()==DONE)return "";
  var direct=tasks.stream().filter(t->t.status()!=DONE&&!session.deferredTasks().contains(t.definition().id())&&t.definition().dependencies().contains(task.definition().id())).map(t->t.definition().id()).sorted().toList();
  if(direct.isEmpty())return "";
  String explanation=!immediate.isEmpty()?"Once you record completion, Life Setup moves these steps forward: "+titles(tasks,immediate)+".":"Related next steps in your plan: "+titles(tasks,direct)+". Their current readiness still applies.";
  var following=tasks.stream().filter(t->t.status()!=DONE&&!session.deferredTasks().contains(t.definition().id())&&!direct.contains(t.definition().id())&&t.definition().dependencies().stream().anyMatch(direct::contains)).map(t->t.definition().id()).sorted().toList();
  return explanation+(following.isEmpty()?"":" After those steps, your plan leads to: "+titles(tasks,following)+".");
 }
 private List<String> situation(UserProfile profile,List<LifeTask> tasks) {
  var facts=new ArrayList<String>();facts.add(profile.alreadyInUae()?"You’re already in the UAE.":"You’re preparing to move to the UAE.");
  tasks.stream().filter(t->t.definition().id().equals("residence")).findFirst().ifPresent(t->facts.add(t.status()==DONE?"Your residence milestone is recorded as complete.":t.status()==IN_PROGRESS?"Your residence is in progress.":"Your residence step is still ahead."));
  tasks.stream().filter(t->t.definition().id().equals("emirates-id")).findFirst().ifPresent(t->facts.add(t.status()==DONE?"Your Emirates ID is recorded as received.":"Your Emirates ID is not recorded as received yet."));
  if(profile.movingWithFamily())facts.add("You’re moving with family.");
  if(profile.wantsToDrive())facts.add("You want to drive.");
  if(profile.bringingPet()&&facts.size()<5)facts.add("You’re bringing a pet.");
  return facts.stream().limit(5).toList();
 }
 public LifePlan calculate(ProfileSession session) {
  var tasks=tasks(session,Set.of());
  long completed=tasks.stream().filter(t->t.status()==DONE).count();
  long ready=tasks.stream().filter(t->t.status()==READY || t.status()==IN_PROGRESS).count();
  var values=new HashMap<String,List<String>>();
  tasks.stream().filter(t->t.status()!=DONE).forEach(t->values.put(t.definition().id(),unlocks(session,tasks,t.definition().id())));
  var next=tasks.stream().filter(t->t.status()==READY || t.status()==IN_PROGRESS).sorted(
   Comparator.<LifeTask>comparingInt(t->values.get(t.definition().id()).size()).reversed()
    .thenComparing(Comparator.comparingInt((LifeTask t)->t.definition().priority()).reversed()).thenComparing(t->t.definition().id())).findFirst();
  String nextAction=next.map(t->t.definition().title()).orElseGet(()->{
   if(completed==tasks.size()) return "You’re all set for now.";
   return tasks.stream().anyMatch(t->t.status()==LATER)
    ? "Your steps are waiting or saved for later. Resume a step when you’re ready."
    : "Your steps are waiting. Open Coming next to see what you need first.";
  });
  var biggest=tasks.stream().filter(t->!values.getOrDefault(t.definition().id(),List.of()).isEmpty()).sorted(Comparator.<LifeTask>comparingInt(t->values.get(t.definition().id()).size()).reversed().thenComparing(Comparator.comparingInt((LifeTask t)->t.definition().priority()).reversed()).thenComparing(t->t.definition().id())).findFirst();
  var guided=tasks.stream().map(t->{var immediate=values.getOrDefault(t.definition().id(),List.of());return new LifeTask(t.definition(),t.status(),t.waitingFor(),null,immediate,statusSummary(t),nextAction(t,tasks),afterCompletion(session,tasks,t,immediate));}).toList();
  return new LifePlan(guided,nextAction,next.map(t->t.definition().id()).orElse(null),next.map(t->values.get(t.definition().id())).orElse(List.of()),tasks.size()-completed,ready,completed,tasks.stream().filter(t->t.status()==BLOCKED).count(),tasks.stream().filter(t->t.status()==LATER).count(),biggest.map(t->t.definition().id()).orElse(null),situation(session.profile(),tasks));
 }
}
