package ASLENIX.pharmacy.demo.controller;


import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.services.AdminServices;
import ASLENIX.pharmacy.demo.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminServices adminServices;


    @GetMapping({"/"  ,"/login"})
    public String getlogin(){
        return "loginForm";
    }


    @PostMapping("/login")
    public String postLogin(
            @ModelAttribute User u ,
            HttpSession session, Model model) {

       User user = userService.userLogin(u.getUsername(), u.getPassword());


        if (user != null) {
            session.setAttribute("activeUser", user);
            session.setMaxInactiveInterval(600); // if user is inactive for more than 10 min session expires

        }else {
            model.addAttribute("error", "Id or password incorrect ");
            return "loginForm";
        }

        if(Objects.equals(user.getStatus(), UserStatus.SUSPENDED)){
            model.addAttribute("error", "Your account has been suspended");
            return "loginForm";
        }

        return "redirect:/Dashboard";

    }

    @GetMapping("/Dashboard")
    public String roleBasedDashboardRedirect(HttpSession session,Model model){
        User activeUser = (User) session.getAttribute("activeUser");
        if (activeUser == null) {
            return "redirect:/login";
        }

        switch (activeUser.getRole()){
            case ADMIN -> {
                return "redirect:/admin/dashboard";
            }
            case PHARMACIST -> {
                model.addAttribute("currentPage", "overview");
                return "redirect:/pharmacist/dashboard";
            }
            case CASHIER -> {
                model.addAttribute("currentPage", "overview");
                return "redirect:/cashier/dashboard";
            }
            case STOREKEEPER  -> {
                model.addAttribute("currentPage", "overview");
                return "redirect:/storekeeper/dashboard";
            }
            default -> {
                model.addAttribute("error", "role not defined ");
                return "loginForm";
            }
        }

    }

    @GetMapping("/logout")
    public String postLogout(HttpSession session){
        session.invalidate();

        return "redirect:/login";
    }
}
