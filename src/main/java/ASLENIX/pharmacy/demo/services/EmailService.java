package ASLENIX.pharmacy.demo.services;

public interface EmailService {
    void sendPasswordSetupEmail(String to, String username, String token);
    void sendPasswordResetEmail(String to, String username, String token);
}
