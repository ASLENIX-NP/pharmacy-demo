package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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

    @Component // 👈 Tells Spring to manage this class
    public static class NightlyTaskScheduler {

        @Scheduled(cron = "0 0 0 * * ?") // 👈 Runs every night at midnight
        public void runMidnightJob() {
            // 1. Fetch your models here
            // 2. Perform the logic or updates
            // 3. Save them back to the database
            System.out.println("Midnight job executed successfully.");
        }
    }

}
