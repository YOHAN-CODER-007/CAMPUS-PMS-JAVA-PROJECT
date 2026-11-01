package com.placement.service;

import com.placement.model.*;
import com.placement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service providing administrative removal capabilities for Placement Officers
 * (e.g., removing fraudulent companies or misconducting students).
 */
@Service
@Transactional
public class OfficerManagementService {

    private static final Logger logger = LoggerFactory.getLogger(OfficerManagementService.class);

    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final InterviewExperienceRepository interviewExperienceRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public OfficerManagementService(StudentProfileRepository studentProfileRepository,
                                  CompanyRepository companyRepository,
                                  JobRepository jobRepository,
                                  ApplicationRepository applicationRepository,
                                  SavedJobRepository savedJobRepository,
                                  InterviewExperienceRepository interviewExperienceRepository,
                                  NotificationRepository notificationRepository,
                                  UserRepository userRepository,
                                  FileStorageService fileStorageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.interviewExperienceRepository = interviewExperienceRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Completely removes a student, their applications, bookmarks, interview experiences, notifications, resume file, and user account.
     */
    public String removeStudent(Long studentId) {
        StudentProfile student = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        String studentName = student.getName();
        User user = student.getUser();

        // 1. Delete resume file if exists
        if (student.getResumePath() != null && !student.getResumePath().trim().isEmpty()) {
            fileStorageService.deleteResume(student.getResumePath());
        }

        // 2. Delete child records
        savedJobRepository.deleteByStudent(student);
        interviewExperienceRepository.deleteByStudent(student);
        applicationRepository.deleteByStudent(student);

        if (user != null) {
            notificationRepository.deleteByUser(user);
        }

        // 3. Delete profile and user
        studentProfileRepository.delete(student);

        if (user != null) {
            userRepository.delete(user);
        }

        logger.info("Admin permanently removed student: ID={}, Name={}", studentId, studentName);
        return studentName;
    }

    /**
     * Completely removes a company (e.g. fraud or blacklisted company), all its job drives, candidate applications,
     * bookmarks, interview reviews, notifications, and user account.
     */
    public String removeCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));

        String companyName = company.getName();
        User user = company.getUser();

        // 1. Clean up all jobs posted by this company and their dependent records
        List<Job> jobs = jobRepository.findByCompany(company);
        for (Job job : jobs) {
            applicationRepository.deleteByJob(job);
            savedJobRepository.deleteByJob(job);
        }
        jobRepository.deleteByCompany(company);

        // 2. Clean up interview experiences associated with this company
        interviewExperienceRepository.deleteByCompany(company);

        // 3. Clean up company user notifications
        if (user != null) {
            notificationRepository.deleteByUser(user);
        }

        // 4. Delete company entity
        companyRepository.delete(company);

        // 5. Delete company user account
        if (user != null) {
            userRepository.delete(user);
        }

        logger.info("Admin permanently removed company: ID={}, Name={}", companyId, companyName);
        return companyName;
    }
}
