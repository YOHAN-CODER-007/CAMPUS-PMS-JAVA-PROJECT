package com.placement.service;

import com.placement.dto.EligibilityResult;
import com.placement.model.Job;
import com.placement.model.StudentProfile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service providing real-time Skill Match calculation and Automated Eligibility checks.
 */
@Service
public class EligibilityService {

    /**
     * Map of canonical department codes to all recognized synonyms, abbreviations, and full names.
     */
    private static final Map<String, Set<String>> DEPT_SYNONYMS = new LinkedHashMap<>();

    static {
        DEPT_SYNONYMS.put("CSE", new HashSet<>(Arrays.asList(
                "cse", "cs", "cs&e", "cs and e", "comp sci", "computer science",
                "computer science & engineering", "computer science and engineering",
                "computer engineering", "computer science engineering"
        )));

        DEPT_SYNONYMS.put("IT", new HashSet<>(Arrays.asList(
                "it", "ise", "is", "infotech", "info tech", "information technology",
                "information science", "information science & engineering",
                "information science and engineering"
        )));

        DEPT_SYNONYMS.put("ECE", new HashSet<>(Arrays.asList(
                "ece", "ec", "e&c", "electronics", "electronics & communication",
                "electronics and communication", "electronics & communication engineering",
                "electronics and communication engineering", "electronics communication"
        )));

        DEPT_SYNONYMS.put("EEE", new HashSet<>(Arrays.asList(
                "eee", "ee", "e&e", "electrical", "electrical & electronics",
                "electrical and electronics", "electrical & electronics engineering",
                "electrical and electronics engineering", "electrical electronics"
        )));

        DEPT_SYNONYMS.put("MECH", new HashSet<>(Arrays.asList(
                "mech", "me", "mechanical", "mechanical engineering"
        )));

        DEPT_SYNONYMS.put("CIVIL", new HashSet<>(Arrays.asList(
                "civil", "ce", "civil engineering"
        )));

        DEPT_SYNONYMS.put("AIDS", new HashSet<>(Arrays.asList(
                "ai", "ds", "aids", "ai/ds", "ai&ds", "ai & ds", "artificial intelligence",
                "data science", "artificial intelligence & data science",
                "artificial intelligence and data science"
        )));
    }

    /**
     * Calculates the case-insensitive skill match percentage between student skills and job requirements.
     */
    public int calculateSkillMatch(StudentProfile student, Job job) {
        if (job == null || job.getRequiredSkillsList().isEmpty()) {
            return 100;
        }
        if (student == null || student.getSkillList().isEmpty()) {
            return 0;
        }

        List<String> required = job.getRequiredSkillsList();
        Set<String> studentSkills = student.getSkillList().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        long matched = required.stream()
                .filter(req -> studentSkills.contains(req.trim().toLowerCase()))
                .count();

        return (int) Math.round(((double) matched / required.size()) * 100.0);
    }

    /**
     * Finds missing skills required by the job that the student has not listed.
     */
    public List<String> getMissingSkills(StudentProfile student, Job job) {
        if (job == null || job.getRequiredSkillsList().isEmpty()) {
            return Collections.emptyList();
        }
        if (student == null || student.getSkillList().isEmpty()) {
            return job.getRequiredSkillsList();
        }

        Set<String> studentSkills = student.getSkillList().stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return job.getRequiredSkillsList().stream()
                .filter(req -> !studentSkills.contains(req.trim().toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Runs comprehensive eligibility validation against all job criteria.
     */
    public EligibilityResult evaluate(StudentProfile student, Job job, boolean alreadyApplied) {
        EligibilityResult result = new EligibilityResult();
        List<String> reasons = new ArrayList<>();

        result.setSkillMatchPercentage(calculateSkillMatch(student, job));
        result.setMissingSkills(getMissingSkills(student, job));

        boolean academicEligible = true;

        if (student == null) {
            reasons.add("Student profile not found. Please log in with a student account.");
            result.setEligible(false);
            result.setCanApply(false);
            result.setFailureReasons(reasons);
            return result;
        }

        // 1. Profile completeness rule (Must be 100%)
        int completeness = student.getProfileCompleteness();
        if (completeness < 100) {
            reasons.add("Profile incomplete (" + completeness + "%). 100% profile completeness required before applying.");
            result.setProfileIncomplete(true);
        }

        // 2. Placed student rule
        if (student.isPlaced()) {
            reasons.add("You are already marked as PLACED in campus recruitment.");
            result.setAlreadyPlaced(true);
        }

        // 3. Minimum CGPA rule
        if (student.getCgpa() == null || student.getCgpa() < job.getMinCgpa()) {
            academicEligible = false;
            double currentCgpa = (student.getCgpa() != null) ? student.getCgpa() : 0.0;
            reasons.add("CGPA below " + job.getMinCgpa() + " (Your CGPA: " + currentCgpa + ")");
        }

        // 4. Maximum Arrears rule
        if (student.getTotalArrears() != null && student.getTotalArrears() > job.getMaxArrearsAllowed()) {
            academicEligible = false;
            reasons.add("Arrears count (" + student.getTotalArrears() + ") exceeds maximum allowed (" + job.getMaxArrearsAllowed() + ")");
        }

        // 5. Eligible Department rule (supporting full form & short form e.g. "Computer Science & Engineering" <-> "CSE")
        if (student.getDepartment() != null && !student.getDepartment().trim().isEmpty()) {
            boolean deptMatches = isDepartmentEligible(student.getDepartment(), job.getEligibleDepartmentsList());

            if (!deptMatches) {
                academicEligible = false;
                reasons.add("Your department ('" + student.getDepartment() + "') is not listed as eligible for this opening (" + job.getEligibleDepartments() + ")");
            }
        } else {
            academicEligible = false;
            reasons.add("Department not specified in profile. Please select your department in My Profile.");
        }

        // 6. Passing Year rule
        if (student.getPassingYear() != null && !student.getPassingYear().equals(job.getEligiblePassingYear())) {
            academicEligible = false;
            reasons.add("Passing year not eligible (Requires " + job.getEligiblePassingYear() + ", your year: " + student.getPassingYear() + ")");
        }

        // 7. Deadline rule
        if (job.isExpired()) {
            reasons.add("Application deadline passed on " + job.getDeadline());
            result.setDeadlinePassed(true);
        }

        // 8. Already applied rule
        if (alreadyApplied) {
            reasons.add("You have already applied for this job drive.");
            result.setAlreadyApplied(true);
        }

        result.setEligible(academicEligible);
        result.setFailureReasons(reasons);

        // Can apply only if academically eligible, profile complete, not placed, deadline not passed, and not already applied
        boolean canApply = academicEligible
                && !result.isProfileIncomplete()
                && !result.isAlreadyPlaced()
                && !result.isDeadlinePassed()
                && !result.isAlreadyApplied();

        result.setCanApply(canApply);
        return result;
    }

    /**
     * Checks if a student's department matches any eligible department specified by a job.
     * Supports flexible matching between full names and acronyms (e.g. "Computer Science & Engineering" <-> "CSE").
     */
    public boolean isDepartmentEligible(String studentDept, List<String> eligibleDepts) {
        if (studentDept == null || studentDept.trim().isEmpty()) {
            return false;
        }
        if (eligibleDepts == null || eligibleDepts.isEmpty()) {
            return true;
        }

        String normalizedStudent = normalizeDept(studentDept);
        Set<String> studentCanonicalCodes = findCanonicalCodes(normalizedStudent);

        for (String rawJobDept : eligibleDepts) {
            String normalizedJobDept = normalizeDept(rawJobDept);
            if (isUniversal(normalizedJobDept)) {
                return true;
            }
            if (normalizedStudent.equals(normalizedJobDept)) {
                return true;
            }
            Set<String> jobCanonicalCodes = findCanonicalCodes(normalizedJobDept);
            for (String code : studentCanonicalCodes) {
                if (jobCanonicalCodes.contains(code)) {
                    return true;
                }
            }
            String acronym = extractAcronym(normalizedStudent);
            if (!acronym.isEmpty() && acronym.equalsIgnoreCase(normalizedJobDept)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeDept(String s) {
        if (s == null) return "";
        return s.toLowerCase()
                .replaceAll("[&]", " and ")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isUniversal(String s) {
        return s.equals("all") || s.equals("any") || s.contains("all dept") || s.contains("all branch") || s.contains("open to all");
    }

    private Set<String> findCanonicalCodes(String normalized) {
        Set<String> codes = new HashSet<>();
        Set<String> tokens = new HashSet<>(Arrays.asList(normalized.split("\\s+")));

        for (Map.Entry<String, Set<String>> entry : DEPT_SYNONYMS.entrySet()) {
            for (String syn : entry.getValue()) {
                String normSyn = normalizeDept(syn);
                if (normSyn.isEmpty()) continue;

                // 1. Exact match
                if (normalized.equals(normSyn)) {
                    codes.add(entry.getKey());
                    break;
                }

                // 2. Multi-word phrase containment with boundary spaces
                if (normSyn.contains(" ") || normSyn.length() > 4) {
                    if ((" " + normalized + " ").contains(" " + normSyn + " ")) {
                        codes.add(entry.getKey());
                        break;
                    }
                } else {
                    // 3. Short acronyms/tokens (<= 4 chars) must match as an isolated token
                    if (tokens.contains(normSyn)) {
                        codes.add(entry.getKey());
                        break;
                    }
                }
            }
        }
        return codes;
    }

    private String extractAcronym(String s) {
        StringBuilder sb = new StringBuilder();
        for (String word : s.split("\\s+")) {
            if (!word.equals("and") && !word.equals("of") && !word.equals("in") && !word.isEmpty()) {
                sb.append(word.charAt(0));
            }
        }
        return sb.toString();
    }
}
