package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.Enums.StorageZone;
import ASLENIX.pharmacy.demo.model.ExpiryDateNotification;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.ScheduleTracker;
import ASLENIX.pharmacy.demo.repository.ExpiryDateNotificationRepository;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.ScheduleTrackerRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Component
public class ExpiryCheckTaskImpl implements SchedulableTask {

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    private ExpiryDateNotificationRepository expiryDateNotificationRepository;

    @Autowired
    private ScheduleTrackerRepository scheduleTrackerRepository;

    Long expiryDaysThreshold = 15L;


    @Override
    public JobType getJobName() {
        return JobType.EXPIRY_CHECKER;
    }

    HashMap<Long , ExpiryDateNotification>  mapInventoryIdToNotification(){
        List<ExpiryDateNotification> expiryDateNotificationList = expiryDateNotificationRepository.findByActionTakenFalse();

        HashMap<Long , ExpiryDateNotification> batchIdMapExpNotification = new HashMap<>();

        for (ExpiryDateNotification exp : expiryDateNotificationList){
            batchIdMapExpNotification.put(exp.getInventoryBatch().getId(),exp);

        }

        return batchIdMapExpNotification;
    }

    @Override
    public void execute() {

        ScheduleTracker scheduleTracker = scheduleTrackerRepository.findByJobType(JobType.EXPIRY_CHECKER);

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        LocalDate today = LocalDate.now();

        if (scheduleTracker.getLastRunTimestamp().isAfter(twentyFourHoursAgo)) {
            System.out.println("Job already executed in last 24 hours. Skipping.");
            return;
        }

        List<ExpiryDateNotification> notificationsToSave = new LinkedList<>();
        List<ExpiryDateNotification> notificationsToDelete = new LinkedList<>();

        List<InventoryBatch> inventoryBatchActiveList =
                inventoryBatchRepository.findBatchesByStatusAndZone(BatchApprovalStatus.APPROVED , StorageZone.MAIN_RACK);


        HashMap<Long ,ExpiryDateNotification> batchIdMapExpiry = mapInventoryIdToNotification();

        for (InventoryBatch inventoryBatch : inventoryBatchActiveList){

            long daysLeft = ChronoUnit.DAYS.between(today, inventoryBatch.getExpiryDate());

            if(daysLeft <= expiryDaysThreshold ){
                if (batchIdMapExpiry.containsKey(inventoryBatch.getId())){

                    // Case A: Notification exists -> Update the dynamic countdown days
                    ExpiryDateNotification expiryDateNotification = batchIdMapExpiry.get(inventoryBatch.getId());

                    expiryDateNotification.setDaysLeft((int) daysLeft);

                    notificationsToSave.add(expiryDateNotification);

                }else {
                    // Case B: No notification yet -> Create a brand new alert record
                    ExpiryDateNotification expiryDateNotification = new ExpiryDateNotification();

                    expiryDateNotification.setDaysLeft((int) daysLeft);
                    expiryDateNotification.setActionTaken(false);
                    expiryDateNotification.setInventoryBatch(inventoryBatch);

                    notificationsToSave.add(expiryDateNotification);
                }
            }else {
                // Case C: Batch is safe, but check if a stale notification exists
                // (e.g., if an admin corrected an incorrect expiry date manually)
                if(batchIdMapExpiry.containsKey(inventoryBatch.getId())){
                    notificationsToDelete.add(batchIdMapExpiry.get(inventoryBatch.getId()));
                }
            }

        }

        if (!notificationsToSave.isEmpty()){
            expiryDateNotificationRepository.saveAll(notificationsToSave);
        }

        if(!notificationsToDelete.isEmpty()){
            expiryDateNotificationRepository.deleteAll(notificationsToDelete);
        }
    }
}
