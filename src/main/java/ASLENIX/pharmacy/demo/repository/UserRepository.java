package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    User findByUsernameAndPassword(String username, String paw);

}
