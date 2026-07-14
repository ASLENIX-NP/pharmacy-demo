package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.model.PasswordResetToken;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.PasswordResetTokenRepository;
import ASLENIX.pharmacy.demo.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Override
    @Transactional
    public PasswordResetToken createToken(User user) {
        // Delete any existing tokens for this user first
        tokenRepository.deleteByUser(user);
        
        String tokenString = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(tokenString, user);
        return tokenRepository.save(token);
    }

    @Override
    public Optional<PasswordResetToken> validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isPresent() && !tokenOpt.get().isExpired()) {
            return tokenOpt;
        }
        return Optional.empty(); // Returns empty if not found or expired
    }

    @Override
    @Transactional
    public void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }
}
