package com.placement.repository;

import com.placement.model.StudentProfile;
import com.placement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for StudentProfile entity.
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser(User user);

    Optional<StudentProfile> findByUserId(Long userId);

    Optional<StudentProfile> findByCollegeEmail(String collegeEmail);

    List<StudentProfile> findByPlacedTrue();

    long countByPlacedTrue();

    @Query("SELECT s.department, COUNT(s) FROM StudentProfile s WHERE s.placed = true GROUP BY s.department")
    List<Object[]> countPlacedStudentsByDepartment();

    @Query("SELECT s.department, COUNT(s) FROM StudentProfile s GROUP BY s.department")
    List<Object[]> countTotalStudentsByDepartment();
}
