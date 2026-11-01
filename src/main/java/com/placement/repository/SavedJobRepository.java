package com.placement.repository;

import com.placement.model.Job;
import com.placement.model.SavedJob;
import com.placement.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for SavedJob entity.
 */
@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByStudentOrderBySavedAtDesc(StudentProfile student);

    Optional<SavedJob> findByStudentAndJob(StudentProfile student, Job job);

    boolean existsByStudentAndJob(StudentProfile student, Job job);

    void deleteByStudentAndJob(StudentProfile student, Job job);

    long countByStudent(StudentProfile student);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM SavedJob s WHERE s.student = :student")
    void deleteByStudent(StudentProfile student);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM SavedJob s WHERE s.job = :job")
    void deleteByJob(Job job);
}
