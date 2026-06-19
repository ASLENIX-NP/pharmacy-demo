package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.Product;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.ProductRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class LowStockTaskImpl implements SchedulableTask {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;


    @Override
    public JobType getJobName() {
        return JobType.LOW_STOCK_CHECKER;
    }








    @Override
    public void execute() {




    }
}
