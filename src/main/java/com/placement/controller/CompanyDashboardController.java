package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.*;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.CompanyRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.NotificationRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Company Dashboard.
 */
@Controller
@RequestMapping("/company")
public class CompanyDashboardController {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;

    public CompanyDashboardController(CompanyRepository companyRepository,
                                      JobRepository jobRepository,
                                      ApplicationRepository applicationRepository,
                                      NotificationRepository notificationRepository) {
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Optional<Company> companyOpt = companyRepository.findByUser(user);

        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            model.addAttribute("company", company);

            List<Job> jobs = jobRepository.findByCompanyOrderByCreatedAtDesc(company);
            model.addAttribute("jobs", jobs);

            long totalJobs = jobs.size();
            long activeJobs = jobs.stream().filter(j -> j.getStatus() == JobStatus.APPROVED && !j.isExpired()).count();
            long pendingJobs = jobs.stream().filter(j -> j.getStatus() == JobStatus.PENDING).count();

            long totalApplicants = 0;
            java.util.List<String> jobLabels = new java.util.ArrayList<>();
            java.util.List<Long> jobApplicantCounts = new java.util.ArrayList<>();

            long countApplied = 0;
            long countShortlisted = 0;
            long countInterview = 0;
            long countSelected = 0;
            long countRejected = 0;

            for (Job job : jobs) {
                long appCount = applicationRepository.countByJob(job);
                totalApplicants += appCount;
                jobLabels.add(job.getTitle());
                jobApplicantCounts.add(appCount);

                List<Application> apps = applicationRepository.findByJobOrderByAppliedDateDesc(job);
                for (Application a : apps) {
                    if (a.getStatus() == ApplicationStatus.APPLIED) countApplied++;
                    else if (a.getStatus() == ApplicationStatus.SHORTLISTED) countShortlisted++;
                    else if (a.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) countInterview++;
                    else if (a.getStatus() == ApplicationStatus.SELECTED || a.getStatus() == ApplicationStatus.PLACED) countSelected++;
                    else if (a.getStatus() == ApplicationStatus.REJECTED) countRejected++;
                }
            }

            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("activeJobs", activeJobs);
            model.addAttribute("pendingJobs", pendingJobs);
            model.addAttribute("totalApplicants", totalApplicants);

            model.addAttribute("jobLabels", jobLabels);
            model.addAttribute("jobApplicantCounts", jobApplicantCounts);
            model.addAttribute("companyPipelineCounts", java.util.List.of(countApplied, countShortlisted, countInterview, countSelected, countRejected));
        } else {
            model.addAttribute("totalJobs", 0);
            model.addAttribute("activeJobs", 0);
            model.addAttribute("pendingJobs", 0);
            model.addAttribute("totalApplicants", 0);
            model.addAttribute("jobLabels", java.util.Collections.emptyList());
            model.addAttribute("jobApplicantCounts", java.util.Collections.emptyList());
            model.addAttribute("companyPipelineCounts", java.util.List.of(0, 0, 0, 0, 0));
        }

        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "company/dashboard";
    }
}
