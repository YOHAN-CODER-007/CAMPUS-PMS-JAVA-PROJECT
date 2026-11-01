package com.placement.service;

import com.placement.dto.CompanyRegisterDto;
import com.placement.dto.StudentRegisterDto;
import com.placement.model.*;
import com.placement.repository.CompanyRepository;
import com.placement.repository.StudentProfileRepository;
import com.placement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service handling user account operations and registrations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks if an email is already registered in the system.
     */
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Finds a user by email address.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    /**
     * Registers a new Student user and initializes their basic StudentProfile.
     */
    @Transactional
    public User registerStudent(StudentRegisterDto dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();

        // 1. Create User entity
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_STUDENT);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // 2. Create StudentProfile entity linked to the user
        StudentProfile profile = new StudentProfile();
        profile.setUser(savedUser);
        profile.setName(dto.getName().trim());
        profile.setDepartment(dto.getDepartment().trim());
        profile.setCollegeEmail(dto.getCollegeEmail().trim());
        profile.setContactNumber(dto.getContactNumber().trim());
        profile.setCgpa(dto.getCgpa());
        profile.setTotalArrears(0); // Default 0
        profile.setPassingYear(dto.getPassingYear());
        profile.setPlaced(false);

        studentProfileRepository.save(profile);

        return savedUser;
    }

    /**
     * Registers a new Company user with PENDING status awaiting officer approval.
     */
    @Transactional
    public User registerCompany(CompanyRegisterDto dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();

        // 1. Create User entity
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_COMPANY);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // 2. Create Company entity with PENDING status
        Company company = new Company();
        company.setUser(savedUser);
        company.setName(dto.getName().trim());
        company.setLocation(dto.getLocation().trim());
        company.setWebsite(dto.getWebsite() != null ? dto.getWebsite().trim() : "");
        company.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        company.setContactEmail(dto.getContactEmail().trim());
        company.setContactNumber(dto.getContactNumber().trim());
        company.setStatus(CompanyStatus.PENDING); // Requires officer approval to log in

        companyRepository.save(company);

        return savedUser;
    }
}
