package com.lifesetup.domain;
public record ProfileDraft(String nationality,String movingFrom,Boolean alreadyInUae,
 Boolean movingWithFamily,Boolean movingWithChildren,UserProfile.ResidenceStatus residenceStatus,
 Boolean hasEmiratesId,Boolean hasHousing,Boolean wantsToDrive,String licenceCountry,Boolean bringingPet) {
 public static ProfileDraft empty() { return new ProfileDraft(null,null,null,null,null,null,null,null,null,null,null); }
}
