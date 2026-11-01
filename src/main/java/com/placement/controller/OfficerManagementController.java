package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.*;
import com.placement.repository.*;
import com.placement.service.NotificationService;
import com.placement.service.OfficerManagementService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller handling Placement Officer approvals, student & company directories, and management operations.
 */
@Controller
@RequestMapping("/officer")
public class OfficerManagementController {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final InterviewExperienceRepository interviewExperienceRepository;
    private final OfficerManagementService officerManagementService;

    public OfficerManagementController(CompanyRepository companyRepository,
                                       JobRepository jobRepository,
                                       StudentProfileRepository studentProfileRepository,
                                       UserRepository userRepository,
                                       ApplicationRepository applicationRepository,
                                       AnnouncementRepository announcementRepository,
                                       NotificationRepository notificationRepository,
                                       NotificationService notificationService,
                                       InterviewExperienceRepository interviewExperienceRepository,
                                       OfficerManagementService officerManagementService) {
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.interviewExperienceRepository = interviewExperienceRepository;
        this.officerManagementService = officerManagementService;
    }

    /**
     * View pending companies awaiting approval.
     */
    @GetMapping("/approvals/companies")
    public String viewPendingCompanies(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Company> pendingCompanies = companyRepository.findByStatus(CompanyStatus.PENDING);
        model.addAttribute("companies", pendingCompanies);
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/pending-companies";
    }

    /**
     * Approve a company registration.
     */
    @PostMapping("/approvals/companies/{id}/approve")
    public String approveCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));

        company.setStatus(CompanyStatus.APPROVED);
        companyRepository.save(company);

        // Notify the company user
        notificationService.createNotification(company.getUser(),
                "Congratulations! Your company registration for '" + company.getName() + "' has been APPROVED by the Placement Cell. You may now post job drives.");

        redirectAttributes.addFlashAttribute("successMessage", "Company '" + company.getName() + "' has been approved successfully!");
        return "redirect:/officer/approvals/companies";
    }

    /**
     * Reject a company registration.
     */
    @PostMapping("/approvals/companies/{id}/reject")
    public String rejectCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));

        company.setStatus(CompanyStatus.REJECTED);
        companyRepository.save(company);

        // Notify the company user
        notificationService.createNotification(company.getUser(),
                "Your company registration for '" + company.getName() + "' was reviewed and rejected by the Placement Cell.");

        redirectAttributes.addFlashAttribute("errorMessage", "Company '" + company.getName() + "' has been rejected.");
        return "redirect:/officer/approvals/companies";
    }

    /**
     * View pending job postings awaiting approval.
     */
    @GetMapping("/approvals/jobs")
    public String viewPendingJobs(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Job> pendingJobs = jobRepository.findByStatus(JobStatus.PENDING);
        model.addAttribute("jobs", pendingJobs);
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/pending-jobs";
    }

    /**
     * Approve a job posting.
     */
    @PostMapping("/approvals/jobs/{id}/approve")
    public String approveJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        job.setStatus(JobStatus.APPROVED);
        jobRepository.save(job);

        // Notify the company user
        notificationService.createNotification(job.getCompany().getUser(),
                "Your job posting '" + job.getTitle() + "' has been APPROVED by the Placement Cell and is now live for eligible students!");

        redirectAttributes.addFlashAttribute("successMessage", "Job drive '" + job.getTitle() + "' has been approved and is now active!");
        return "redirect:/officer/approvals/jobs";
    }

    /**
     * Reject a job posting.
     */
    @PostMapping("/approvals/jobs/{id}/reject")
    public String rejectJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        job.setStatus(JobStatus.REJECTED);
        jobRepository.save(job);

        // Notify the company user
        notificationService.createNotification(job.getCompany().getUser(),
                "Your job posting '" + job.getTitle() + "' was rejected by the Placement Cell. Please contact the cell for feedback.");

        redirectAttributes.addFlashAttribute("errorMessage", "Job drive '" + job.getTitle() + "' has been rejected.");
        return "redirect:/officer/approvals/jobs";
    }

    /**
     * All students directory with block/unblock capability.
     */
    @GetMapping("/students")
    public String listAllStudents(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<StudentProfile> students = studentProfileRepository.findAll();
        model.addAttribute("students", students);
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/students";
    }

    /**
     * Toggle active/blocked status for a student user.
     */
    @PostMapping("/students/{id}/toggle-status")
    public String toggleStudentStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        User user = student.getUser();
        user.setActive(!user.isActive());
        userRepository.save(user);

        String statusStr = user.isActive() ? "unblocked (active)" : "blocked (deactivated)";
        redirectAttributes.addFlashAttribute("successMessage", "Student " + student.getName() + " is now " + statusStr + ".");
        return "redirect:/officer/students";
    }

    /**
     * Permanently remove a student (e.g. for misconduct, fraud, or fake profile).
     */
    @PostMapping("/students/{id}/delete")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String studentName = officerManagementService.removeStudent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Student '" + studentName + "' has been permanently removed from the portal.");
        return "redirect:/officer/students";
    }

    /**
     * All companies directory with block/unblock capability.
     */
    @GetMapping("/companies")
    public String listAllCompanies(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Company> companies = companyRepository.findAllByOrderByNameAsc();
        model.addAttribute("companies", companies);
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/companies";
    }

    /**
     * Toggle active/blocked status for a company user.
     */
    @PostMapping("/companies/{id}/toggle-status")
    public String toggleCompanyStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));

        User user = company.getUser();
        user.setActive(!user.isActive());
        userRepository.save(user);

        String statusStr = user.isActive() ? "unblocked (active)" : "blocked (deactivated)";
        redirectAttributes.addFlashAttribute("successMessage", "Company " + company.getName() + " user account is now " + statusStr + ".");
        return "redirect:/officer/companies";
    }

    /**
     * Permanently remove a company (e.g. for fraud, scam, or disqualification).
     */
    @PostMapping("/companies/{id}/delete")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String companyName = officerManagementService.removeCompany(id);
        redirectAttributes.addFlashAttribute("successMessage", "Company '" + companyName + "' and all associated job drives have been permanently removed from the portal.");
        return "redirect:/officer/companies";
    }

    /**
     * Announcements management view.
     */
    @GetMapping("/announcements")
    public String listAnnouncements(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("announcements", announcements);
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/announcements";
    }

    /**
     * Post a new announcement.
     */
    @PostMapping("/announcements")
    public String postAnnouncement(@RequestParam String title,
                                   @RequestParam String message,
                                   RedirectAttributes redirectAttributes) {
        if (title != null && !title.trim().isEmpty() && message != null && !message.trim().isEmpty()) {
            Announcement announcement = new Announcement(title.trim(), message.trim());
            announcementRepository.save(announcement);
            redirectAttributes.addFlashAttribute("successMessage", "New announcement broadcast successfully!");
        }
        return "redirect:/officer/announcements";
    }

    /**
     * Remove an existing announcement that is timed out or obsolete.
     */
    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found with ID: " + id));

        String title = announcement.getTitle();
        announcementRepository.delete(announcement);
        redirectAttributes.addFlashAttribute("successMessage", "Announcement '" + title + "' has been removed successfully.");
        return "redirect:/officer/announcements";
    }

    /**
     * Reports and export center.
     */
    @GetMapping("/reports")
    public String reportsOverview(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("totalStudents", studentProfileRepository.count());
        model.addAttribute("placedStudents", studentProfileRepository.countByPlacedTrue());
        model.addAttribute("totalApplications", applicationRepository.count());
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/reports";
    }

    /**
     * Escape CSV values to preserve double quotes and avoid breaking CSV delimiters.
     */
    private String escapeCsv(String val) {
        if (val == null) {
            return "";
        }
        return val.replace("\"", "\"\"");
    }

    /**
     * Format phone number as an Excel text formula to prevent scientific notation conversion (e.g. 9.88E+09).
     */
    private String formatPhoneCsv(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "\"N/A\"";
        }
        String cleaned = phone.trim().replace("\"", "");
        return "=\"" + cleaned + "\"";
    }

    /**
     * Format date as an Excel text formula to prevent Excel cell width overflow (########).
     */
    private String formatDateCsv(java.time.LocalDate date) {
        if (date == null) {
            return "\"N/A\"";
        }
        return "=\"" + date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "\"";
    }

    /**
     * Format datetime as an Excel text formula to prevent nanosecond overflow or truncation.
     */
    private String formatDateTimeCsv(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "\"N/A\"";
        }
        return "=\"" + dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a")) + "\"";
    }

    /**
     * Export Placed Students as CSV.
     */
    @GetMapping("/reports/export/placed")
    public void exportPlacedStudentsCsv(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"placed_students.csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.print("\uFEFF"); // Write UTF-8 BOM so Excel opens with proper UTF-8 character encoding
        writer.println("Student ID,Full Name,Department,College Email,Contact Number,CGPA,Graduation Year,Hiring Company,Job Role,Package (LPA),Offers Count,All Placed Offers,Placed Status");

        List<StudentProfile> placedStudents = studentProfileRepository.findByPlacedTrue();
        for (StudentProfile s : placedStudents) {
            List<Application> studentApps = applicationRepository.findByStudentOrderByAppliedDateDesc(s);
            List<Application> placedOffers = studentApps.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.PLACED || a.getStatus() == ApplicationStatus.SELECTED)
                    .sorted((a1, a2) -> Double.compare(a2.getJob().getPackageLpa(), a1.getJob().getPackageLpa()))
                    .toList();

            String hiringCompany;
            String jobRole;
            String packageLpa;
            int offersCount = placedOffers.size();
            String allOffersStr;

            if (!placedOffers.isEmpty()) {
                Application topOffer = placedOffers.get(0);
                hiringCompany = topOffer.getJob().getCompany().getName();
                jobRole = topOffer.getJob().getTitle();
                packageLpa = String.format("%.2f", topOffer.getJob().getPackageLpa());
                allOffersStr = placedOffers.stream()
                        .map(a -> a.getJob().getCompany().getName() + " (" + String.format("%.1f", a.getJob().getPackageLpa()) + " LPA)")
                        .collect(java.util.stream.Collectors.joining("; "));
            } else {
                hiringCompany = "Direct / Off-Campus Placement";
                jobRole = "Graduate Trainee";
                packageLpa = "N/A";
                offersCount = 1;
                allOffersStr = "Direct / Off-Campus Placement";
            }

            writer.printf("%d,\"%s\",\"%s\",\"%s\",%s,%.2f,%d,\"%s\",\"%s\",%s,%d,\"%s\",%s\n",
                    s.getId(),
                    escapeCsv(s.getName()),
                    escapeCsv(s.getDepartment()),
                    escapeCsv(s.getCollegeEmail()),
                    formatPhoneCsv(s.getContactNumber()),
                    s.getCgpa(),
                    s.getPassingYear(),
                    escapeCsv(hiringCompany),
                    escapeCsv(jobRole),
                    packageLpa.equals("N/A") ? "\"N/A\"" : packageLpa,
                    offersCount,
                    escapeCsv(allOffersStr),
                    s.isPlaced() ? "PLACED" : "UNPLACED");
        }
        writer.flush();
    }

    /**
     * Export All Applications as CSV.
     */
    @GetMapping("/reports/export/applications")
    public void exportAllApplicationsCsv(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"all_applications.csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.print("\uFEFF"); // Write UTF-8 BOM so Excel opens with proper UTF-8 character encoding
        writer.println("Application ID,Candidate Name,Department,College Email,Contact Number,CGPA,Job Title,Company Name,Package (LPA),Status,Applied Date,Interview Date,Interview Time,Interview Venue");

        List<Application> applications = applicationRepository.findAll();
        for (Application a : applications) {
            writer.printf("%d,\"%s\",\"%s\",\"%s\",%s,%.2f,\"%s\",\"%s\",%.1f,\"%s\",%s,%s,\"%s\",\"%s\"\n",
                    a.getId(),
                    escapeCsv(a.getStudent().getName()),
                    escapeCsv(a.getStudent().getDepartment()),
                    escapeCsv(a.getStudent().getCollegeEmail()),
                    formatPhoneCsv(a.getStudent().getContactNumber()),
                    a.getStudent().getCgpa(),
                    escapeCsv(a.getJob().getTitle()),
                    escapeCsv(a.getJob().getCompany().getName()),
                    a.getJob().getPackageLpa(),
                    a.getStatus().name(),
                    formatDateTimeCsv(a.getAppliedDate()),
                    formatDateCsv(a.getInterviewDate()),
                    a.getInterviewTime() != null ? escapeCsv(a.getInterviewTime()) : "N/A",
                    a.getInterviewVenue() != null ? escapeCsv(a.getInterviewVenue()) : "N/A");
        }
        writer.flush();
    }

    /**
     * Mark or unmark a student as Placed.
     */
    @PostMapping("/students/{id}/toggle-placed")
    public String toggleStudentPlaced(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        boolean newPlacedState = !student.isPlaced();
        student.setPlaced(newPlacedState);
        studentProfileRepository.save(student);

        if (newPlacedState) {
            // Check if student has any SELECTED applications and advance them to PLACED
            List<Application> apps = applicationRepository.findByStudentOrderByAppliedDateDesc(student);
            for (Application a : apps) {
                if (a.getStatus() == ApplicationStatus.SELECTED) {
                    a.setStatus(ApplicationStatus.PLACED);
                    applicationRepository.save(a);
                }
            }
            notificationService.createNotification(student.getUser(),
                    "Congratulations! You have been officially marked as PLACED by the Placement Cell!");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student " + student.getName() + " marked as officially PLACED!");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Student " + student.getName() + " placement status updated to Unplaced.");
        }

        return "redirect:/officer/students";
    }

    /**
     * View all student-submitted interview experiences.
     */
    @GetMapping("/experiences")
    public String viewExperiences(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<InterviewExperience> experiences = interviewExperienceRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("experiences", experiences);
        if (userDetails != null) {
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(userDetails.getUser()));
        }
        return "officer/experiences";
    }

    /**
     * Delete an inappropriate or duplicate interview experience.
     */
    @PostMapping("/experiences/{id}/delete")
    public String deleteExperience(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        interviewExperienceRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Interview experience post removed successfully.");
        return "redirect:/officer/experiences";
    }
}
