package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.model.User;

import java.util.Optional;

public interface UserService {
    User userLogin(String username , String password);

    Optional<User> findUserByEmail(String email);

    User editUser(Long userId, String firstName, String lastName);

    String generateUserName(String firstName, Long userId);

    String generateInitials(String firstName, String lastName);

}

