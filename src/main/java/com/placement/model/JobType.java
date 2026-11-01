package com.placement.model;

/**
 * Types of job openings.
 */
public enum JobType {
    FULL_TIME("Full Time"),
    INTERNSHIP("Internship"),
    INTERNSHIP_PLUS_PPO("Internship + PPO");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
