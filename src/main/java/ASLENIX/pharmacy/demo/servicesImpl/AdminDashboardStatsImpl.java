package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.AdminDashboardStats;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.ScheduleTracker;
import ASLENIX.pharmacy.demo.repository.AdminDashboardStatsRepository;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.InvoiceRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
        int month = now.getMonthValue();

        AdminDashboardStats adminDashboardStats = adminDashboardStatsRepository.findByYearAndMonth(year, month);
        if (adminDashboardStats == null) {
            adminDashboardStats = new AdminDashboardStats();
            adminDashboardStats.setYear(year);
            adminDashboardStats.setMonth(month);
        }

        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.getDayOfMonth());

        List<Invoice> monthlyInvoices = invoiceRepository.findCompleteInvoicesByDateRange(startDate, endDate);

        Double total = 0.0;
        if (monthlyInvoices != null && !monthlyInvoices.isEmpty()) {
            for (Invoice invoice : monthlyInvoices) {
                if (invoice.getGrandTotal() != null) {
                    total += invoice.getGrandTotal();
                }
            }
        }

        adminDashboardStats.setThisMonthSales(total);
        adminDashboardStats.setLastUpdated(now);
        adminDashboardStatsRepository.save(adminDashboardStats);
    }
    }