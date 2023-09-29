package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;

public interface AccountService {
    boolean isRegistered(String email);

    Account registerAccount(AccountModel accountModel);

    boolean verifyAccount(String token);

}
