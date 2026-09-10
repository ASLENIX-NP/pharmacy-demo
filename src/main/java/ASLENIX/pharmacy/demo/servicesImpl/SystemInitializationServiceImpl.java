package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.UserRole;
import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.EmailService;
import ASLENIX.pharmacy.demo.services.SystemInitializationService;
import ASLENIX.pharmacy.demo.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class SystemInitializationServiceImpl implements SystemInitializationService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;


    @Value("${SYSTEM_INITIALIZATION_SECRET}")
    private String initializationSecret;

    @Value("${MAIL_USERNAME}")
    private String defaultUserEmail;

    @Override
    public String initializeSystem(String providedKey) {

        if (!initializationSecret.equals(providedKey)) {
            throw new RuntimeException("Invalid initialization key");
        }

        if(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE) > 0) {
            throw new RuntimeException("System already initialized");
        }

        if(userRepository.existsByEmail(defaultUserEmail)) {
            updatedDefaultUserAsActiveAdmin();
            return "System initialized successfully: default user updated to active admin";

        }

        addDefaultUser();
        return "System initialized successfully";
    }

    @Override
    public void updatedDefaultUserAsActiveAdmin() {
        User user = userRepository.findByEmail(defaultUserEmail).get();
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

    }

    @Override
    public void addDefaultUser() {

        User user = new User();

        user.setFirstName("DefaultAdmin");
        user.setLastName("User");
        user.setEmail(defaultUserEmail);
        user.setPassword(UUID.randomUUID().toString());
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(LocalDate.now());

        User savedUser = userRepository.save(user);

        PasswordResetToken token = tokenService.createToken(savedUser);
        emailService.sendPasswordSetupEmail(savedUser.getEmail(), savedUser.getUsername(), token.getToken());
    }
}
