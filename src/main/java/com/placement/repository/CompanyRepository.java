package com.placement.repository;

import com.placement.model.Company;
import com.placement.model.CompanyStatus;
import com.placement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Company entity.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUser(User user);

    Optional<Company> findByUserId(Long userId);

    List<Company> findByStatus(CompanyStatus status);

    long countByStatus(CompanyStatus status);

    Optional<Company> findByName(String name);

    List<Company> findAllByOrderByNameAsc();
}
