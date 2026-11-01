package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.*;
import com.placement.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Controller for Officer (Admin) Dashboard.
 */
@Controller
@RequestMapping("/officer")
public class OfficerDashboardController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;

    public OfficerDashboardController(UserRepository userRepository,
                                    StudentProfileRepository studentProfileRepository,
                                    CompanyRepository companyRepository,
                                    JobRepository jobRepository,
                                    ApplicationRepository applicationRepository,
                                    NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        long totalStudents = userRepository.countByRole(Role.ROLE_STUDENT);
        long placedStudents = studentProfileRepository.countByPlacedTrue();
        long totalCompanies = companyRepository.count();
        long pendingCompanies = companyRepository.countByStatus(CompanyStatus.PENDING);
        long approvedCompanies = companyRepository.countByStatus(CompanyStatus.APPROVED);

        long totalJobs = jobRepository.count();
        long pendingJobs = jobRepository.countByStatus(JobStatus.PENDING);
        long approvedJobs = jobRepository.countByStatus(JobStatus.APPROVED);

        long totalApplications = applicationRepository.count();

        // Calculate placement percentage
        double placementPercentage = 0.0;
        if (totalStudents > 0) {
            placementPercentage = BigDecimal.valueOf(((double) placedStudents / totalStudents) * 100.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // Pending lists for quick review
        List<Company> pendingCompanyList = companyRepository.findByStatus(CompanyStatus.PENDING);
        List<Job> pendingJobList = jobRepository.findByStatus(JobStatus.PENDING);

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("placedStudents", placedStudents);
        model.addAttribute("placementPercentage", placementPercentage);
        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("pendingCompanies", pendingCompanies);
        model.addAttribute("approvedCompanies", approvedCompanies);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("pendingJobs", pendingJobs);
        model.addAttribute("approvedJobs", approvedJobs);
        model.addAttribute("totalApplications", totalApplications);

        model.addAttribute("pendingCompanyList", pendingCompanyList);
        model.addAttribute("pendingJobList", pendingJobList);

        // --- Chart 1: Department-wise Placement Comparison ---
        java.util.List<Object[]> totalByDept = studentProfileRepository.countTotalStudentsByDepartment();
        java.util.List<Object[]> placedByDept = studentProfileRepository.countPlacedStudentsByDepartment();
        java.util.Map<String, Long> totalMap = new java.util.LinkedHashMap<>();
        for (Object[] row : totalByDept) {
            totalMap.put((String) row[0], (Long) row[1]);
        }
        java.util.Map<String, Long> placedMap = new java.util.HashMap<>();
        for (Object[] row : placedByDept) {
            placedMap.put((String) row[0], (Long) row[1]);
        }
        java.util.List<String> deptLabels = new java.util.ArrayList<>(totalMap.keySet());
        java.util.List<Long> deptTotalCounts = new java.util.ArrayList<>();
        java.util.List<Long> deptPlacedCounts = new java.util.ArrayList<>();
        for (String dept : deptLabels) {
            deptTotalCounts.add(totalMap.getOrDefault(dept, 0L));
            deptPlacedCounts.add(placedMap.getOrDefault(dept, 0L));
        }
        model.addAttribute("deptLabels", deptLabels);
        model.addAttribute("deptTotalCounts", deptTotalCounts);
        model.addAttribute("deptPlacedCounts", deptPlacedCounts);

        // --- Chart 2: Selections by Recruiting Company ---
        java.util.List<Object[]> companySelections = applicationRepository.countSelectionsByCompany();
        java.util.List<String> companyLabels = new java.util.ArrayList<>();
        java.util.List<Long> companySelectCounts = new java.util.ArrayList<>();
        for (Object[] row : companySelections) {
            companyLabels.add((String) row[0]);
            companySelectCounts.add((Long) row[1]);
        }
        model.addAttribute("companyLabels", companyLabels);
        model.addAttribute("companySelectCounts", companySelectCounts);

        // --- Chart 3: Salary Package Distribution ---
        java.util.List<Job> allApprovedJobs = jobRepository.findByStatus(JobStatus.APPROVED);
        long below5 = allApprovedJobs.stream().filter(j -> j.getPackageLpa() < 5.0).count();
        long fiveToTen = allApprovedJobs.stream().filter(j -> j.getPackageLpa() >= 5.0 && j.getPackageLpa() < 10.0).count();
        long tenToFifteen = allApprovedJobs.stream().filter(j -> j.getPackageLpa() >= 10.0 && j.getPackageLpa() < 15.0).count();
        long aboveFifteen = allApprovedJobs.stream().filter(j -> j.getPackageLpa() >= 15.0).count();
        model.addAttribute("packageDistribution", java.util.List.of(below5, fiveToTen, tenToFifteen, aboveFifteen));

        // --- Chart 4: Application Recruitment Pipeline Breakdown ---
        java.util.List<Object[]> statusCounts = applicationRepository.countApplicationsByStatus();
        java.util.Map<String, Long> statusMap = new java.util.HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put(((ApplicationStatus) row[0]).name(), (Long) row[1]);
        }
        java.util.List<String> pipelineLabels = java.util.List.of("Applied", "Shortlisted", "Interview", "Selected", "Placed", "Rejected");
        java.util.List<Long> pipelineCounts = java.util.List.of(
                statusMap.getOrDefault("APPLIED", 0L),
                statusMap.getOrDefault("SHORTLISTED", 0L),
                statusMap.getOrDefault("INTERVIEW_SCHEDULED", 0L),
                statusMap.getOrDefault("SELECTED", 0L),
                statusMap.getOrDefault("PLACED", 0L),
                statusMap.getOrDefault("REJECTED", 0L)
        );
        model.addAttribute("pipelineLabels", pipelineLabels);
        model.addAttribute("pipelineCounts", pipelineCounts);

        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }

        return "officer/dashboard";
    }
}
