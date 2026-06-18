package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem , Long> {
}
