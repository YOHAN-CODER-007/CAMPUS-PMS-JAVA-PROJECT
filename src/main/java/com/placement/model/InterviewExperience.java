package com.placement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Interview Experience posted by placed students about a company.
 * Can be read by all students, deleted by the Officer if needed.
 */
@Entity
@Table(name = "interview_experiences")
public class InterviewExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank(message = "Rounds description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rounds; // e.g. "Round 1: Online Assessment, Round 2: Technical Interview..."

    @NotBlank(message = "Questions asked are required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionsAsked;

    @NotBlank(message = "Preparation tips are required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String tips;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public InterviewExperience() {
    }

    public InterviewExperience(StudentProfile student, Company company, String rounds,
                               String questionsAsked, String tips) {
        this.student = student;
        this.company = company;
        this.rounds = rounds;
        this.questionsAsked = questionsAsked;
        this.tips = tips;
        this.createdAt = LocalDateTime.now();
    }

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getRounds() {
        return rounds;
    }

    public void setRounds(String rounds) {
        this.rounds = rounds;
    }

    public String getQuestionsAsked() {
        return questionsAsked;
    }

    public void setQuestionsAsked(String questionsAsked) {
        this.questionsAsked = questionsAsked;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
