package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.Supplier;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
}
