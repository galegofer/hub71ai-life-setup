package com.lifesetup.api;
import com.lifesetup.application.*;
import com.lifesetup.domain.*;
import com.lifesetup.integration.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api")
public class ApiController {
 private final ProfileService profiles;private final LifeSetupAssistant assistant;private final ProfileExtractionService extraction;
 public ApiController(ProfileService profiles,LifeSetupAssistant assistant,ProfileExtractionService extraction) { this.profiles=profiles;this.assistant=assistant;this.extraction=extraction; }
 @GetMapping("/health") public Map<String,String> health() { return Map.of("status","ok","mode","prototype"); }
 @GetMapping("/countries") public Countries.Dataset countries() { return Countries.dataset(); }
 @GetMapping("/catalogue") public Map<String,Object> catalogue() { return Map.of("name","Abu Dhabi Newcomer Setup Graph","scope","Curated from official UAE and Abu Dhabi sources. Dependencies and applicability are prototype planning assumptions; discovery dates are not full requirements reviews.","stateRules",List.of("Confirmed profile facts and recorded completions make a step done.","Unfinished dependencies block a step, except the documented ID collection prototype rule.","Deferred steps are saved for later; other unblocked steps are ready or in progress.","Readiness does not confirm government eligibility."),"tasks",TaskCatalogue.all()); }
 @PostMapping("/profile") public ProfileService.Snapshot create(@RequestBody UserProfile p) { return profiles.create(p); }
 @PostMapping("/demo") public ProfileService.Snapshot demo() { return profiles.create(UserProfile.demo()); }
 @GetMapping("/demo-profile") public UserProfile demoProfile() { return UserProfile.demo(); }
 public record Description(@NotBlank @Size(max=1200) String description) {}
 @PostMapping("/profile/extract") public ProfileExtractionService.Result extract(@Valid @RequestBody Description request) { return extraction.extract(request.description()); }
 @GetMapping("/plan/{id}") public ProfileService.Snapshot plan(@PathVariable String id) { return profiles.get(id); }
 @PutMapping("/profile/{id}") public ProfileService.Snapshot update(@PathVariable String id,@RequestBody UserProfile p) { return profiles.update(id,p); }
 @PostMapping("/plan/{id}/tasks/{taskId}/complete") public ProfileService.Snapshot complete(@PathVariable String id,@PathVariable String taskId) { return profiles.action(id,taskId,"complete"); }
 public record Action(@NotBlank String action) {}
 @PostMapping("/plan/{id}/tasks/{taskId}") public ProfileService.Snapshot action(@PathVariable String id,@PathVariable String taskId,@Valid @RequestBody Action request) { return profiles.action(id,taskId,request.action()); }
 @GetMapping("/tasks/{id}") public TaskDefinition task(@PathVariable String id) { return TaskCatalogue.find(id); }
 @GetMapping("/tasks/{id}/service") public GovernmentServiceConnector.ServiceInfo info(@PathVariable String id) { return new MockGovernmentServiceConnector().information(TaskCatalogue.find(id)); }
 public record Question(@NotBlank String profileId,@NotBlank @Size(max=500) String question,String taskId) {}
 @PostMapping("/assistant") public LifeSetupAssistant.Answer ask(@Valid @RequestBody Question q) { return assistant.ask(q.profileId(),q.question(),q.taskId()); }
}
