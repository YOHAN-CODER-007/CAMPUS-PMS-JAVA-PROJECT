package com.placement.repository;

import com.placement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Application entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentOrderByAppliedDateDesc(StudentProfile student);

    List<Application> findByJobOrderByAppliedDateDesc(Job job);

    Optional<Application> findByStudentAndJob(StudentProfile student, Job job);

    boolean existsByStudentAndJob(StudentProfile student, Job job);

    long countByStudent(StudentProfile student);

    long countByStudentAndStatus(StudentProfile student, ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    long countByJob(Job job);

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countApplicationsByStatus();

    @Query("SELECT a.job.company.name, COUNT(a) FROM Application a WHERE a.status = 'SELECTED' OR a.status = 'PLACED' GROUP BY a.job.company.name")
    List<Object[]> countSelectionsByCompany();

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job = :job GROUP BY a.status")
    List<Object[]> countStatusByJob(Job job);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Application a WHERE a.student = :student")
    void deleteByStudent(StudentProfile student);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Application a WHERE a.job = :job")
    void deleteByJob(Job job);
}
