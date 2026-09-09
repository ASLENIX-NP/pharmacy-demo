package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.Enums.UserRole;
import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByUsernameAndPassword(String username, String paw);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRoleAndStatus(UserRole role, UserStatus status);

    @Query("SELECT u FROM User u WHERE u.id <> :userId")
    List<User> findAllExceptUser(@Param("userId") Long userId);

}
