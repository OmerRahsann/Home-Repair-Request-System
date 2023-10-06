package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.model.RegisterModel;

public interface AccountService {
    boolean isRegistered(String email);

    Account registerAccount(RegisterModel registerModel);

    void sendEmailVerification(Account account);

    boolean verifyAccount(String token);

}
