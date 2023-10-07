package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.model.RegisterModel;
import jakarta.validation.constraints.NotBlank;

public interface AccountService {
    boolean isRegistered(String email);

    Account registerAccount(RegisterModel registerModel);

    void sendEmailVerification(Account account);

    boolean verifyAccount(@NotBlank String token);

}
