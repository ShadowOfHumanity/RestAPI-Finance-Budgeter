package com.example.demo.config;

import com.example.demo.DB.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

    private final UserRepository userRepository;
    private final FinancesService financesService;
    private final SpentYearlyService spentYearlyService;

    @Autowired
    public SchedulingConfig(UserRepository userRepository,
                            FinancesService financesService,
                            SpentYearlyService spentYearlyService) {
        this.userRepository = userRepository;
        this.financesService = financesService;
        this.spentYearlyService = spentYearlyService;
    }

    // Run at midnight on the first day of each month (0 0 0 1 * ?)
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void performMonthlyReset() {
        logger.info("Starting monthly spending reset at: {}", LocalDateTime.now());

        try {
            List<User> allUsers = userRepository.findAll();
            int processedUsers = 0;
            int failedUsers = 0;

            for (User user : allUsers) {
                try {
                    // Reset monthly spending for each user
                    financesService.resetMonthlySpending(user.getUserID());
                    processedUsers++;
                    logger.debug("Successfully reset spending for user: {}", user.getUsername());
                } catch (Exception e) {
                    failedUsers++;
                    logger.error("Failed to reset spending for user: {}. Error: {}",
                            user.getUsername(), e.getMessage());
                }
            }

            logger.info("Monthly reset completed. Processed: {}, Failed: {}",
                    processedUsers, failedUsers);

        } catch (Exception e) {
            logger.error("Critical error during monthly reset: {}", e.getMessage());
            // add notification service for failures
        }
    }

    // Run at midnight on January 1st (0 0 0 1 1 ?)
    @Scheduled(cron = "0 0 0 1 1 ?")
    @Transactional
    public void initializeNewYearRecords() {
        logger.info("Starting new year initialization at: {}", LocalDateTime.now());

        try {
            List<User> allUsers = userRepository.findAll();
            int processedUsers = 0;
            int failedUsers = 0;

            for (User user : allUsers) {
                try {
                    // Create new year record for each user
                    spentYearlyService.createInitialYearlyRecord(user);
                    processedUsers++;
                    logger.debug("Successfully created new year record for user: {}",
                            user.getUsername());
                } catch (Exception e) {
                    failedUsers++;
                    logger.error("Failed to create new year record for user: {}. Error: {}",
                            user.getUsername(), e.getMessage());
                }
            }

            logger.info("New year initialization completed. Processed: {}, Failed: {}",
                    processedUsers, failedUsers);

        } catch (Exception e) {
            logger.error("Critical error during new year initialization: {}", e.getMessage());
        }
    }

    // Run every day at midnight to check for any missed resets (0 0 0 * * ?)
    @Scheduled(cron = "0 0 0 * * ?")
    public void verifyMonthlyResets() {
        LocalDateTime now = LocalDateTime.now();

        // Only run this check if it's not the first day of the month
        // (since the main reset would have just run)
        if (now.getDayOfMonth() != 1) {
            logger.info("Starting daily reset verification at: {}", now);

            try {
                List<User> allUsers = userRepository.findAll();
                for (User user : allUsers) {
                    Finances finances = financesService.getFinancesForUser(user.getUserID());

                    // Check if spending should have been reset but wasn't
                    if (needsReset(finances)) {
                        logger.warn("Found missed reset for user: {}. Performing reset now.",
                                user.getUsername());
                        financesService.resetMonthlySpending(user.getUserID());
                    }
                }
            } catch (Exception e) {
                logger.error("Error during reset verification: {}", e.getMessage());
            }
        }
    }

    private boolean needsReset(Finances finances) {
        //placeholder for future implementation
        return false;
    }
}
