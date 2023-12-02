package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestMailConfig;
import homerep.springy.controller.AccountController;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private Customer customer;

    private ServiceProvider serviceProvider;

    private static final String CUSTOMER_EMAIL = "example@example.com";
    private static final User CUSTOMER_USER = new User(CUSTOMER_EMAIL, "", List.of(AccountType.CUSTOMER, Verified.INSTANCE));

    private static final String SERVICE_PROVIDER_EMAIL = "test@localhost";
    private static final User SERVICE_PROVIDER_USER = new User(SERVICE_PROVIDER_EMAIL, "", List.of(AccountType.SERVICE_PROVIDER, Verified.INSTANCE));
    private static final String UNVERIFIED_EMAIL = "unverified@localhost";
    private static final User UNVERIFIED_USER = new User(UNVERIFIED_EMAIL, "", List.of(AccountType.SERVICE_PROVIDER));

    @BeforeEach
    void reset() {
        customer = dummyDataComponent.createCustomer(CUSTOMER_EMAIL);
        serviceProvider = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_EMAIL);

        ServiceProvider unverifiedProvider = dummyDataComponent.createServiceProvider(UNVERIFIED_EMAIL);
        unverifiedProvider.getAccount().setVerified(false);
        accountRepository.save(unverifiedProvider.getAccount());
    }

    private static Stream<Arguments> allUsers() {
        return Stream.of(
                Arguments.of(CUSTOMER_USER, AccountType.CUSTOMER, true),
                Arguments.of(SERVICE_PROVIDER_USER, AccountType.SERVICE_PROVIDER, true),
                Arguments.of(UNVERIFIED_USER, AccountType.SERVICE_PROVIDER, false)
        );
    }

    @ParameterizedTest
    @MethodSource("allUsers")
    void getAccountInfoTests(User user, AccountType type, boolean isVerified) {
        assertEquals(type, accountController.getAccountType(user));
        assertEquals(isVerified, accountController.isVerified(user));
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
        CustomerInfoModel newInfoModel = accountController.getCustomerInfo(CUSTOMER_USER);
        assertEquals(infoModel, newInfoModel);
    }

    @Test
    void getCustomerInfo() {
        CustomerInfoModel infoModel = accountController.getCustomerInfo(CUSTOMER_USER);
        assertEquals(CustomerInfoModel.fromEntity(customer), infoModel);
    }

    @Test
    @Transactional
    void getServiceProviderInfo() {
        ServiceProviderInfoModel infoModel = accountController.getServiceProviderInfo(SERVICE_PROVIDER_USER);
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
        ServiceProviderInfoModel newInfoModel = accountController.getServiceProviderInfo(SERVICE_PROVIDER_USER);
        assertEquals(infoModel, newInfoModel);
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
    }
}
