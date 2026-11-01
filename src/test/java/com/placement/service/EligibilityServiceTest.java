package com.placement.service;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class EligibilityServiceTest {

    private final EligibilityService service = new EligibilityService();

    @Test
    void testDepartmentMatchingFullToShort() {
        // Student in full form, job in short form
        assertTrue(service.isDepartmentEligible("Computer Science & Engineering", Arrays.asList("CSE", "IT")));
        assertTrue(service.isDepartmentEligible("Information Technology", Arrays.asList("CSE", "IT")));
        assertTrue(service.isDepartmentEligible("Electronics & Communication", Arrays.asList("ECE")));
        assertTrue(service.isDepartmentEligible("Electrical & Electronics", Arrays.asList("EEE", "ECE")));
        assertTrue(service.isDepartmentEligible("Mechanical Engineering", Arrays.asList("MECH")));
        assertTrue(service.isDepartmentEligible("Civil Engineering", Arrays.asList("CIVIL")));
        assertTrue(service.isDepartmentEligible("Artificial Intelligence & Data Science", Arrays.asList("AIDS")));
    }

    @Test
    void testDepartmentMatchingShortToFull() {
        // Student in short form, job in full form
        assertTrue(service.isDepartmentEligible("CSE", Collections.singletonList("Computer Science & Engineering")));
        assertTrue(service.isDepartmentEligible("IT", Collections.singletonList("Information Technology")));
        assertTrue(service.isDepartmentEligible("ECE", Collections.singletonList("Electronics & Communication")));
    }

    @Test
    void testUniversalDepartments() {
        assertTrue(service.isDepartmentEligible("Mechanical Engineering", Collections.singletonList("All Branches")));
        assertTrue(service.isDepartmentEligible("Civil Engineering", Collections.singletonList("All")));
    }

    @Test
    void testIneligibleDepartment() {
        assertFalse(service.isDepartmentEligible("Civil Engineering", Arrays.asList("CSE", "IT")));
        assertFalse(service.isDepartmentEligible("Mechanical Engineering", Arrays.asList("CSE", "ECE")));
    }
}
