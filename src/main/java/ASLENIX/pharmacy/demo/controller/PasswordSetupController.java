package ASLENIX.pharmacy.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordSetupController {

    @GetMapping("/set-password")
    public String showSetPasswordForm(@RequestParam(value = "token", required = false) String token, Model model) {
        // Just for UI testing purposes right now.
        // Later we will add logic to validate the token before showing the page.
        model.addAttribute("token", token != null ? token : "dummy-token-for-testing");
        return "set-password";
    }
}
