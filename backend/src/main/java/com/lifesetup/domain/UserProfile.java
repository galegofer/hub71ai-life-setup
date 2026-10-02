package com.lifesetup.domain;
public record UserProfile(String nationality, String movingFrom, boolean alreadyInUae,
 boolean movingWithFamily, boolean movingWithChildren, ResidenceStatus residenceStatus,
 boolean hasEmiratesId, boolean hasHousing, boolean wantsToDrive,
 String licenceCountry, boolean bringingPet) {
 public enum ResidenceStatus { NOT_STARTED, IN_PROGRESS, COMPLETE }
 public UserProfile {
  nationality=Countries.normalize(nationality,false);
  movingFrom=Countries.normalize(movingFrom,false);
  licenceCountry=Countries.normalize(licenceCountry,true);
  if (residenceStatus == null) throw new IllegalArgumentException("Choose your residence status.");
  if (hasEmiratesId && residenceStatus != ResidenceStatus.COMPLETE) throw new IllegalArgumentException("Confirm your residence is complete if you have your Emirates ID.");
  if (movingWithChildren && !movingWithFamily) throw new IllegalArgumentException("Choose moving with family if children are moving with you.");
 }
 public static UserProfile demo() {
  return new UserProfile("ES","ES",true,true,true,ResidenceStatus.IN_PROGRESS,false,false,true,"ES",true);
 }
}
