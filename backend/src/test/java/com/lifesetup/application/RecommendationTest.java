package com.lifesetup.application;
import com.lifesetup.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class RecommendationTest {
 @Test void catalogueOrderCannotChangeRecommendationOrUnlocks() {
  var session=new ProfileSession("test",UserProfile.demo(),Set.of(),Set.of(),Set.of());
  var original=new LifePlanEngine().calculate(session);var reversed=new ArrayList<>(TaskCatalogue.all());Collections.reverse(reversed);
  var shuffled=new LifePlanEngine(reversed).calculate(session);
  assertEquals("emirates-id",original.nextBestTaskId());assertEquals(original.nextBestTaskId(),shuffled.nextBestTaskId());
  assertEquals(List.of("bank","driving"),shuffled.nextBestUnlockTaskIds());
 }
 @Test void explicitPriorityBreaksEqualUnlockValue() {
  var p=new UserProfile(null,null,false,false,false,UserProfile.ResidenceStatus.NOT_STARTED,false,false,false,null,false);
  var plan=new LifePlanEngine().calculate(new ProfileSession("test",p,Set.of(),Set.of(),Set.of()));
  assertEquals("residence",plan.nextBestTaskId());assertEquals(List.of("emirates-id"),plan.nextBestUnlockTaskIds());
 }
 @Test void savedAndCompletedTasksDoNotInflateUnlockValue() {
  var deferred=new LifePlanEngine().calculate(new ProfileSession("test",UserProfile.demo(),Set.of(),Set.of(),Set.of("bank","driving")));
  assertEquals("housing",deferred.nextBestTaskId());assertEquals(List.of("tawtheeq"),deferred.nextBestUnlockTaskIds());
  var completed=new LifePlanEngine().calculate(new ProfileSession("test",UserProfile.demo(),Set.of("emirates-id","bank","driving"),Set.of(),Set.of()));
  assertEquals("housing",completed.nextBestTaskId());assertFalse(completed.nextBestUnlockTaskIds().contains("bank"));
 }
 @Test void stableIdBreaksMetadataTies() {
  var z=new TaskDefinition("z","Z","",List.of(),List.of(),null,"","");var a=new TaskDefinition("a","A","",List.of(),List.of(),null,"","");
  var plan=new LifePlanEngine(List.of(z,a)).calculate(new ProfileSession("test",UserProfile.demo(),Set.of(),Set.of(),Set.of()));assertEquals("a",plan.nextBestTaskId());
 }
}
