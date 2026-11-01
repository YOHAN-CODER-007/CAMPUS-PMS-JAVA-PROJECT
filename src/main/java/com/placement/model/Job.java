package com.placement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job posting entity created by Companies and approved by Officers.
 */
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank(message = "Job title is required")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Job type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobType jobType = JobType.FULL_TIME;

    @NotBlank(message = "Job description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotBlank(message = "Job location is required")
    @Column(nullable = false)
    private String location;

    @NotNull(message = "Package (LPA) is required")
    @DecimalMin(value = "0.1", inclusive = true, message = "Package must be at least 0.1 LPA")
    @Column(nullable = false)
    private Double packageLpa;

    @NotNull(message = "Vacancies count is required")
    @Min(value = 1, message = "There must be at least 1 vacancy")
    @Column(nullable = false)
    private Integer vacancies = 1;

    @NotNull(message = "Minimum CGPA is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", inclusive = true, message = "CGPA cannot exceed 10.0")
    @Column(nullable = false)
    private Double minCgpa = 6.0;

    @NotNull(message = "Maximum arrears allowed is required")
    @Min(value = 0, message = "Arrears allowed cannot be negative")
    @Column(nullable = false)
    private Integer maxArrearsAllowed = 0;

    @NotBlank(message = "Required skills are required (comma separated)")
    @Column(nullable = false, length = 500)
    private String requiredSkills; // e.g. "Java, Spring Boot, MySQL"

    @NotBlank(message = "At least one eligible department is required")
    @Column(nullable = false, length = 500)
    private String eligibleDepartments; // e.g. "Computer Science, Information Technology"

    @NotNull(message = "Eligible passing year is required")
    @Min(value = 2020, message = "Passing year must be valid")
    @Max(value = 2035, message = "Passing year must be valid")
    @Column(nullable = false)
    private Integer eligiblePassingYear;

    @Column(nullable = false)
    private boolean internshipExperienceRequired = false;

    private String internshipExperienceDetails; // short description if internship experience required

    @NotNull(message = "Application deadline is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate deadline;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Job() {
    }

    public Job(Company company, String title, JobType jobType, String description, String location,
               Double packageLpa, Integer vacancies, Double minCgpa, Integer maxArrearsAllowed,
               String requiredSkills, String eligibleDepartments, Integer eligiblePassingYear,
               boolean internshipExperienceRequired, String internshipExperienceDetails,
               LocalDate deadline, JobStatus status) {
        this.company = company;
        this.title = title;
        this.jobType = jobType;
        this.description = description;
        this.location = location;
        this.packageLpa = packageLpa;
        this.vacancies = vacancies;
        this.minCgpa = minCgpa;
        this.maxArrearsAllowed = maxArrearsAllowed;
        this.requiredSkills = requiredSkills;
        this.eligibleDepartments = eligibleDepartments;
        this.eligiblePassingYear = eligiblePassingYear;
        this.internshipExperienceRequired = internshipExperienceRequired;
        this.internshipExperienceDetails = internshipExperienceDetails;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Checks if the job application deadline has passed.
     */
    public boolean isExpired() {
        return deadline != null && deadline.isBefore(LocalDate.now());
    }

    /**
     * Returns the effective status, automatically showing CLOSED if deadline passed.
     */
    public JobStatus getEffectiveStatus() {
        if (this.status == JobStatus.APPROVED && isExpired()) {
            return JobStatus.CLOSED;
        }
        return this.status;
    }

    /**
     * Splits comma-separated required skills into a clean List.
     */
    public List<String> getRequiredSkillsList() {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(requiredSkills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Splits comma-separated departments into a clean List.
     */
    public List<String> getEligibleDepartmentsList() {
        if (eligibleDepartments == null || eligibleDepartments.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(eligibleDepartments.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(Double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public Integer getVacancies() {
        return vacancies;
    }

    public void setVacancies(Integer vacancies) {
        this.vacancies = vacancies;
    }

    public Double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(Double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public Integer getMaxArrearsAllowed() {
        return maxArrearsAllowed;
    }

    public void setMaxArrearsAllowed(Integer maxArrearsAllowed) {
        this.maxArrearsAllowed = maxArrearsAllowed;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getEligibleDepartments() {
        return eligibleDepartments;
    }

    public void setEligibleDepartments(String eligibleDepartments) {
        this.eligibleDepartments = eligibleDepartments;
    }

    public Integer getEligiblePassingYear() {
        return eligiblePassingYear;
    }

    public void setEligiblePassingYear(Integer eligiblePassingYear) {
        this.eligiblePassingYear = eligiblePassingYear;
    }

    public boolean isInternshipExperienceRequired() {
        return internshipExperienceRequired;
    }

    public void setInternshipExperienceRequired(boolean internshipExperienceRequired) {
        this.internshipExperienceRequired = internshipExperienceRequired;
    }

    public String getInternshipExperienceDetails() {
        return internshipExperienceDetails;
    }

    public void setInternshipExperienceDetails(String internshipExperienceDetails) {
        this.internshipExperienceDetails = internshipExperienceDetails;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
