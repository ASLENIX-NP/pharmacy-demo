package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.Enums.JobType;

import java.time.LocalDateTime;

public interface SchedulableTask {
    JobType getJobName();
    void execute();
}
