package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.*;
import com.placement.repository.ApplicationRepository;
import com.placement.repository.CompanyRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.NotificationRepository;
import com.placement.repository.SavedJobRepository;
import com.placement.repository.StudentProfileRepository;
import com.placement.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Controller handling Company profile, job posting, job management, and candidate review.
 */
@Controller
@RequestMapping("/company")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final StudentProfileRepository studentProfileRepository;

    public CompanyController(CompanyRepository companyRepository,
                             JobRepository jobRepository,
                             ApplicationRepository applicationRepository,
                             SavedJobRepository savedJobRepository,
                             NotificationRepository notificationRepository,
                             NotificationService notificationService,
                             StudentProfileRepository studentProfileRepository) {
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.studentProfileRepository = studentProfileRepository;
    }

    /**
     * View and edit Company Profile.
     */
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for user: " + user.getEmail()));

        model.addAttribute("company", company);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "company/profile";
    }

    /**
     * Process company profile updates.
     */
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("company") Company formCompany,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = userDetails.getUser();
        Company existing = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for user: " + user.getEmail()));

        if (bindingResult.hasErrors()) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
            return "company/profile";
        }

        existing.setName(formCompany.getName().trim());
        existing.setLocation(formCompany.getLocation().trim());
        existing.setWebsite(formCompany.getWebsite() != null ? formCompany.getWebsite().trim() : "");
        existing.setDescription(formCompany.getDescription() != null ? formCompany.getDescription().trim() : "");
        existing.setContactEmail(formCompany.getContactEmail().trim());
        existing.setContactNumber(formCompany.getContactNumber().trim());

        companyRepository.save(existing);

        // Notify Placement Officer admin about the company profile changes
        notificationService.notifyOfficers("Profile Update: Company '" + existing.getName() + "' (Location: " + existing.getLocation() + ") updated their profile details.");

        redirectAttributes.addFlashAttribute("successMessage", "Company profile updated successfully!");
        return "redirect:/company/profile";
    }

    /**
     * List all jobs posted by this company.
     */
    @GetMapping("/jobs")
    public String listJobs(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        List<Job> jobs = jobRepository.findByCompanyOrderByCreatedAtDesc(company);
        model.addAttribute("jobs", jobs);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "company/jobs";
    }

    /**
     * Show form to post a new job opening.
     */
    @GetMapping("/jobs/create")
    public String showCreateJobForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (!model.containsAttribute("job")) {
            Job job = new Job();
            job.setCompany(company);
            model.addAttribute("job", job);
        }

        model.addAttribute("jobTypes", JobType.values());
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "company/post-job";
    }

    /**
     * Process new job opening submission.
     */
    @PostMapping("/jobs/create")
    public String processCreateJob(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @Valid @ModelAttribute("job") Job job,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (job.getDeadline() != null && !job.getDeadline().isAfter(LocalDate.now())) {
            bindingResult.rejectValue("deadline", "error.deadline", "Deadline must be a future date");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("jobTypes", JobType.values());
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
            return "company/post-job";
        }

        job.setCompany(company);
        job.setStatus(JobStatus.PENDING); // Awaiting Officer approval per spec

        Job savedJob = jobRepository.save(job);

        // Send confirmation notification
        notificationService.createNotification(user,
                "Job posting '" + savedJob.getTitle() + "' has been submitted and is currently pending Placement Officer approval.");

        redirectAttributes.addFlashAttribute("successMessage",
                "Job posting submitted successfully! It is currently PENDING approval from the Placement Officer.");
        return "redirect:/company/jobs";
    }

    /**
     * Remove an existing job opening posted by this company (e.g. posted by mistake).
     */
    @PostMapping("/jobs/{id}/delete")
    @Transactional
    public String deleteJob(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for user: " + user.getEmail()));

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));

        // Security check: ensure this job belongs to the authenticated company
        if (!job.getCompany().getId().equals(company.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: You can only remove jobs posted by your own company.");
            return "redirect:/company/jobs";
        }

        String jobTitle = job.getTitle();

        // 1. Delete candidate applications received for this job
        applicationRepository.deleteByJob(job);

        // 2. Delete student bookmarks / saved jobs for this job
        savedJobRepository.deleteByJob(job);

        // 3. Delete the job listing
        jobRepository.delete(job);

        redirectAttributes.addFlashAttribute("successMessage", "Job opening '" + jobTitle + "' has been successfully removed.");
        return "redirect:/company/jobs";
    }

    /**
     * View applicants across all company job openings.
     */
    @GetMapping("/applicants")
    public String viewAllApplicants(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        List<Job> jobs = jobRepository.findByCompany(company);
        List<Application> applications = jobs.stream()
                .flatMap(j -> applicationRepository.findByJobOrderByAppliedDateDesc(j).stream())
                .toList();

        model.addAttribute("applications", applications);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "company/applicants";
    }

    /**
     * View applicants for a specific job opening.
     */
    @GetMapping("/jobs/{jobId}/applicants")
    public String viewJobApplicants(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Long jobId,
                                    Model model) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        // Verify that this job belongs to the authenticated company
        if (!job.getCompany().getId().equals(company.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized access to job candidates");
        }

        List<Application> applications = applicationRepository.findByJobOrderByAppliedDateDesc(job);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "company/applicants";
    }

    /**
     * Update candidate application recruitment status (e.g. SHORTLISTED, SELECTED, REJECTED).
     */
    @PostMapping("/applications/{id}/status")
    public String updateApplicationStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable Long id,
                                          @RequestParam ApplicationStatus status,
                                          @RequestParam(value = "jobId", required = false) Long jobId,
                                          RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));

        // Verify company owns this job
        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to modify this application");
        }

        application.setStatus(status);
        applicationRepository.save(application);

        // Synchronize student placed status if placed by company
        StudentProfile student = application.getStudent();
        if (status == ApplicationStatus.PLACED) {
            if (!student.isPlaced()) {
                student.setPlaced(true);
                studentProfileRepository.save(student);
            }
        }

        // Send alert notification to the student
        String studentMsg;
        if (status == ApplicationStatus.SELECTED) {
            studentMsg = "Congratulations! You have been SELECTED for the '" + application.getJob().getTitle() + "' drive at " + company.getName() + "!";
        } else if (status == ApplicationStatus.SHORTLISTED) {
            studentMsg = "Great news! Your profile has been SHORTLISTED for '" + application.getJob().getTitle() + "' at " + company.getName() + ".";
        } else if (status == ApplicationStatus.REJECTED) {
            studentMsg = "Update regarding your application for '" + application.getJob().getTitle() + "' at " + company.getName() + ": We regret to inform you that your profile was not selected.";
        } else {
            studentMsg = "Your application status for '" + application.getJob().getTitle() + "' at " + company.getName() + " has been updated to: " + status.getDisplayName();
        }

        notificationService.createNotification(application.getStudent().getUser(), studentMsg);

        redirectAttributes.addFlashAttribute("successMessage",
                "Candidate status updated to " + status.getDisplayName() + " successfully.");

        if (jobId != null) {
            return "redirect:/company/jobs/" + jobId + "/applicants";
        }
        return "redirect:/company/applicants";
    }

    /**
     * Schedule an interview round for an applicant candidate.
     */
    @PostMapping("/applications/{id}/schedule")
    public String scheduleInterview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Long id,
                                    @RequestParam(name = "interviewDate") String interviewDateStr,
                                    @RequestParam String interviewTime,
                                    @RequestParam String interviewVenue,
                                    @RequestParam(value = "jobId", required = false) Long jobId,
                                    RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));

        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to schedule this interview");
        }

        LocalDate interviewDate = parseDateSafely(interviewDateStr);

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        application.setInterviewDate(interviewDate);
        application.setInterviewTime(interviewTime != null ? interviewTime.trim() : "");
        application.setInterviewVenue(interviewVenue != null ? interviewVenue.trim() : "");
        applicationRepository.save(application);

        // Notify student with full details
        String alertText = String.format("Interview Scheduled! Your interview for '%s' at %s is set for %s at %s. Venue/Mode: %s.",
                application.getJob().getTitle(), company.getName(), interviewDate, application.getInterviewTime(), application.getInterviewVenue());
        notificationService.createNotification(application.getStudent().getUser(), alertText);

        redirectAttributes.addFlashAttribute("successMessage",
                "Interview scheduled successfully for " + application.getStudent().getName() + " on " + interviewDate + ".");

        if (jobId != null) {
            return "redirect:/company/jobs/" + jobId + "/applicants";
        }
        return "redirect:/company/applicants";
    }

    private LocalDate parseDateSafely(String s) {
        if (s == null || s.trim().isEmpty()) {
            return LocalDate.now().plusDays(1);
        }
        String clean = s.trim();
        List<java.time.format.DateTimeFormatter> formatters = Arrays.asList(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        );
        for (java.time.format.DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(clean, fmt);
            } catch (Exception ignored) {
            }
        }
        return LocalDate.now().plusDays(1);
    }

    /**
     * Export all applicant candidates for a company's job as CSV.
     */
    @GetMapping("/jobs/{jobId}/export-csv")
    public void exportJobApplicantsCsv(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long jobId,
                                       jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        User user = userDetails.getUser();
        Company company = companyRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.getCompany().getId().equals(company.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to export this job's applicants");
        }

        response.setContentType("text/csv");
        String safeTitle = job.getTitle().replaceAll("[^a-zA-Z0-9_-]", "_");
        response.setHeader("Content-Disposition", "attachment; filename=\"applicants_" + safeTitle + ".csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.println("Application ID,Candidate Name,Department,College Email,Contact Number,CGPA,Arrears,Status,Applied Date,Interview Date,Interview Time,Interview Venue");

        List<Application> applications = applicationRepository.findByJobOrderByAppliedDateDesc(job);
        for (Application a : applications) {
            StudentProfile s = a.getStudent();
            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    a.getId(),
                    s.getName(),
                    s.getDepartment(),
                    s.getCollegeEmail(),
                    s.getContactNumber(),
                    s.getCgpa(),
                    s.getTotalArrears(),
                    a.getStatus().name(),
                    a.getAppliedDate().toString(),
                    a.getInterviewDate() != null ? a.getInterviewDate().toString() : "N/A",
                    a.getInterviewTime() != null ? a.getInterviewTime() : "N/A",
                    a.getInterviewVenue() != null ? a.getInterviewVenue() : "N/A");
        }
        writer.flush();
    }
}
