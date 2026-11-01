package com.placement.repository;

import com.placement.model.Company;
import com.placement.model.InterviewExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for InterviewExperience entity.
 */
@Repository
public interface InterviewExperienceRepository extends JpaRepository<InterviewExperience, Long> {

    List<InterviewExperience> findAllByOrderByCreatedAtDesc();

    List<InterviewExperience> findByCompanyOrderByCreatedAtDesc(Company company);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM InterviewExperience ie WHERE ie.student = :student")
    void deleteByStudent(com.placement.model.StudentProfile student);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM InterviewExperience ie WHERE ie.company = :company")
    void deleteByCompany(Company company);
}
