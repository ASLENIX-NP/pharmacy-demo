package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.exception.InvalideTokenException;
import ASLENIX.pharmacy.demo.exception.MismatchPasswordException;
import ASLENIX.pharmacy.demo.exception.UserNotFoundException;
import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.EmailService;
import ASLENIX.pharmacy.demo.services.PasswordServices;
import ASLENIX.pharmacy.demo.services.TokenService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordServicesImpl implements PasswordServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenServiceImpl tokenService;

    @Autowired
    private EmailService emailService;

    @Override
    public void sendPasswordForgotEmail(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if(userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with the provided email address.");
        }

        User user = userOpt.get();
        PasswordResetToken token = tokenService.createToken(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token.getToken());

    }

    @Override
    public void setPassword(String token, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
           throw new MismatchPasswordException("Passwords do not match.");

        }
        String passwordRegex =
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[#@$!%*?&#])[A-Za-z\\d#@$!%*?&#]{6,}$";

        if (!password.matches(passwordRegex)) {
          throw new MismatchPasswordException("Password must be at least 6 characters long and contain at least one letter, one number, and one special character.");
        }

        Optional<PasswordResetToken> tokenOpt = tokenService.validateToken(token);

        if (tokenOpt.isEmpty()) {
            throw new InvalideTokenException("The setup link is invalid or has expired.");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
        tokenService.deleteToken(resetToken);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new MismatchPasswordException("The new passwords do not match.");
        }

        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }

        User user = userOpt.get();

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new MismatchPasswordException("The old password is incorrect.");
        }

        String passwordRegex =
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[#@$!%*?&])[A-Za-z\\d#@$!%*?&]{6,}$";

        if (!newPassword.matches(passwordRegex)) {
            throw new MismatchPasswordException("The new password must be at least 6 characters long and contain at least one letter, one number, and one special character.");
        }

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);

        userRepository.save(user);
    }
}
