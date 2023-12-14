package homerep.springy.service.impl;

import homerep.springy.config.AccountServiceConfig;
import homerep.springy.config.EmailAllowListConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.type.Token;
import homerep.springy.model.AccountModel;
import homerep.springy.model.RegisterModel;
import homerep.springy.model.ChangePasswordModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.resetpassword.ResetPasswordModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.service.AccountService;
import homerep.springy.service.EmailService;
import homerep.springy.type.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    private SessionRegistry sessionRegistry;

    @Autowired
    private SessionRepository<? extends Session> sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountServiceConfig accountServiceConfig;

    @Autowired
    private EmailAllowListConfig emailAllowListConfig;

    // TODO configurable expiration
    private static final Duration RESET_TOKEN_VALID_DURATION = Duration.ofHours(2);

    // TODO configurable expiration
    // Time after token creation in which a new token can be created
    private static final Duration RESET_TOKEN_REFRESH_DURATION = RESET_TOKEN_VALID_DURATION.minus(Duration.ofMinutes(90));

    @Override
    public boolean isAllowedEmail(String email) {
        if (!emailAllowListConfig.isEnabled()) {
            return true;
        }
        return emailAllowListConfig.getEmails().contains(email);
    }

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
        account.setVerified(!accountServiceConfig.isRequireVerification());
        account = accountRepository.save(account);

        // registerModel should be valid, just check the instance type
        if (registerModel.accountInfo() instanceof ServiceProviderInfoModel infoModel) {
            ServiceProvider serviceProvider = new ServiceProvider(account);
            updateServiceProviderInfo(serviceProvider, infoModel);
        } else if (registerModel.accountInfo() instanceof CustomerInfoModel infoModel) {
            Customer customer = new Customer(account);
            updateCustomerInfo(customer, infoModel);
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
                "token", URLEncoder.encode(account.getResetPasswordToken().getValue(), StandardCharsets.US_ASCII),
                "expire_at", String.valueOf(account.getResetPasswordToken().getExpireAt().getEpochSecond())
        ));
    }

    @Override
    public boolean resetPassword(ResetPasswordModel resetPasswordModel) {
        Account account = accountRepository.findByResetPasswordTokenValue(resetPasswordModel.token());
        if (account == null) {
            return false;
        }
        if (account.getResetPasswordToken().isExpired()) {
            account.setResetPasswordToken(null);
            accountRepository.save(account);
            return false;
        }
        account.setResetPasswordToken(null);
        account.setPassword(passwordEncoder.encode(resetPasswordModel.password()));
        account = accountRepository.save(account);
        logoutSessions(account);
        return true;
    }

    @Override
    public boolean changePassword(Account account, ChangePasswordModel model) {
        if (!passwordEncoder.matches(model.currentPassword(), account.getPassword())) {
            return false;
        }
        account.setPassword(passwordEncoder.encode(model.newPassword()));
        account = accountRepository.save(account);
        logoutSessions(account);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("No user with the given email was found.");
        }
        return new User(account);
    }

    @Override
    public void updateCustomerInfo(Customer customer, CustomerInfoModel infoModel) {
        customer.setFirstName(infoModel.firstName());
        customer.setMiddleName(infoModel.middleName());
        customer.setLastName(infoModel.lastName());
        customer.setAddress(infoModel.address());
        customer.setPhoneNumber(infoModel.phoneNumber());
        customerRepository.save(customer);
    }

    @Override
    public void updateServiceProviderInfo(ServiceProvider serviceProvider, ServiceProviderInfoModel infoModel) {
        serviceProvider.setName(infoModel.name());
        serviceProvider.setDescription(infoModel.description());
        serviceProvider.setServices(new ArrayList<>(infoModel.services()));
        serviceProvider.setPhoneNumber(infoModel.phoneNumber());
        serviceProvider.setAddress(infoModel.address());
        serviceProvider.setContactEmailAddress(infoModel.contactEmailAddress());
        serviceProviderRepository.save(serviceProvider);
    }

    @Override
    public void logoutSessions(Account account) {
        for (SessionInformation session : sessionRegistry.getAllSessions(account.getEmail(), false)) {
            session.expireNow();
            sessionRepository.deleteById(session.getSessionId());
        }
    }
}
