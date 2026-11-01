package com.placement.model;

/**
 * Status of a registered company.
 * PENDING: Newly registered company awaiting Officer approval.
 * APPROVED: Approved by Officer, can log in and post jobs.
 * REJECTED: Rejected by Officer.
 */
public enum CompanyStatus {
    PENDING,
    APPROVED,
    REJECTED
}
