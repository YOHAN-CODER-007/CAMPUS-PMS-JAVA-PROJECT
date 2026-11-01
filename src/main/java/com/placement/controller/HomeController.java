package com.placement.controller;

import com.placement.model.CompanyStatus;
import com.placement.model.Role;
import com.placement.repository.CompanyRepository;
import com.placement.repository.JobRepository;
import com.placement.repository.StudentProfileRepository;
import com.placement.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Controller for the public home page with live campus statistics.
 */
@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    public HomeController(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          CompanyRepository companyRepository,
                          JobRepository jobRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        long studentsRegistered = userRepository.countByRole(Role.ROLE_STUDENT);
        long studentsPlaced = studentProfileRepository.countByPlacedTrue();
        long companiesCount = companyRepository.countByStatus(CompanyStatus.APPROVED);

        Double maxPackage = jobRepository.findMaxApprovedPackage();
        Double avgPackage = jobRepository.findAverageApprovedPackage();

        double highestPackageLpa = (maxPackage != null) ? maxPackage : 0.0;
        double averagePackageLpa = 0.0;
        if (avgPackage != null) {
            averagePackageLpa = BigDecimal.valueOf(avgPackage)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        model.addAttribute("studentsRegistered", studentsRegistered);
        model.addAttribute("studentsPlaced", studentsPlaced);
        model.addAttribute("companiesCount", companiesCount);
        model.addAttribute("highestPackage", highestPackageLpa);
        model.addAttribute("averagePackage", averagePackageLpa);

        return "home";
    }
}
