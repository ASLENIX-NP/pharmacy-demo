package ASLENIX.pharmacy.demo.model;

import ASLENIX.pharmacy.demo.Enums.JobType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_tracker_tbl" )
@Getter
@Setter
public class ScheduleTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column( nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private JobType jobTypes;


    @Column( nullable = false)
    private LocalDateTime lastRunTimestamp;

    @Column(nullable = false , insertable = false , updatable = false)
    private Integer thresholdTime =  24;

}
