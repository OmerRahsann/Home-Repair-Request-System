package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestMailConfig;
import homerep.springy.controller.AccountController;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.exception.ApiException;
import homerep.springy.model.ChangePasswordModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.AccountService;
import homerep.springy.type.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestMailConfig.class)
@TestDatabaseConfig
public class AccountInfoTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountController accountController;

    @Autowired
    private AccountService accountService;

    @Autowired
    private DummyDataComponent dummyDataComponent;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Customer customer;

    private ServiceProvider serviceProvider;

    private User customerUser;
    private User serviceProviderUser;
    private User unverifiedUser;

    private static final String CUSTOMER_EMAIL = "example@example.com";
    private static final String SERVICE_PROVIDER_EMAIL = "test@localhost";
    private static final String UNVERIFIED_EMAIL = "unverified@localhost";

    private static final String INITIAL_PASSWORD = "TestPassword1";
    private static final String INCORRECT_PASSWORD = "Hunter42";
    private static final String NEW_PASSWORD = "TheNewPassword!";

    @BeforeEach
    void reset() {
        customer = dummyDataComponent.createCustomer(CUSTOMER_EMAIL);
        customerUser = new User(customer.getAccount());
        serviceProvider = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_EMAIL);
        serviceProviderUser = new User(serviceProvider.getAccount());

        ServiceProvider unverifiedProvider = dummyDataComponent.createServiceProvider(UNVERIFIED_EMAIL);
        unverifiedProvider.getAccount().setVerified(false);
        unverifiedUser = new User(unverifiedProvider.getAccount());
        accountRepository.save(unverifiedProvider.getAccount());
    }

    private Stream<Arguments> allUsers() {
        return Stream.of(
                Arguments.of(customerUser, AccountType.CUSTOMER, true),
                Arguments.of(serviceProviderUser, AccountType.SERVICE_PROVIDER, true),
                Arguments.of(unverifiedUser, AccountType.SERVICE_PROVIDER, false)
        );
    }

    @ParameterizedTest
    @MethodSource("allUsers")
    void getAccountInfoTests(User user, AccountType type, boolean isVerified) {
        assertEquals(type, accountController.getAccountType(user));
        assertEquals(isVerified, accountController.isVerified(user));
        assertEquals(user.getUsername(), accountController.getEmail(user));
    }

    @Test
    void updateCustomerInfo() {
        CustomerInfoModel infoModel = new CustomerInfoModel(
                dummyDataComponent.generateDummySentence(),
                dummyDataComponent.generateDummySentence(),
                dummyDataComponent.generateDummySentence(),
                "220 Rowan Boulevard, Rowan, Glassboro, NJ, USA",
                "9999999999"
        );
        accountService.updateCustomerInfo(customer, infoModel);
        CustomerInfoModel newInfoModel = accountController.getCustomerInfo(customerUser);
        assertEquals(infoModel, newInfoModel);
    }

    @Test
    void getCustomerInfo() {
        CustomerInfoModel infoModel = accountController.getCustomerInfo(customerUser);
        assertEquals(CustomerInfoModel.fromEntity(customer), infoModel);
    }

    @Test
    @Transactional
    void getServiceProviderInfo() {
        ServiceProviderInfoModel infoModel = accountController.getServiceProviderInfo(serviceProviderUser);
        assertEquals(ServiceProviderInfoModel.fromEntity(serviceProvider), infoModel);
    }

    @Test
    @Transactional
    void updateServiceProviderInfo() {
        ServiceProviderInfoModel infoModel = new ServiceProviderInfoModel(
                dummyDataComponent.generateDummySentence(),
                dummyDataComponent.generateDummySentence(),
                List.of("Roofwork", "Magic"),
                "9999999999",
                "220 Rowan Boulevard, Rowan, Glassboro, NJ, USA",
                SERVICE_PROVIDER_EMAIL
        );
        accountService.updateServiceProviderInfo(serviceProvider, infoModel);
        ServiceProviderInfoModel newInfoModel = accountController.getServiceProviderInfo(serviceProviderUser);
        assertEquals(infoModel, newInfoModel);
    }

    @Test
    @Transactional
    void changePassword() {
        // Add the initial password to the account
        Account account = customer.getAccount();
        account.setPassword(passwordEncoder.encode(INITIAL_PASSWORD));
        account = accountRepository.save(account);
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        // Attempting to update the password with an incorrect current password does not work
        assertFalse(accountService.changePassword(account, new ChangePasswordModel(
                INCORRECT_PASSWORD,
                NEW_PASSWORD
        )));
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(INCORRECT_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
        // Updating the password with the correct current password works
        assertTrue(accountService.changePassword(account, new ChangePasswordModel(
                INITIAL_PASSWORD,
                NEW_PASSWORD
        )));
        assertFalse(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
    }

    @Test
    @Transactional
    void changePasswordController() {
        // Add the initial password to the account
        Account account = customer.getAccount();
        account.setPassword(passwordEncoder.encode(INITIAL_PASSWORD));
        account = accountRepository.save(account);
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        // Attempting to update the password with an incorrect current password throws an ApiException
        ApiException exception = assertThrows(ApiException.class, () -> accountController.changePassword(new ChangePasswordModel(
                INCORRECT_PASSWORD,
                NEW_PASSWORD
        ), customerUser));
        assertEquals("incorrect_password", exception.getType());
        // and does not change the password
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(INCORRECT_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
        // Updating the password with the correct current password works
        assertDoesNotThrow(() -> accountController.changePassword(new ChangePasswordModel(
                INITIAL_PASSWORD,
                NEW_PASSWORD
        ), customerUser));
        assertFalse(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
    }

    @Test
    void notLoggedIn() throws Exception {
        this.mvc.perform(get("/api/account/type"))
                .andExpect(status().isForbidden());
        this.mvc.perform(get("/api/account/verified"))
                .andExpect(status().isForbidden());

        this.mvc.perform(get("/api/account/customer"))
                .andExpect(status().isForbidden());
        this.mvc.perform(post("/api/account/customer/update"))
                .andExpect(status().isForbidden());

        this.mvc.perform(get("/api/account/provider"))
                .andExpect(status().isForbidden());
        this.mvc.perform(post("/api/account/provider/update"))
                .andExpect(status().isForbidden());

        this.mvc.perform(post("/api/account/change_password"))
                .andExpect(status().isForbidden());
    }
}
