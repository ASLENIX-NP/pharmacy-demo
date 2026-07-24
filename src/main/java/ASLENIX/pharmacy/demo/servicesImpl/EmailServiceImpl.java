package ASLENIX.pharmacy.demo.servicesImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import ASLENIX.pharmacy.demo.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void sendPasswordSetupEmail(String to, String username, String token) {
        String setupUrl = baseUrl+"/password/set?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to ABIS PLUS - Set up your password");
        message.setText("Hello " + username + ",\n\n" +
                "You have been added to the ABIS PLUS system.\n" +
                "Please click the link below to set up your password and activate your account:\n\n" +
                setupUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Regards,\nABIS PLUS Admin");

        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetUrl =baseUrl + "/password/set?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ABIS PLUS - Password Reset Request");
        message.setText("Hello " + username + ",\n\n" +
                "We received a request to reset your password for your ABIS PLUS account.\n" +
                "Please click the link below to set a new password:\n\n" +
                resetUrl + "\n\n" +
                "This link will expire in 24 hours.\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Regards,\nABIS PLUS Admin");

        mailSender.send(message);
    }
}
