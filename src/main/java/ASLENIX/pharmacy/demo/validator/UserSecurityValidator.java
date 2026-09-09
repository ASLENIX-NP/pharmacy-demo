package ASLENIX.pharmacy.demo.validator;


import ASLENIX.pharmacy.demo.Enums.UserRole;
import ASLENIX.pharmacy.demo.Enums.UserStatus;
import ASLENIX.pharmacy.demo.exception.LastAdminLockoutException;
import ASLENIX.pharmacy.demo.exception.SelfModificationException;
import ASLENIX.pharmacy.demo.exception.UserNotFoundException;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSecurityValidator {


    @Autowired
    private UserRepository userRepository;

    private boolean isSelfModification(User activeUser, User targetUser)
    {
        return activeUser.getId().equals(targetUser.getId());
    }

    private boolean wouldRemoveLastActiveAdmin(User newTargetUSer, User oldTargetUser){

        if(oldTargetUser.getRole() != UserRole.ADMIN){
            return false;
        }

        if(newTargetUSer.getRole() == UserRole.ADMIN){
            return false;
        }


        return userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE) <= 1;

    }

    public void validateStatusChange(Long activeUserId , User newTargetUSer, User oldTargetUser) {

        Optional<User> activeUserOpt = userRepository.findById(activeUserId);
        if (activeUserOpt.isEmpty()){
            throw new UserNotFoundException("User not found");
        }

        User activeUser = activeUserOpt.get();

        if (isSelfModification(activeUser, oldTargetUser)) {
            throw new SelfModificationException("You cannot modify your own account status");
        }

        if(wouldRemoveLastActiveAdmin( newTargetUSer,  oldTargetUser )) {
            throw new LastAdminLockoutException("You cannot modify the status of the last active admin");
        }

    }












}
