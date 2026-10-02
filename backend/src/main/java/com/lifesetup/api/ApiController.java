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
 private final ProfileService profiles;private final LifeSetupAssistant assistant;
 public ApiController(ProfileService profiles,LifeSetupAssistant assistant) { this.profiles=profiles;this.assistant=assistant; }
 @GetMapping("/health") public Map<String,String> health() { return Map.of("status","ok","mode","prototype"); }
 @PostMapping("/profile") public ProfileService.Snapshot create(@RequestBody UserProfile p) { return profiles.create(p); }
 @PostMapping("/demo") public ProfileService.Snapshot demo() { return profiles.create(UserProfile.demo()); }
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
