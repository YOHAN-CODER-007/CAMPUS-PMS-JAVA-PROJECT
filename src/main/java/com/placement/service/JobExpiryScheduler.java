package com.placement.service;

import com.placement.model.Job;
import com.placement.model.JobStatus;
import com.placement.repository.JobRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled service that automatically monitors campus job drives and marks expired jobs as CLOSED.
 */
@Component
public class JobExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobExpiryScheduler.class);

    private final JobRepository jobRepository;

    public JobExpiryScheduler(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Run on startup to immediately synchronize job states.
     */
    @PostConstruct
    public void init() {
        checkAndCloseExpiredJobs();
    }

    /**
     * Runs periodically every hour to close jobs that have passed their deadline.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledJobExpiryCheck() {
        checkAndCloseExpiredJobs();
    }

    /**
     * Finds active approved jobs where application deadline is before today,
     * and transitions status to CLOSED.
     */
    public void checkAndCloseExpiredJobs() {
        List<Job> activeJobs = jobRepository.findByStatus(JobStatus.APPROVED);
        int closedCount = 0;

        for (Job job : activeJobs) {
            if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
                job.setStatus(JobStatus.CLOSED);
                jobRepository.save(job);
                closedCount++;
                logger.info("Auto-closed expired campus job drive: ID={}, Title='{}', Deadline={}",
                        job.getId(), job.getTitle(), job.getDeadline());
            }
        }

        if (closedCount > 0) {
            logger.info("JobExpiryScheduler: Successfully auto-closed {} expired job drive(s).", closedCount);
        }
    }
}
