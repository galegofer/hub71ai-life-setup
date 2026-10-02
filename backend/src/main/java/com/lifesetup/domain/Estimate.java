package com.lifesetup.domain;
import java.util.Objects;
public record Estimate(String displayValue,Confidence confidence,String basis,String sourceUrl) {
 public enum Confidence { OFFICIAL, PROVIDER_SPECIFIC, TYPICAL, PROTOTYPE, UNKNOWN }
 public Estimate {
  Objects.requireNonNull(confidence);
  if(displayValue==null||displayValue.isBlank()||basis==null||basis.isBlank())throw new IllegalArgumentException("An estimate needs a value and basis.");
  if(confidence==Confidence.OFFICIAL&&(sourceUrl==null||!sourceUrl.startsWith("https://")))throw new IllegalArgumentException("An official estimate needs its supporting source.");
 }
 public static Estimate unknown(String displayValue,String basis) { return new Estimate(displayValue,Confidence.UNKNOWN,basis,null); }
}
