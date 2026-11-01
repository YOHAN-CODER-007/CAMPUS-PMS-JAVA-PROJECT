package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.dto.EligibilityResult;
import com.placement.model.*;
import com.placement.repository.*;
import com.placement.service.EligibilityService;
import com.placement.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller handling Student Job search with multi-filters, Skill Match %,
 * Automated Eligibility validation, application submission, saved jobs, and interview experiences.
 */
@Controller
@RequestMapping("/student")
public class StudentJobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final InterviewExperienceRepository interviewExperienceRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EligibilityService eligibilityService;

    public StudentJobController(JobRepository jobRepository,
                                ApplicationRepository applicationRepository,
                                SavedJobRepository savedJobRepository,
                                StudentProfileRepository studentProfileRepository,
                                CompanyRepository companyRepository,
                                InterviewExperienceRepository interviewExperienceRepository,
                                NotificationRepository notificationRepository,
                                NotificationService notificationService,
                                EligibilityService eligibilityService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.interviewExperienceRepository = interviewExperienceRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.eligibilityService = eligibilityService;
    }

    /**
     * Browse approved jobs with search, multi-filters, and sorting.
     */
    @GetMapping("/jobs")
    public String browseJobs(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "minPackage", required = false) Double minPackage,
                             @RequestParam(value = "skill", required = false) String skill,
                             @RequestParam(value = "jobType", required = false) String jobType,
                             @RequestParam(value = "onlyEligible", required = false) Boolean onlyEligible,
                             @RequestParam(value = "sort", required = false) String sort,
                             Model model) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);

        List<Job> allApproved = jobRepository.findByStatus(JobStatus.APPROVED);

        // Filter and compute match stats for display
        List<Map<String, Object>> jobCardData = new ArrayList<>();

        for (Job j : allApproved) {
            if (j.isExpired()) continue;

            // Apply keyword filter
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                boolean matchesKeyword = j.getTitle().toLowerCase().contains(kw)
                        || j.getCompany().getName().toLowerCase().contains(kw)
                        || j.getLocation().toLowerCase().contains(kw)
                        || j.getRequiredSkills().toLowerCase().contains(kw);
                if (!matchesKeyword) continue;
            }

            // Location filter
            if (location != null && !location.trim().isEmpty()) {
                if (!j.getLocation().toLowerCase().contains(location.trim().toLowerCase())) {
                    continue;
                }
            }

            // Min package filter
            if (minPackage != null && j.getPackageLpa() < minPackage) {
                continue;
            }

            // Skill filter
            if (skill != null && !skill.trim().isEmpty()) {
                if (!j.getRequiredSkills().toLowerCase().contains(skill.trim().toLowerCase())) {
                    continue;
                }
            }

            // Job Type filter
            if (jobType != null && !jobType.trim().isEmpty() && !"ALL".equalsIgnoreCase(jobType)) {
                if (!j.getJobType().name().equalsIgnoreCase(jobType.trim())) {
                    continue;
                }
            }

            int matchPercent = eligibilityService.calculateSkillMatch(profile, j);
            List<String> missing = eligibilityService.getMissingSkills(profile, j);
            boolean alreadyApplied = profile != null && applicationRepository.existsByStudentAndJob(profile, j);
            EligibilityResult eligResult = eligibilityService.evaluate(profile, j, alreadyApplied);

            // "Only jobs I am eligible for" filter
            if (Boolean.TRUE.equals(onlyEligible) && !eligResult.isCanApply()) {
                continue;
            }

            boolean isSaved = profile != null && savedJobRepository.existsByStudentAndJob(profile, j);

            Map<String, Object> card = new HashMap<>();
            card.put("job", j);
            card.put("skillMatch", matchPercent);
            card.put("missingSkills", missing);
            card.put("eligible", eligResult.isEligible());
            card.put("canApply", eligResult.isCanApply());
            card.put("alreadyApplied", alreadyApplied);
            card.put("isSaved", isSaved);

            jobCardData.add(card);
        }

        // Sorting
        if ("package_desc".equals(sort)) {
            jobCardData.sort((a, b) -> Double.compare(((Job) b.get("job")).getPackageLpa(), ((Job) a.get("job")).getPackageLpa()));
        } else if ("package_asc".equals(sort)) {
            jobCardData.sort((a, b) -> Double.compare(((Job) a.get("job")).getPackageLpa(), ((Job) b.get("job")).getPackageLpa()));
        } else if ("deadline_asc".equals(sort)) {
            jobCardData.sort((a, b) -> ((Job) a.get("job")).getDeadline().compareTo(((Job) b.get("job")).getDeadline()));
        } else {
            // Default newest first
            jobCardData.sort((a, b) -> ((Job) b.get("job")).getCreatedAt().compareTo(((Job) a.get("job")).getCreatedAt()));
        }

        model.addAttribute("jobCards", jobCardData);
        model.addAttribute("profile", profile);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("minPackage", minPackage);
        model.addAttribute("skill", skill);
        model.addAttribute("jobType", jobType);
        model.addAttribute("onlyEligible", onlyEligible);
        model.addAttribute("sort", sort);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "student/jobs";
    }

    /**
     * View detailed job opening with live automated eligibility checklist.
     */
    @GetMapping("/jobs/{id}")
    public String viewJobDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 Model model) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        boolean alreadyApplied = profile != null && applicationRepository.existsByStudentAndJob(profile, job);
        boolean isSaved = profile != null && savedJobRepository.existsByStudentAndJob(profile, job);

        EligibilityResult eligibility = eligibilityService.evaluate(profile, job, alreadyApplied);

        model.addAttribute("job", job);
        model.addAttribute("profile", profile);
        model.addAttribute("eligibility", eligibility);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "student/job-details";
    }

    /**
     * Submit an application for a campus recruitment drive.
     */
    @PostMapping("/jobs/{id}/apply")
    public String applyForJob(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        boolean alreadyApplied = applicationRepository.existsByStudentAndJob(profile, job);
        EligibilityResult eligibility = eligibilityService.evaluate(profile, job, alreadyApplied);

        if (!eligibility.isCanApply()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Application submission failed: You do not satisfy the required eligibility criteria.");
            return "redirect:/student/jobs/" + id;
        }

        // Create application
        Application application = new Application(profile, job);
        applicationRepository.save(application);

        // Notify student
        notificationService.createNotification(user,
                "You have successfully applied for '" + job.getTitle() + "' at " + job.getCompany().getName() + ".");

        // Notify company
        notificationService.createNotification(job.getCompany().getUser(),
                "New candidate application received: " + profile.getName() + " (" + profile.getDepartment() + ", CGPA " + profile.getCgpa() + ") applied for '" + job.getTitle() + "'.");

        redirectAttributes.addFlashAttribute("successMessage",
                "Application submitted successfully for " + job.getTitle() + "! Track your status under 'My Applications'.");
        return "redirect:/student/applications";
    }

    /**
     * Toggle bookmark/saved status for a job drive.
     */
    @PostMapping("/jobs/{id}/toggle-save")
    public String toggleSaveJob(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Long id,
                                @RequestParam(value = "redirect", required = false, defaultValue = "jobs") String redirect,
                                RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        Optional<SavedJob> existingSaved = savedJobRepository.findByStudentAndJob(profile, job);
        if (existingSaved.isPresent()) {
            savedJobRepository.delete(existingSaved.get());
            redirectAttributes.addFlashAttribute("infoMessage", "Job removed from saved bookmarks.");
        } else {
            SavedJob newSaved = new SavedJob(profile, job);
            savedJobRepository.save(newSaved);
            redirectAttributes.addFlashAttribute("successMessage", "Job saved to your bookmarks!");
        }

        if ("details".equals(redirect)) {
            return "redirect:/student/jobs/" + id;
        } else if ("saved".equals(redirect)) {
            return "redirect:/student/saved-jobs";
        }
        return "redirect:/student/jobs";
    }

    /**
     * View all student applications with status tracking.
     */
    @GetMapping("/applications")
    public String myApplications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Optional<StudentProfile> profileOpt = studentProfileRepository.findByUser(user);

        List<Application> applications = Collections.emptyList();
        if (profileOpt.isPresent()) {
            applications = applicationRepository.findByStudentOrderByAppliedDateDesc(profileOpt.get());
        }

        model.addAttribute("applications", applications);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "student/applications";
    }

    /**
     * View saved/bookmarked jobs.
     */
    @GetMapping("/saved-jobs")
    public String savedJobs(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Optional<StudentProfile> profileOpt = studentProfileRepository.findByUser(user);

        List<SavedJob> savedList = Collections.emptyList();
        if (profileOpt.isPresent()) {
            savedList = savedJobRepository.findByStudentOrderBySavedAtDesc(profileOpt.get());
        }

        model.addAttribute("savedJobs", savedList);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "student/saved-jobs";
    }

    /**
     * View interview experiences.
     */
    @GetMapping("/experiences")
    public String interviewExperiences(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user).orElse(null);

        List<InterviewExperience> experiences = interviewExperienceRepository.findAllByOrderByCreatedAtDesc();
        List<Company> approvedCompanies = companyRepository.findByStatus(CompanyStatus.APPROVED);

        model.addAttribute("experiences", experiences);
        model.addAttribute("profile", profile);
        model.addAttribute("companies", approvedCompanies);
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "student/experiences";
    }

    /**
     * Post an interview experience (Allowed for placed students).
     */
    @PostMapping("/experiences")
    public String postExperience(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam Long companyId,
                                 @RequestParam String rounds,
                                 @RequestParam String questionsAsked,
                                 @RequestParam String tips,
                                 RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        if (!profile.isPlaced()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only placed students are eligible to submit company interview experiences.");
            return "redirect:/student/experiences";
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        InterviewExperience exp = new InterviewExperience(profile, company, rounds.trim(), questionsAsked.trim(), tips.trim());
        interviewExperienceRepository.save(exp);

        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you! Your interview experience for " + company.getName() + " has been published.");
        return "redirect:/student/experiences";
    }

    /**
     * Delete an interview experience submitted by the authenticated student.
     */
    @PostMapping("/experiences/{id}/delete")
    @Transactional
    public String deleteExperience(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        InterviewExperience exp = interviewExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interview experience not found: " + id));

        // Security check: ensure the student can only delete their own experience
        if (!exp.getStudent().getId().equals(profile.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: You can only remove your own interview experiences.");
            return "redirect:/student/experiences";
        }

        String companyName = exp.getCompany().getName();
        interviewExperienceRepository.delete(exp);

        redirectAttributes.addFlashAttribute("successMessage",
                "Your interview experience for " + companyName + " has been removed.");
        return "redirect:/student/experiences";
    }

    /**
     * Remove a saved/bookmarked job/company from the student's bookmarked list.
     */
    @PostMapping("/saved-jobs/{id}/delete")
    @Transactional
    public String deleteSavedJob(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        SavedJob saved = savedJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Saved job bookmark not found: " + id));

        // Security check: ensure student can only remove their own bookmark
        if (!saved.getStudent().getId().equals(profile.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: You can only manage your own bookmarks.");
            return "redirect:/student/saved-jobs";
        }

        String companyName = saved.getJob().getCompany().getName();
        String jobTitle = saved.getJob().getTitle();
        savedJobRepository.delete(saved);

        redirectAttributes.addFlashAttribute("successMessage",
                "Removed " + companyName + " (" + jobTitle + ") from your bookmarks.");
        return "redirect:/student/saved-jobs";
    }
}
