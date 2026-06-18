package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.Enums.JobType;

public interface SchedulableTask {
    JobType getJobName();
    void execute();
}
