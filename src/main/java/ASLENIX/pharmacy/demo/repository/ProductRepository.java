package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {

}
