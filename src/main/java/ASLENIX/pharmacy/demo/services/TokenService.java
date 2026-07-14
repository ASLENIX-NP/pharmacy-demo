package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import java.util.Optional;

public interface TokenService {
    PasswordResetToken createToken(User user);
    Optional<PasswordResetToken> validateToken(String token);
    void deleteToken(PasswordResetToken token);
}
