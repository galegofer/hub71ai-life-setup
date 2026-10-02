package com.lifesetup.application;
import com.lifesetup.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class CountriesTest {
 @Test void datasetUsesUniqueCodesAndNames() {
  var rows=Countries.dataset().countries();assertEquals(249,rows.size());
  assertEquals(rows.size(),rows.stream().map(Countries.Country::code).distinct().count());
  assertTrue(rows.stream().allMatch(c->c.code().matches("[A-Z]{2}")&&!c.name().isBlank()));
  assertEquals("Spain",rows.stream().filter(c->c.code().equals("ES")).findFirst().orElseThrow().name());
 }
 @Test void legacyValuesNormalizeAndUnknownIsValid() {
  assertEquals("ES",Countries.normalize("Spanish",false));assertEquals("ES",Countries.normalize("Spain",false));assertEquals("AE",Countries.normalize("UAE",false));
  assertEquals("NONE",Countries.normalize("None",true));assertNull(Countries.normalize(null,false));
  var p=new UserProfile(null,"ES",false,false,false,UserProfile.ResidenceStatus.NOT_STARTED,false,false,false,null,false);
  assertNull(p.nationality());assertNull(p.licenceCountry());assertEquals("ES",p.movingFrom());
  var snapshot=new ProfileService().create(p);assertNull(snapshot.profile().nationality());assertNull(snapshot.profile().licenceCountry());
 }
 @Test void invalidCodesAndNoneOutsideLicenceAreRejected() {
  for(String invalid:List.of("ZZ","XX","Atlantis","NONE"))assertThrows(IllegalArgumentException.class,()->Countries.normalize(invalid,false));
 }
 @Test void demoValuesAreExplicitAndIndependent() {
  var p=UserProfile.demo();assertEquals("ES",p.nationality());assertEquals("ES",p.movingFrom());assertEquals("ES",p.licenceCountry());
  var edited=new UserProfile("AE",p.movingFrom(),p.alreadyInUae(),p.movingWithFamily(),p.movingWithChildren(),p.residenceStatus(),p.hasEmiratesId(),p.hasHousing(),p.wantsToDrive(),p.licenceCountry(),p.bringingPet());
  assertEquals("ES",edited.movingFrom());assertEquals("ES",edited.licenceCountry());
 }
}
