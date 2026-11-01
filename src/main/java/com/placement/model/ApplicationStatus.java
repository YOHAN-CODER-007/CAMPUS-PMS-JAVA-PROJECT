package com.placement.model;

/**
 * Status of a student's job application.
 * Status badge colors per spec:
 * APPLIED (Blue)
 * SHORTLISTED (Amber)
 * INTERVIEW_SCHEDULED (Purple)
 * SELECTED (Green)
 * REJECTED (Red)
 * PLACED (Dark Green)
 */
public enum ApplicationStatus {
    APPLIED("Applied", "badge-applied"),
    SHORTLISTED("Shortlisted", "badge-shortlisted"),
    INTERVIEW_SCHEDULED("Interview Scheduled", "badge-interview"),
    SELECTED("Selected", "badge-selected"),
    REJECTED("Rejected", "badge-rejected"),
    PLACED("Placed", "badge-placed");

    private final String displayName;
    private final String badgeClass;

    ApplicationStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
