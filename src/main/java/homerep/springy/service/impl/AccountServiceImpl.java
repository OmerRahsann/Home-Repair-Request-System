package homerep.springy.service.impl;

import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService, UserDetailsService {
    @Autowired
    private AccountRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean isRegistered(String email) {
        return repository.findByEmail(email) != null;
    }

    @Override
    public Account registerAccount(AccountModel accountModel) {
        Account account = new Account();
        account.setEmail(accountModel.email());
        account.setPassword(passwordEncoder.encode(accountModel.password()));
        account.setVerificationToken(UUID.randomUUID().toString());
        account.setType(Account.AccountType.SERVICE_REQUESTER);
        // TODO generate url and send email
        return repository.save(account);
    }

    @Override
    public boolean verifyAccount(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Account account = repository.findByVerificationToken(token);
        if (account == null) {
            return false; // TODO exception?
        }
        account.setVerified(true);
        account.setVerificationToken(null);
        repository.save(account);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = repository.findByEmail(username);
        if (account != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(account.getType());
            if (account.isVerified()) {
                authorities.add(new SimpleGrantedAuthority("VERIFIED"));
            }
            return new User(account.getEmail(), account.getPassword(), authorities);
        }
        return null;
    }
}
