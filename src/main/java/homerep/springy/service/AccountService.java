package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.model.RegisterModel;
import homerep.springy.model.resetpassword.ResetPasswordModel;
import jakarta.validation.constraints.NotBlank;

public interface AccountService {
    boolean isAllowedEmail(String email);

    boolean isRegistered(String email);

    Account registerAccount(RegisterModel registerModel);

    void sendEmailVerification(Account account);

    boolean verifyAccount(@NotBlank String token);

    void sendResetPassword(String email);

    boolean resetPassword(ResetPasswordModel resetPasswordModel);
}
