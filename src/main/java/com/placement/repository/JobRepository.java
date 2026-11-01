package com.placement.repository;

import com.placement.model.Company;
import com.placement.model.Job;
import com.placement.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Job entity.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

    List<Job> findByCompany(Company company);

    List<Job> findByCompanyOrderByCreatedAtDesc(Company company);

    long countByStatus(JobStatus status);

    @Query("SELECT MAX(j.packageLpa) FROM Job j WHERE j.status = 'APPROVED'")
    Double findMaxApprovedPackage();

    @Query("SELECT AVG(j.packageLpa) FROM Job j WHERE j.status = 'APPROVED'")
    Double findAverageApprovedPackage();

    List<Job> findAllByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Job j WHERE j.company = :company")
    void deleteByCompany(Company company);
}
