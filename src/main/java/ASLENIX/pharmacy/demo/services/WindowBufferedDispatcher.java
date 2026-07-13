package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.AdminDashboardStats;
import ASLENIX.pharmacy.demo.model.FinanceStats;
import ASLENIX.pharmacy.demo.model.ScheduleTracker;
import ASLENIX.pharmacy.demo.repository.ScheduleTrackerRepository;
import ASLENIX.pharmacy.demo.servicesImpl.AdminDashboardStatsImpl;
import ASLENIX.pharmacy.demo.servicesImpl.ExpiryCheckTaskImpl;
import ASLENIX.pharmacy.demo.servicesImpl.FinanceStatsImpl;
import ASLENIX.pharmacy.demo.servicesImpl.LowStockTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WindowBufferedDispatcher {

    @Autowired
    private ScheduleTrackerRepository scheduleTrackerRepository;

    @Autowired
    private ExpiryCheckTaskImpl expiryCheckTask;

    @Autowired
    private LowStockTaskImpl lowStockTask;

    @Autowired
    private AdminDashboardStatsImpl adminDashboardStats;

    @Autowired
    private FinanceStatsImpl financeStats;

    // Fires every 5 minutes. If a target window is open, it goes through.
    // Otherwise, it skips the database completely.
    //change this if jobs become more than 6
    @Scheduled(cron = "0 */5 * * * *")
    public void evaluateTimelineTick() {

        int currentMinute = LocalDateTime.now().getMinute();

        JobType[] jobs = JobType.values();
        int totalJobs = jobs.length;

        // 1. Dynamically calculate the perfect step interval (e.g., 60 / 4 = 15 minutes)
        int stepInterval = 60 / totalJobs;

        // 2. Dynamically scale the window width to be 1/3 of the interval (Minimum of 2 mins for safety)
        int dynamicWindowWidth = Math.max(2, stepInterval / 3);


        JobType targetJobName = null;

        // 3. Dynamically check which job window is currently open
        for (int i = 0; i < totalJobs; i++) {
            int windowStartMinute = i * stepInterval;
            int windowEndMinute = windowStartMinute + dynamicWindowWidth;

            if (currentMinute >= windowStartMinute && currentMinute <= windowEndMinute) {
                targetJobName = jobs[i];
                break;
            }


        }
        // 4. Guard Clause: Outside any active window, exit instantly
        if (targetJobName == null) {
            return;
        }

        processJobExecution(targetJobName);
    }

    private void processJobExecution(JobType jobType) {

        ScheduleTracker scheduleTracker = scheduleTrackerRepository.findByJobType(jobType);

        LocalDateTime cycleCutoff = LocalDateTime.now().minusHours(scheduleTracker.getThresholdTime());

// Guard Clause: Exit early if the task ran within the threshold time
        if (scheduleTracker.getLastRunTimestamp().isAfter(cycleCutoff)) {
            System.out.println(scheduleTracker.getJobType().getValue()+ " ran recently. Skipping execution.");
            return;
        }

        System.out.println(scheduleTracker.getJobType().getValue()+ "Threshold exceeded. Running the task....");


        switch (scheduleTracker.getJobType()){

            case EXPIRY_CHECKER-> {
                try {
                    expiryCheckTask.execute();

                    scheduleTracker.setLastRunTimestamp(LocalDateTime.now());
                    scheduleTrackerRepository.save(scheduleTracker);
                }catch (RuntimeException e) {
                    System.err.println("Job failed to execute: " + e.getMessage());
                }
           }

            case LOW_STOCK_CHECKER ->{

                try {
                    lowStockTask.execute();

                    scheduleTracker.setLastRunTimestamp(LocalDateTime.now());
                    scheduleTrackerRepository.save(scheduleTracker);
                }catch (RuntimeException e) {
                    System.err.println("Job failed to execute: " + e.getMessage());
                }

            }

            case ADMIN_DASHBOARD_UPDATER -> {

                try {
                    adminDashboardStats.execute();

                    scheduleTracker.setLastRunTimestamp(LocalDateTime.now());
                    scheduleTrackerRepository.save(scheduleTracker);
                }catch (RuntimeException e) {
                    System.err.println("Job failed to execute: " + e.getMessage());
                }


            }

            case FINANCE_UPDATED -> {

                try {
                    financeStats.execute();

                    scheduleTracker.setLastRunTimestamp(LocalDateTime.now());
                    scheduleTrackerRepository.save(scheduleTracker);
                }catch (RuntimeException e) {
                    System.err.println("Job failed to execute: " + e.getMessage());
                }

            }

            default -> {
                return;
            }
        }

    }






}
