package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.AdminDashboardStats;
import ASLENIX.pharmacy.demo.model.ScheduleTracker;
import ASLENIX.pharmacy.demo.repository.AdminDashboardStatsRepository;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.InvoiceRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AdminDashboardStatsImpl implements SchedulableTask {

    @Autowired
    private AdminDashboardStatsRepository adminDashboardStatsRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public JobType getJobName() {
        return JobType.ADMIN_DASHBOARD_UPDATER;
    }

    @Override
    public void execute() {

        LocalDate now = LocalDate.now();

        int year = now.getYear();

        AdminDashboardStats adminDashboardStats = adminDashboardStatsRepository.findByYearAndMonth(year,month);







        Long total ;

        adminDashboardStats.setThisMonthSales(total);

        adminDashboardStatsRepository.save(adminDashboardStats);

    }
}
