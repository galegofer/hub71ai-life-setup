package com.lifesetup.domain;
import java.time.Instant;
import java.util.Objects;
public record GovernmentServiceStatus(ConnectionMode connectionMode,ServiceState state,String plainLanguageStatus,Instant checkedAt) {
 public enum ConnectionMode { LIVE, MOCK, LINK_ONLY }
 public enum ServiceState { UNKNOWN, NOT_STARTED, IN_PROGRESS, COMPLETED }
 public GovernmentServiceStatus {
  Objects.requireNonNull(connectionMode);Objects.requireNonNull(state);
  if(plainLanguageStatus==null||plainLanguageStatus.isBlank())throw new IllegalArgumentException("A plain-language status is required.");
  if(connectionMode==ConnectionMode.LIVE&&checkedAt==null)throw new IllegalArgumentException("Live status requires a check time.");
 }
}
