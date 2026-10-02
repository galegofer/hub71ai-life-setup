package com.lifesetup.integration;
import com.lifesetup.domain.ProfileDraft;
@FunctionalInterface
public interface ProfileExtractionClient { ProfileDraft extract(String description) throws Exception; }
