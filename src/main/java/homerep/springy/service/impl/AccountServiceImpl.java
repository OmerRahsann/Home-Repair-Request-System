package homerep.springy.service.impl;

import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.model.AccountModel;
import homerep.springy.model.RegisterModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.service.AccountService;
import homerep.springy.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService, UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UriBuilderFactory uriBuilderFactory;

    @Override
    public boolean isRegistered(String email) {
        return accountRepository.findByEmail(email) != null;
    }

    @Override
    @Transactional
    public Account registerAccount(RegisterModel registerModel) {
        AccountModel accountModel = registerModel.account();
        Account account = new Account();
        account.setEmail(accountModel.email());
        account.setPassword(passwordEncoder.encode(accountModel.password()));
        account.setType(registerModel.type());
        account = accountRepository.save(account);

        // registerModel should be valid, just check the instance type
        if (registerModel.accountInfo() instanceof ServiceProviderInfoModel infoModel) {
            ServiceProvider serviceProvider = new ServiceProvider(account);
            serviceProvider.setName(infoModel.name());
            serviceProvider.setDescription(infoModel.description());
            // TODO Services
            serviceProvider.setPhoneNumber(infoModel.phoneNumber());
            serviceProvider.setContactEmailAddress(infoModel.contactEmailAddress());

            serviceProvider = serviceProviderRepository.save(serviceProvider);
        } else if (registerModel.accountInfo() instanceof CustomerInfoModel infoModel) {
            Customer customer = new Customer(account);
            customer.setFirstName(infoModel.firstName());
            customer.setMiddleName(infoModel.middleName());
            customer.setLastName(infoModel.lastName());
            customer.setAddress(infoModel.address());
            customer.setPhoneNumber(infoModel.phoneNumber());

            customer = customerRepository.save(customer);
        }

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
        account = accountRepository.save(account);

        URI verifyUri = uriBuilderFactory
                .uriString("/api/verify")
                .queryParam("token", "{token}")
                .build(account.getVerificationToken());

        emailService.sendEmail(account.getEmail(), "email-verification", Map.of(
                "token-url", verifyUri.toASCIIString()
        ));
    }

    @Override
    public boolean verifyAccount(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Account account = accountRepository.findByVerificationToken(token);
        // TODO should tokens expire?
        if (account == null) {
            return false; // TODO exception?
        }
        account.setVerified(true);
        account.setVerificationToken(null);
        accountRepository.save(account);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username);
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
