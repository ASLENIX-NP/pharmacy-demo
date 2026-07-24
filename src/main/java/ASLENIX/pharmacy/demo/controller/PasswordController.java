package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.exception.InvalideTokenException;
import ASLENIX.pharmacy.demo.exception.MismatchPasswordException;
import ASLENIX.pharmacy.demo.exception.UserNotFoundException;
import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.services.EmailService;
import ASLENIX.pharmacy.demo.services.TokenService;
import ASLENIX.pharmacy.demo.servicesImpl.PasswordServicesImpl;
import ASLENIX.pharmacy.demo.servicesImpl.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordServicesImpl passwordServices;

    @GetMapping("/password/forgot")
    public String showForgotPasswordForm() {
        return "UserForgotPassword";
    }

    @PostMapping("/password/forgot")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {

        try {
            passwordServices.sendPasswordForgotEmail(email);
            redirectAttributes.addFlashAttribute("success", "If an account with that email exists, a password reset link has been sent.");
        } catch (UserNotFoundException e) {
            // Handle the case where the user is not found
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Handle any other exceptions that might occur during email sending
            redirectAttributes.addFlashAttribute("error", "An error occurred while sending the password reset email.");
        }finally {
            redirectAttributes.addFlashAttribute("email", email);
        }
        return "redirect:/password/forgot";
    }


    @GetMapping("/password/set")
    public String showSetPasswordForm(
            @RequestParam(value = "token", required = false) String token,
            Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("error", "Invalid or missing token.");
            return "UserSetPassword";
        }

        Optional<PasswordResetToken> tokenOpt = tokenService.validateToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "The setup link is invalid or has expired.");
            return "UserSetPassword";
        }

        model.addAttribute("token", token);
        return "UserSetPassword";
    }

    @PostMapping("/password/set")
    public String handlePasswordSetup(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes)
    {
        try {
            passwordServices.setPassword(token, password, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Password has been reset successfully.");

        } catch (InvalideTokenException | MismatchPasswordException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("token", token);
            return "redirect:/password/set";
        }
        return "redirect:/login";
    }


    @GetMapping("/password/change")
    public String changePasswordForm() {
        return "UserChangePassword";
    }

    @PostMapping("/password/change")
    public String handlePasswordChange(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes, HttpSession session)
    {
        try {

            User user = (User) session.getAttribute("activeUser");
            passwordServices.changePassword(user.getId(), oldPassword, password, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Password has been reset successfully.");

        } catch (InvalideTokenException | MismatchPasswordException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/password/change";
        }
        return "redirect:/user/accountsettings";
    }



}
