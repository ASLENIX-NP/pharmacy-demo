package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.FinanceStats;
import ASLENIX.pharmacy.demo.repository.FinanceStatsRepository;
import ASLENIX.pharmacy.demo.repository.InvoiceRepository;
import ASLENIX.pharmacy.demo.repository.PurchaseOrderRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FinanceStatsImpl implements SchedulableTask {

    @Autowired
    private FinanceStatsRepository financeStatsRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;


    @Override
    public JobType getJobName() {
        return JobType.FINANCE_UPDATED;
    }


    private FinanceStats updateFinanceStats(FinanceStats financeStats){

        LocalDate startDate = LocalDate.of(financeStats.getYear(),financeStats.getMonth(), 1);

        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Double totalRevenue = invoiceRepository.sumRevenueBetween(startDate , endDate);
        totalRevenue = (totalRevenue != null) ? totalRevenue : 0.0;

        Double totalPurchaseOrder = purchaseOrderRepository.sumPurchasesBetween(startDate , endDate);
        totalPurchaseOrder = (totalPurchaseOrder != null) ? totalPurchaseOrder:0.0;

        Double netProfit = totalRevenue - totalPurchaseOrder;

        Double profitMargin = (netProfit/ totalRevenue ) *100;

        financeStats.setTotalRevenue(totalRevenue);
        financeStats.setPurchaseOrderTotal(totalPurchaseOrder);
        financeStats.setNetProfit(netProfit);
        financeStats.setProfitMargin(profitMargin);
        financeStats.setLastUpdated(LocalDateTime.now());

        return financeStatsRepository.save(financeStats);
    }

    @Override
    public void execute() {

        LocalDate todayMonthDate = LocalDate.now();
        int todayMonthYear = todayMonthDate.getYear();
        int todayMonth = todayMonthDate.getMonthValue();

        FinanceStats todayFinanceStat= financeStatsRepository.findByYearAndMonth(todayMonthYear, todayMonth)
                .orElseGet(() -> {
                    FinanceStats newStats = new FinanceStats();
                    newStats.setYear(todayMonthYear);
                    newStats.setMonth(todayMonth);
                    return newStats;
                });

        todayFinanceStat = updateFinanceStats(todayFinanceStat);

        LocalDate pastMonthDate= LocalDate.now().minusMonths(1);

        int lastMonthYear = pastMonthDate.getYear();
        int lastMonth = pastMonthDate.getMonthValue();

        LocalDate lastDayOfPastMonth = pastMonthDate.withDayOfMonth(pastMonthDate.lengthOfMonth());

        LocalDateTime endOfPastMonth = lastDayOfPastMonth.atTime(23, 59, 59);

        Optional<FinanceStats> lastMonthOpt = financeStatsRepository.findByYearAndMonth(lastMonthYear, lastMonth);

        FinanceStats lastMonthFinanceStat ;

        if (lastMonthOpt.isEmpty()) {
            FinanceStats tempFinanceStat = new FinanceStats();
            tempFinanceStat.setYear(todayMonthYear);
            tempFinanceStat.setMonth(todayMonth);
            lastMonthFinanceStat = updateFinanceStats(tempFinanceStat);

        } else {

            lastMonthFinanceStat = lastMonthOpt.get();

            if (lastMonthFinanceStat.getLastUpdated() == null || lastMonthFinanceStat.getLastUpdated().isBefore(endOfPastMonth)) {
                lastMonthFinanceStat = updateFinanceStats(lastMonthFinanceStat );
            }
        }

        Double tempGrowthRate = calculateProRatedGrowthRate(
                todayFinanceStat.getTotalRevenue() ,
                lastMonthFinanceStat.getTotalRevenue());
        todayFinanceStat.setTotalRevenueGrowthRate(tempGrowthRate);

        tempGrowthRate = calculateProRatedGrowthRate(
                todayFinanceStat.getPurchaseOrderTotal() ,
                lastMonthFinanceStat.getPurchaseOrderTotal());
        todayFinanceStat.setPurchaseOrderTotalGrowthRate(tempGrowthRate);

        tempGrowthRate = calculateProRatedGrowthRate(
                todayFinanceStat.getNetProfit() ,
                lastMonthFinanceStat.getNetProfit());
        todayFinanceStat.setNetProfitGrowthRate(tempGrowthRate);

        tempGrowthRate = calculateProRatedGrowthRate(
                todayFinanceStat.getProfitMargin() ,
                lastMonthFinanceStat.getProfitMargin());
        todayFinanceStat.setProfitMarginGrowthRate(tempGrowthRate);

        todayFinanceStat.setLastUpdated(LocalDateTime.now());
        financeStatsRepository.save(todayFinanceStat);

    }

    private Double calculateProRatedGrowthRate(Double currentTotal, Double lastMonthTotal) {
        if (currentTotal == null || lastMonthTotal == null || lastMonthTotal == 0.0) {
            return 0.0;
        }

        // Declare the calendar dates directly inside the method
        LocalDate today = LocalDate.now();
        LocalDate pastMonthDate = today.minusMonths(1);

        int daysPassed = today.getDayOfMonth();
        int totalDaysInLastMonth = pastMonthDate.lengthOfMonth();

        if (daysPassed <= 0) {
            return 0.0;
        }

        // Pro-rate last month's data based on current days passed
        Double lastMonthProRatedTarget = (lastMonthTotal / totalDaysInLastMonth) * daysPassed;

        if (lastMonthProRatedTarget == 0.0) {
            return 0.0;
        }

        return ((currentTotal - lastMonthProRatedTarget) / lastMonthProRatedTarget) * 100;
    }
}
