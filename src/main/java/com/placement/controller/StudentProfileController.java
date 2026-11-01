package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.StudentProfile;
import com.placement.model.User;
import com.placement.repository.NotificationRepository;
import com.placement.repository.StudentProfileRepository;
import com.placement.service.FileStorageService;
import com.placement.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Controller handling Student Profile management, updates, and resume uploads.
 */
@Controller
@RequestMapping("/student")
public class StudentProfileController {

    private final StudentProfileRepository studentProfileRepository;
    private final FileStorageService fileStorageService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public StudentProfileController(StudentProfileRepository studentProfileRepository,
                                    FileStorageService fileStorageService,
                                    NotificationRepository notificationRepository,
                                    NotificationService notificationService) {
        this.studentProfileRepository = studentProfileRepository;
        this.fileStorageService = fileStorageService;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Display the Student Profile view and edit form.
     */
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    StudentProfile p = new StudentProfile();
                    p.setUser(user);
                    return p;
                });

        model.addAttribute("profile", profile);
        model.addAttribute("completeness", profile.getProfileCompleteness());
        model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
        return "student/profile";
    }

    /**
     * Process student profile update and resume PDF upload.
     */
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profile") StudentProfile formProfile,
                                BindingResult bindingResult,
                                @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = userDetails.getUser();
        StudentProfile existing = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for user: " + user.getEmail()));

        if (bindingResult.hasErrors()) {
            model.addAttribute("completeness", existing.getProfileCompleteness());
            model.addAttribute("unreadNotifications", notificationRepository.countByUserAndIsReadFalse(user));
            return "student/profile";
        }

        // Update profile fields
        existing.setName(formProfile.getName().trim());
        existing.setDateOfBirth(formProfile.getDateOfBirth());
        existing.setDepartment(formProfile.getDepartment().trim());
        existing.setCollegeEmail(formProfile.getCollegeEmail().trim());
        existing.setContactNumber(formProfile.getContactNumber().trim());
        existing.setCgpa(formProfile.getCgpa());
        existing.setTotalArrears(formProfile.getTotalArrears() != null ? formProfile.getTotalArrears() : 0);
        existing.setPassingYear(formProfile.getPassingYear());
        existing.setSkills(formProfile.getSkills() != null ? formProfile.getSkills().trim() : "");

        // Handle resume upload if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                String savedFilename = fileStorageService.storeResume(resumeFile, existing.getId());
                existing.setResumePath(savedFilename);
            } catch (IllegalArgumentException | IOException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/student/profile";
            }
        }

        studentProfileRepository.save(existing);

        // Notify Placement Officer admin about the student profile changes
        notificationService.notifyOfficers("Profile Update: Student " + existing.getName() + " (" + existing.getDepartment() + ", CGPA: " + existing.getCgpa() + ") updated their profile details.");

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully! Profile completeness is now " + existing.getProfileCompleteness() + "%.");
        return "redirect:/student/profile";
    }

    /**
     * Download or view the uploaded PDF resume.
     * Accessible by Student, Company recruiters, and Placement Officers.
     */
    @GetMapping("/resume/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadResume(@PathVariable String filename) {
        try {
            Resource file = fileStorageService.loadResumeAsResource(filename);
            if (file != null && file.exists() && file.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            }
        } catch (Exception e) {
            // file missing or unreadable
        }
        return ResponseEntity.notFound().build();
    }
}
