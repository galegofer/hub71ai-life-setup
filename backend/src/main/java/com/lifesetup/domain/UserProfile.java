package com.lifesetup.domain;
public record UserProfile(String nationality, String movingFrom, boolean alreadyInUae,
 boolean movingWithFamily, boolean movingWithChildren, ResidenceStatus residenceStatus,
 boolean hasEmiratesId, boolean hasHousing, boolean wantsToDrive,
 String licenceCountry, boolean bringingPet) {
 public enum ResidenceStatus { NOT_STARTED, IN_PROGRESS, COMPLETE }
 public UserProfile {
  if (nationality == null || nationality.isBlank() || nationality.length()>100) throw new IllegalArgumentException("Tell us your nationality.");
  if (residenceStatus == null) throw new IllegalArgumentException("Choose your residence status.");
  if (hasEmiratesId && residenceStatus != ResidenceStatus.COMPLETE) throw new IllegalArgumentException("Confirm your residence is complete if you have your Emirates ID.");
  if (movingWithChildren && !movingWithFamily) throw new IllegalArgumentException("Choose moving with family if children are moving with you.");
  if (movingFrom == null || movingFrom.length()>100 || licenceCountry == null || licenceCountry.length()>100) throw new IllegalArgumentException("Check your country answers.");
 }
 public static UserProfile demo() {
  return new UserProfile("Spanish","Spain",true,true,true,ResidenceStatus.IN_PROGRESS,false,false,true,"Spain",true);
 }
}
