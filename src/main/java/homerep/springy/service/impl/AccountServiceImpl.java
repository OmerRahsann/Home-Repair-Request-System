package homerep.springy.service.impl;

import homerep.springy.authorities.Verified;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.type.Token;
import homerep.springy.model.AccountModel;
import homerep.springy.model.RegisterModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.resetpassword.ResetPasswordModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.service.AccountService;
import homerep.springy.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
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

    @Value("${homerep.require-verification:#{true}}")
    private boolean requireVerification;

    // TODO configurable expiration
    private static final Duration RESET_TOKEN_VALID_DURATION = Duration.ofHours(2);

    // TODO configurable expiration
    // Time after token creation in which a new token can be created
    private static final Duration RESET_TOKEN_REFRESH_DURATION = RESET_TOKEN_VALID_DURATION.minus(Duration.ofMinutes(90));

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
        account.setVerified(!requireVerification);
        account = accountRepository.save(account);

        // registerModel should be valid, just check the instance type
        if (registerModel.accountInfo() instanceof ServiceProviderInfoModel infoModel) {
            ServiceProvider serviceProvider = new ServiceProvider(account);
            serviceProvider.setName(infoModel.name());
            serviceProvider.setDescription(infoModel.description());
            serviceProvider.setServices(infoModel.services());
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
        emailService.sendEmail(account.getEmail(), "email-verification", Map.of(
                "token", URLEncoder.encode(account.getVerificationToken(), StandardCharsets.US_ASCII)
        ));
    }

    @Override
    public boolean verifyAccount(String token) {
        Account account = accountRepository.findByVerificationToken(token);
        if (account == null) {
            return false;
        }
        account.setVerified(true);
        account.setVerificationToken(null);
        accountRepository.save(account);
        return true;
    }

    @Override
    public void sendResetPassword(String email) {
        Account account = accountRepository.findByEmail(email);
        if (account == null || !account.isVerified()) {
            return;
        }
        if (account.getResetPasswordToken() != null) {
            if (!account.getResetPasswordToken().canRefresh()) {
                return;
            }
        }

        Instant now = Instant.now();
        Instant refreshAt = now.plus(RESET_TOKEN_REFRESH_DURATION);
        Instant expireAt = now.plus(RESET_TOKEN_VALID_DURATION);
        account.setResetPasswordToken(new Token(refreshAt, expireAt));
        account = accountRepository.save(account);
        emailService.sendEmail(account.getEmail(), "reset-password", Map.of(
                "token", URLEncoder.encode(account.getResetPasswordToken().getVal(), StandardCharsets.US_ASCII)
        ));
    }

    @Override
    public boolean resetPassword(ResetPasswordModel resetPasswordModel) {
        Account account = accountRepository.findByResetPasswordTokenVal(resetPasswordModel.token());
        if (account == null) {
            return false;
        }
        if (account.getResetPasswordToken().isExpired()) {
            account.setResetPasswordToken(null);
            accountRepository.save(account);
            return false;
        }
        // TODO invalidate sessions
        account.setResetPasswordToken(null);
        account.setPassword(passwordEncoder.encode(resetPasswordModel.password()));
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
                authorities.add(Verified.INSTANCE);
            }
            return new User(account.getEmail(), account.getPassword(), authorities);
        }
        return null;
    }
}
