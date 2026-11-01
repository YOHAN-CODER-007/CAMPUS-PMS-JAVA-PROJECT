package com.placement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Detailed profile for a registered student.
 * Linked 1:1 with the User account.
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String name;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @NotBlank(message = "College email is required")
    @Email(message = "Please enter a valid college email")
    @Column(nullable = false)
    private String collegeEmail;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    @Column(nullable = false, length = 15)
    private String contactNumber;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", inclusive = true, message = "CGPA cannot exceed 10.0")
    @Column(nullable = false)
    private Double cgpa;

    @NotNull(message = "Total arrears count is required")
    @Min(value = 0, message = "Arrears count cannot be negative")
    @Column(nullable = false)
    private Integer totalArrears = 0;

    @NotNull(message = "Passing year is required")
    @Min(value = 2020, message = "Passing year must be valid")
    @Max(value = 2035, message = "Passing year must be valid")
    @Column(nullable = false)
    private Integer passingYear;

    @Column(length = 500)
    private String skills; // Stored as comma-separated: Java, Python, Spring Boot

    private String resumePath; // File path to uploaded PDF resume

    @Column(nullable = false)
    private boolean placed = false;

    // Constructors
    public StudentProfile() {
    }

    public StudentProfile(User user, String name, String department, String collegeEmail, String contactNumber) {
        this.user = user;
        this.name = name;
        this.department = department;
        this.collegeEmail = collegeEmail;
        this.contactNumber = contactNumber;
    }

    /**
     * Calculates the profile completeness percentage (0 to 100%).
     * 10 fields evaluated (10% each):
     * name, dateOfBirth, department, collegeEmail, contactNumber,
     * cgpa, totalArrears, passingYear, skills, resumePath
     */
    public int getProfileCompleteness() {
        int completed = 0;
        if (name != null && !name.trim().isEmpty()) completed += 10;
        if (dateOfBirth != null) completed += 10;
        if (department != null && !department.trim().isEmpty()) completed += 10;
        if (collegeEmail != null && !collegeEmail.trim().isEmpty()) completed += 10;
        if (contactNumber != null && !contactNumber.trim().isEmpty()) completed += 10;
        if (cgpa != null) completed += 10;
        if (totalArrears != null) completed += 10;
        if (passingYear != null) completed += 10;
        if (skills != null && !skills.trim().isEmpty()) completed += 10;
        if (resumePath != null && !resumePath.trim().isEmpty()) completed += 10;
        return completed;
    }

    /**
     * Splits comma-separated skills into a clean, trimmed List of Strings.
     */
    public List<String> getSkillList() {
        if (skills == null || skills.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(skills.split(","))
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCollegeEmail() {
        return collegeEmail;
    }

    public void setCollegeEmail(String collegeEmail) {
        this.collegeEmail = collegeEmail;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getTotalArrears() {
        return totalArrears;
    }

    public void setTotalArrears(Integer totalArrears) {
        this.totalArrears = totalArrears;
    }

    public Integer getPassingYear() {
        return passingYear;
    }

    public void setPassingYear(Integer passingYear) {
        this.passingYear = passingYear;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }
}
