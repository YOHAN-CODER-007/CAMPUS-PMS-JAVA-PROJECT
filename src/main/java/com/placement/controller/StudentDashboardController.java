package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.ApplicationStatus;
import com.placement.model.StudentProfile;
import com.placement.model.User;
import com.placement.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

/**
 * Controller for Student Dashboard and main student portal entry point.
 */
@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentProfileRepository studentProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;

    public StudentDashboardController(StudentProfileRepository studentProfileRepository,
                                      ApplicationRepository applicationRepository,
                                      SavedJobRepository savedJobRepository,
                                      AnnouncementRepository announcementRepository,
                                      NotificationRepository notificationRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Optional<StudentProfile> profileOpt = studentProfileRepository.findByUser(user);

        if (profileOpt.isPresent()) {
            StudentProfile profile = profileOpt.get();
            model.addAttribute("profile", profile);
            model.addAttribute("completeness", profile.getProfileCompleteness());

            // Key metrics for the student
            long appliedCount = applicationRepository.countByStudent(profile);
            long shortlistedCount = applicationRepository.countByStudentAndStatus(profile, ApplicationStatus.SHORTLISTED);
            long interviewCount = applicationRepository.countByStudentAndStatus(profile, ApplicationStatus.INTERVIEW_SCHEDULED);
            long selectedCount = applicationRepository.countByStudentAndStatus(profile, ApplicationStatus.SELECTED)
                    + applicationRepository.countByStudentAndStatus(profile, ApplicationStatus.PLACED);
            long savedCount = savedJobRepository.countByStudent(profile);

            model.addAttribute("appliedCount", appliedCount);
            model.addAttribute("shortlistedCount", shortlistedCount);
            model.addAttribute("interviewCount", interviewCount);
            model.addAttribute("selectedCount", selectedCount);
            model.addAttribute("savedCount", savedCount);
        } else {
            model.addAttribute("completeness", 0);
            model.addAttribute("appliedCount", 0);
            model.addAttribute("shortlistedCount", 0);
            model.addAttribute("interviewCount", 0);
            model.addAttribute("selectedCount", 0);
            model.addAttribute("savedCount", 0);
        }

        // Latest announcements
        model.addAttribute("announcements", announcementRepository.findTop5ByOrderByCreatedAtDesc());
        // Unread notifications count
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));

        return "student/dashboard";
    }
}
