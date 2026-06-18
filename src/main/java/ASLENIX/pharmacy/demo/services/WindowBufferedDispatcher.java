package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.ScheduleTracker;
import ASLENIX.pharmacy.demo.repository.ScheduleTrackerRepository;
import ASLENIX.pharmacy.demo.servicesImpl.ExpiryCheckTaskImpl;
import ASLENIX.pharmacy.demo.servicesImpl.LowStockTaskImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WindowBufferedDispatcher {

    @Autowired
    private ScheduleTrackerRepository scheduleTrackerRepository;

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
        LocalDateTime cycleCutoff = LocalDateTime.now().minusHours(23);
        ScheduleTracker scheduleTracker = scheduleTrackerRepository.findByJobType(jobType);

        switch (scheduleTracker.getJobTypes()){

            case EXPIRY_CHECKER-> {
                ExpiryCheckTaskImpl expiryCheckTask = new ExpiryCheckTaskImpl();
                expiryCheckTask.execute();
            }

            case LOW_STOCK_CHECKER ->{
                LowStockTaskImpl lowStockTask = new LowStockTaskImpl();
                lowStockTask.execute();

            }

                default -> {
                return;
            }
        }

    }






}
