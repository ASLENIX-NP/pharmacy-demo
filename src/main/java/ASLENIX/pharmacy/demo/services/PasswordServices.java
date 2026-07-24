package ASLENIX.pharmacy.demo.services;


public interface PasswordServices {
        void sendPasswordForgotEmail(String email);

        void setPassword(String token  , String password , String confirmPassword);

        void changePassword(Long userId,String oldPassword, String newPassword, String confirmNewPassword);

}
