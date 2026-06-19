package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.ScheduleTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleTrackerRepository extends JpaRepository<ScheduleTracker , UUID> {
    ScheduleTracker findByJobType(JobType jobType);


}
