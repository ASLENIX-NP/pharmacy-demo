package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, Long id);
}
