package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.EmailService;
import ASLENIX.pharmacy.demo.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgotPassword";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            PasswordResetToken token = tokenService.createToken(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token.getToken());
        }

        // We always show the same message whether the email exists or not to prevent email enumeration
        redirectAttributes.addFlashAttribute("success", "If an account with that email exists, a password reset link has been sent.");
        
        return "redirect:/forgot-password";
    }
}
