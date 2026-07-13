package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User userLogin(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }


}
