package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.TokenService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PasswordSetupController {

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/set-password")
    public String showSetPasswordForm(@RequestParam(value = "token", required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("error", "Invalid or missing token.");
            return "set-password";
        }
        
        Optional<PasswordResetToken> tokenOpt = tokenService.validateToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "The setup link is invalid or has expired.");
            return "set-password";
        }
        
        model.addAttribute("token", token);
        return "set-password";
    }
    
    @PostMapping("/set-password")
    public String handlePasswordSetup(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "set-password";
        }
        String passwordRegex =
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$";

        if (!password.matches(passwordRegex)) {
            model.addAttribute("error",
                    "Password must be at least 6 characters long and contain at least one letter, one number, and one special character.");
            model.addAttribute("token", token);
            return "set-password";
        }

        Optional<PasswordResetToken> tokenOpt = tokenService.validateToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "The setup link is invalid or has expired.");
            return "set-password";
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        
        // Hash the password with BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);
        
        userRepository.save(user);
        
        // Delete the token so it can't be reused
        tokenService.deleteToken(resetToken);
        
        return "redirect:/login";
    }
}
