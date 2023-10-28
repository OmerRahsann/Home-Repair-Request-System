package homerep.springy.controller;

import homerep.springy.authorities.AccountType;
import homerep.springy.entity.Account;
import homerep.springy.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/type")
    public AccountType getAccountType(@AuthenticationPrincipal User user) {
        Account account = accountRepository.findByEmail(user.getUsername());
        return account.getType();
    }

    @GetMapping("/verified")
    public boolean isVerified(@AuthenticationPrincipal User user) {
        Account account = accountRepository.findByEmail(user.getUsername());
        return account.isVerified();
    }
}
