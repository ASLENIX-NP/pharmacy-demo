package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findByUsernameAndPassword(String username, String paw);
    boolean existsByEmail(String email);
    java.util.Optional<User> findByEmail(String email);

}
