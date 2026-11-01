package com.placement.config;

import com.placement.model.Company;
import com.placement.model.CompanyStatus;
import com.placement.model.Role;
import com.placement.model.User;
import com.placement.repository.CompanyRepository;
import com.placement.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Custom UserDetailsService that validates user credentials and approval status.
 * Ensures COMPANIES cannot log in while pending officer approval.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public CustomUserDetailsService(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        if (!user.isActive()) {
            throw new DisabledException("ACCOUNT_INACTIVE");
        }

        // Special rule for COMPANY role: Must be APPROVED by the Officer to log in
        if (user.getRole() == Role.ROLE_COMPANY) {
            Optional<Company> companyOpt = companyRepository.findByUser(user);
            if (companyOpt.isPresent()) {
                Company company = companyOpt.get();
                if (company.getStatus() == CompanyStatus.PENDING) {
                    throw new DisabledException("COMPANY_PENDING");
                } else if (company.getStatus() == CompanyStatus.REJECTED) {
                    throw new DisabledException("COMPANY_REJECTED");
                }
            }
        }

        return new CustomUserDetails(user);
    }
}
