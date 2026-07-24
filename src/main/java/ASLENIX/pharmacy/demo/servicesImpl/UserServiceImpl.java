package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.exception.UserNotFoundException;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User userLogin(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            try {
                if (BCrypt.checkpw(password, user.getPassword())) {
                    return user;
                }
            } catch (IllegalArgumentException e) {
                // Not a valid BCrypt hash, ignore and try fallback
            }

            // Fallback for existing plain text passwords
            if (password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User editUser(Long userId, String firstName, String lastName) {
        User user = userRepository.findById(userId).orElseThrow(()->
                new UserNotFoundException("User not found"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setInitials(generateInitials(firstName, lastName));
        user.setUsername(generateUserName(firstName, userId));
        return userRepository.save(user);
    }


    @Override
    public String generateUserName(String firstName, Long userId) {
        if (firstName == null) {
            firstName = "";
        }

        String combined = firstName + (userId != null ? userId : "");

        return combined.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    @Override
    public String generateInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();

        if (firstName != null && !firstName.trim().isEmpty()) {
            initials.append(firstName.trim().charAt(0));
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            initials.append(lastName.trim().charAt(0));
        }

        return initials.toString().toUpperCase();
    }



}
