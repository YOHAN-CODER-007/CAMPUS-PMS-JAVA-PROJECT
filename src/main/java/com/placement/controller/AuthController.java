package com.placement.controller;

import com.placement.dto.CompanyRegisterDto;
import com.placement.dto.StudentRegisterDto;
import com.placement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling user authentication (login, registration for students and companies).
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Unified login page for Students, Companies, and Officers.
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            switch (error) {
                case "pending":
                    model.addAttribute("pendingMessage", "Your company registration is pending officer approval. Please check back later.");
                    break;
                case "rejected":
                    model.addAttribute("errorMessage", "Your company registration was rejected. Please contact the placement cell.");
                    break;
                case "disabled":
                    model.addAttribute("errorMessage", "Your account has been deactivated. Please contact the administrator.");
                    break;
                default:
                    model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
                    break;
            }
        }

        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        if (registered != null) {
            if ("company".equals(registered)) {
                model.addAttribute("infoMessage", "Company registration submitted successfully! Your account is pending Officer approval before you can log in.");
            } else if ("student".equals(registered)) {
                model.addAttribute("successMessage", "Registration successful! You can now log in with your credentials.");
            }
        }

        return "login";
    }

    /**
     * Student self-registration form.
     */
    @GetMapping("/register/student")
    public String showStudentRegisterForm(Model model) {
        if (!model.containsAttribute("studentDto")) {
            model.addAttribute("studentDto", new StudentRegisterDto());
        }
        return "register-student";
    }

    /**
     * Process student registration.
     */
    @PostMapping("/register/student")
    public String processStudentRegistration(@Valid @ModelAttribute("studentDto") StudentRegisterDto dto,
                                             BindingResult bindingResult,
                                             RedirectAttributes redirectAttributes,
                                             Model model) {
        // Password confirmation check
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.studentDto", "Passwords do not match");
        }

        // Email uniqueness check
        if (dto.getEmail() != null && userService.isEmailTaken(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.studentDto", "An account with this email already exists");
        }

        if (bindingResult.hasErrors()) {
            return "register-student";
        }

        userService.registerStudent(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please log in.");
        return "redirect:/login?registered=student";
    }

    /**
     * Company self-registration form.
     */
    @GetMapping("/register/company")
    public String showCompanyRegisterForm(Model model) {
        if (!model.containsAttribute("companyDto")) {
            model.addAttribute("companyDto", new CompanyRegisterDto());
        }
        return "register-company";
    }

    /**
     * Process company registration.
     */
    @PostMapping("/register/company")
    public String processCompanyRegistration(@Valid @ModelAttribute("companyDto") CompanyRegisterDto dto,
                                             BindingResult bindingResult,
                                             RedirectAttributes redirectAttributes,
                                             Model model) {
        // Password confirmation check
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.companyDto", "Passwords do not match");
        }

        // Email uniqueness check
        if (dto.getEmail() != null && userService.isEmailTaken(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.companyDto", "An account with this email already exists");
        }

        if (bindingResult.hasErrors()) {
            return "register-company";
        }

        userService.registerCompany(dto);
        return "redirect:/login?registered=company";
    }
}
