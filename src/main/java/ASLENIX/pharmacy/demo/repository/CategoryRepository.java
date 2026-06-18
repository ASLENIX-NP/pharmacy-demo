package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CategoryRepository extends JpaRepository<Category ,Long> {
}
