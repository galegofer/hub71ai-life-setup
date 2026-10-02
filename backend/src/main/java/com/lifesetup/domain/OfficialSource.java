package com.lifesetup.domain;
import java.time.LocalDate;
public record OfficialSource(String authority, String title, String url, LocalDate lastChecked, String scope) { }
