package com.placement.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of automated eligibility checks on a student for a specific job drive.
 */
public class EligibilityResult {

    private boolean eligible;
    private boolean canApply;
    private int skillMatchPercentage;
    private List<String> missingSkills = new ArrayList<>();
    private List<String> failureReasons = new ArrayList<>();
    private boolean alreadyApplied;
    private boolean deadlinePassed;
    private boolean alreadyPlaced;
    private boolean profileIncomplete;

    public EligibilityResult() {
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isCanApply() {
        return canApply;
    }

    public void setCanApply(boolean canApply) {
        this.canApply = canApply;
    }

    public int getSkillMatchPercentage() {
        return skillMatchPercentage;
    }

    public void setSkillMatchPercentage(int skillMatchPercentage) {
        this.skillMatchPercentage = skillMatchPercentage;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(List<String> failureReasons) {
        this.failureReasons = failureReasons;
    }

    public boolean isAlreadyApplied() {
        return alreadyApplied;
    }

    public void setAlreadyApplied(boolean alreadyApplied) {
        this.alreadyApplied = alreadyApplied;
    }

    public boolean isDeadlinePassed() {
        return deadlinePassed;
    }

    public void setDeadlinePassed(boolean deadlinePassed) {
        this.deadlinePassed = deadlinePassed;
    }

    public boolean isAlreadyPlaced() {
        return alreadyPlaced;
    }

    public void setAlreadyPlaced(boolean alreadyPlaced) {
        this.alreadyPlaced = alreadyPlaced;
    }

    public boolean isProfileIncomplete() {
        return profileIncomplete;
    }

    public void setProfileIncomplete(boolean profileIncomplete) {
        this.profileIncomplete = profileIncomplete;
    }
}
