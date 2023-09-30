package homerep.springy.service.impl;

import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService, UserDetailsService {
    @Autowired
    private AccountRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UriBuilderFactory uriBuilderFactory;

    @Override
    public boolean isRegistered(String email) {
        return repository.findByEmail(email) != null;
    }

    @Override
    public Account registerAccount(AccountModel accountModel) {
        Account account = new Account();
        account.setEmail(accountModel.email());
        account.setPassword(passwordEncoder.encode(accountModel.password()));
        account.setType(Account.AccountType.SERVICE_REQUESTER);

        account = repository.save(account);
        sendEmailVerification(account);
        return account;
    }

    @Override
    public void sendEmailVerification(Account account) {
        if (account.isVerified()) {
            // Already verified there's nothing to do
            return;
        }
        account.setVerificationToken(UUID.randomUUID().toString());
        account = repository.save(account);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setFrom("noreply@localhost"); // TODO make configurable or leave in template?

        URI verifyUri = uriBuilderFactory
                .uriString("/api/verify")
                .queryParam("token", "{token}")
                .build(account.getVerificationToken());

        mailMessage.setText(verifyUri.toASCIIString()); // TODO pretty email templates?
        mailSender.send(mailMessage); // TODO handle MailException gracefully?
    }

    @Override
    public boolean verifyAccount(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Account account = repository.findByVerificationToken(token);
        // TODO should tokens expire?
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
