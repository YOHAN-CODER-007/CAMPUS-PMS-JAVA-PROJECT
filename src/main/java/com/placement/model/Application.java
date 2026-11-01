package com.placement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a student's job application and its recruitment workflow status.
 */
@Entity
@Table(name = "applications",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "job_id"})})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private LocalDateTime appliedDate = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate interviewDate;

    private String interviewTime; // e.g. "10:30 AM"

    private String interviewVenue; // e.g. "Seminar Hall B / Google Meet"

    // Constructors
    public Application() {
    }

    public Application(StudentProfile student, Job job) {
        this.student = student;
        this.job = job;
        this.appliedDate = LocalDateTime.now();
        this.status = ApplicationStatus.APPLIED;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(String interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getInterviewVenue() {
        return interviewVenue;
    }

    public void setInterviewVenue(String interviewVenue) {
        this.interviewVenue = interviewVenue;
    }
}
