package com.lifesetup.application;
import com.lifesetup.domain.*;
import com.lifesetup.integration.*;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class LifeSetupAssistant {
 private final ProfileService profiles;private final OpenAiClient openAi;
 public record Answer(String answer,String mode,List<OfficialSource> sources) {}
 @org.springframework.beans.factory.annotation.Autowired
 public LifeSetupAssistant(ProfileService profiles) { this(profiles,configuredClient()); }
 public LifeSetupAssistant(ProfileService profiles,OpenAiClient client) { this.profiles=profiles;this.openAi=client; }
 private static OpenAiClient configuredClient() {
  String key=System.getenv("OPENAI_API_KEY");String model=System.getenv("OPENAI_MODEL");
  return key!=null&&!key.isBlank()&&model!=null&&!model.isBlank()?new ResponsesOpenAiClient(key,model):null;
 }
 public Answer ask(String id,String question,String taskId) {
  var snapshot=profiles.get(id);var plan=snapshot.plan();String q=question.toLowerCase(Locale.ROOT);
  String selected=taskId;String kind="OTHER";String mode="PLAN_RULES";
  if(q.contains("cost")||q.contains("fee")||q.contains("how much")) kind="COST";
  else if(q.contains("how long")||q.contains("duration")) kind="TIME";
  else if(q.contains("document")||q.contains("need")||q.contains("require")) kind="REQUIREMENTS";
  else if(q.contains("update")||(q.startsWith("change ")||q.contains(" change "))||q.contains("received")||q.contains("i have")) kind="UPDATE";
  else if(q.contains("can i")||q.contains("ready")||q.contains("now")||q.contains("next")||q.contains("status")) kind="STATUS";
  if(selected==null || selected.isBlank()) {
   if(q.contains("licen") || q.contains("driv")) selected="driving";
   else if(q.contains("bank")) selected="bank";
   else if(q.contains("emirates") || q.contains(" id")) selected="emirates-id";
   else if(q.contains("water") || q.contains("electric")) selected="utilities";
   else if(q.contains("tawtheeq")||q.contains("rental registration")) selected="tawtheeq";
   else if(q.contains("home")||q.contains("hous")) selected="housing";
   else if(q.contains("residence")||q.contains("visa")) selected="residence";
   else if(q.contains("pet")) selected="pet";
   else if(q.contains("school")||q.contains("nursery")) selected="school";
   else if(q.contains("insurance")) selected="insurance";
  }
  if(openAi!=null) {
   try { var intent=openAi.interpret(question,plan.tasks().stream().map(t->t.definition().id()).toList());if(taskId==null||taskId.isBlank())selected=intent.taskId();kind=intent.kind();mode="OPENAI_INTENT_PLAN_RULES"; }
   catch(Exception ignored) { mode="PLAN_RULES_FALLBACK"; }
  }
  final String target=selected;
  var task=plan.tasks().stream().filter(t->t.definition().id().equals(target)).findFirst();
  if(kind.equals("UPDATE"))return new Answer("Edit your answers or mark the step done in your plan. I won’t change your progress from a chat message.",mode,List.of());
  if(kind.equals("COST")||kind.equals("TIME"))return new Answer("I don’t have a verified current "+(kind.equals("COST")?"cost":"time estimate")+" for this. Check with the official service or provider.",mode,task.isPresent()&&task.get().definition().officialSource()!=null?List.of(task.get().definition().officialSource()):List.of());
  if(task.isEmpty()) return new Answer("Your next step is: "+plan.nextBestAction()+" Open a task to see what to do. I can explain your plan, but official services must confirm requirements and costs.",mode,List.of());
  var t=task.get();String text;
  if(kind.equals("REQUIREMENTS"))text=String.join(" ",t.definition().requirements())+" Confirm the full requirements with the service provider.";
  else if(!kind.equals("STATUS"))text="I can explain this step in your plan. "+t.definition().summary()+" Check the service provider for other details.";
  else text=switch(t.status()) {
   case BLOCKED -> "Not yet. First, "+t.waitingFor().stream().map(d->TaskCatalogue.find(d).title().toLowerCase(Locale.ROOT)).reduce((a,b)->a+" and "+b).orElse("finish the earlier step")+". Then you can check this step.";
   case DONE -> "You’ve marked this step as done. Your plan has been updated.";
   case LATER -> "You’ve saved this step for later. Move it back to your plan when you’re ready.";
   default -> t.definition().id().equals("driving")?"Yes. Your plan is ready for you to check your licence exchange options. Open the official service to confirm whether you qualify and what you need.":"You can work on this now. "+t.definition().summary();
  };
  return new Answer(text,mode,t.definition().officialSource()==null?List.of():List.of(t.definition().officialSource()));
 }
}
