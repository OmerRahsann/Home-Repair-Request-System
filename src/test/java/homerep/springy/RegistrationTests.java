package homerep.springy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.store.FolderException;
import homerep.springy.authorities.AccountType;
import homerep.springy.config.AccountServiceConfig;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestMailConfig;
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
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestMailConfig.class)
@TestDatabaseConfig
class RegistrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountServiceConfig accountServiceConfig;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GreenMailBean greenMailBean;

    private static final String TEST_EMAIL = "example@example.com";
    private static final String TEST_PASSWORD = "ProAsHeckZoey";
    private static final String TEST2_EMAIL = "example2@example.com";
    private static final String TEST2_PASSWORD = "Hunter42";
    private static final String INVALID_TOKEN = "ObviouslyNotAToken";

    private static final RegisterModel VALID_CUSTOMER1 = new RegisterModel(
            new AccountModel(TEST_EMAIL, TEST_PASSWORD),
            AccountType.CUSTOMER,
            new CustomerInfoModel("Zoey", "", "Proasheck",
                    "201 Mullica Hill Rd, Glassboro, NJ 08028", "8562564000")
    );

    private static final RegisterModel VALID_CUSTOMER2 = new RegisterModel(
            new AccountModel(TEST2_EMAIL, TEST2_PASSWORD),
            AccountType.CUSTOMER,
            new CustomerInfoModel("Marina", "", "Hale",
                    "201 Mullica Hill Rd, Glassboro, NJ 08028", "5105553456")
    );

    private static final RegisterModel VALID_SERVICE_PROVIDER = new RegisterModel(
            new AccountModel(TEST_EMAIL, TEST2_PASSWORD),
            AccountType.SERVICE_PROVIDER,
            new ServiceProviderInfoModel("Sakura HVAC and Plumbing", "We fix your HVAC for you!",
                    List.of("HVAC"), "3095550000", "201 Mullica Hill Rd, Glassboro, NJ 08028",
                    "contact@example.com")
    );

    @BeforeEach
    void reset() throws FolderException {
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
        accountServiceConfig.setRequireVerification(true);
    }

    @Test
    void verificationEmailTest() throws Exception {
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // Register an account
        this.mvc.perform(postJson("/api/register", VALID_CUSTOMER1))
                .andExpect(status().isOk());
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertNotNull(account);
        // A verification email is sent
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // only to the registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(TEST_EMAIL, message.getAllRecipients()[0].toString());
        // with a link to verify the account
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        String tokenUrl = "http://localhost:0/api/verify?token=" + account.getVerificationToken();
        assertTrue(content.contains(tokenUrl));

        // Attempting to register twice fails
        this.mvc.perform(postJson("/api/register", VALID_CUSTOMER1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("already_registered"));
        // and no verification email is sent
        assertEquals(1, greenMailBean.getReceivedMessages().length);
    }

    @Test
    void registerLoginTest() throws Exception {
        // Register an account
        this.mvc.perform(postJson("/api/register", VALID_CUSTOMER1))
                .andExpect(status().isOk());
        // An account is created and stored to the database
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertEquals(TEST_EMAIL, account.getEmail());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, account.getPassword()));
        assertFalse(account.isVerified());
        assertNotNull(account.getVerificationToken());

        // Invalid verification tokens are rejected
        this.mvc.perform(get("/api/verify?token={token}", INVALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("invalid_token"));
        // No change to the account
        Account afterFailedVerifyaccount = accountRepository.findByEmail(TEST_EMAIL);
        assertEquals(account, afterFailedVerifyaccount);
        // Use the correct verification token
        this.mvc.perform(get("/api/verify?token={token}", account.getVerificationToken()))
                .andExpect(status().isOk());
        // Verification status is updated
        Account afterVerifyaccount = accountRepository.findByEmail(TEST_EMAIL);
        assertTrue(afterVerifyaccount.isVerified());
        assertNull(afterVerifyaccount.getVerificationToken());
        // Token is invalidated
        this.mvc.perform(get("/api/verify?token={token}", account.getVerificationToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("invalid_token"));
        // Authenticated after login
        this.mvc.perform(postJson("/api/login", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(authenticated());
    }

    @Test
    void registerMultipleTest() throws Exception {
        this.mvc.perform(postJson("/api/register", VALID_CUSTOMER1))
                .andExpect(status().isOk());
        this.mvc.perform(postJson("/api/register", VALID_CUSTOMER2))
                .andExpect(status().isOk());
        List<Account> accounts = accountRepository.findAll();
        assertEquals(2, accounts.size());

        Account account1 = accountRepository.findByEmail(TEST_EMAIL);
        assertEquals(TEST_EMAIL, account1.getEmail());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, account1.getPassword()));
        assertFalse(account1.isVerified());
        assertNotNull(account1.getVerificationToken());

        Account account2 = accountRepository.findByEmail(TEST2_EMAIL);
        assertEquals(TEST2_EMAIL, account2.getEmail());
        assertTrue(passwordEncoder.matches(TEST2_PASSWORD, account2.getPassword()));
        assertFalse(account2.isVerified());
        assertNotNull(account2.getVerificationToken());
    }

    @Test
    void registerCustomer() throws Exception {
        this.mvc.perform(postJson("/api/register", VALID_CUSTOMER1))
                .andExpect(status().isOk());
        List<Account> accounts = accountRepository.findAll();
        assertEquals(1, accounts.size());
        List<Customer> customers = customerRepository.findAll();
        assertEquals(1, customers.size());
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        assertTrue(serviceProviders.isEmpty());
        // Account is stored to the database
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertEquals(TEST_EMAIL, account.getEmail());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, account.getPassword()));
        assertFalse(account.isVerified());
        assertNotNull(account.getVerificationToken());
        // An associated Customer is also stored
        CustomerInfoModel customerInfo = (CustomerInfoModel) VALID_CUSTOMER1.accountInfo();
        Customer customer = customerRepository.findByAccount(account);
        assertNotNull(customer);
        assertEquals(customerInfo.firstName(), customer.getFirstName());
        assertEquals(customerInfo.middleName(), customer.getMiddleName());
        assertEquals(customerInfo.lastName(), customer.getLastName());
        assertEquals(customerInfo.address(), customer.getAddress());
        assertEquals(customerInfo.phoneNumber(), customer.getPhoneNumber());
    }

    @Test
    @Transactional
    void registerServiceProvider() throws Exception {
        this.mvc.perform(postJson("/api/register", VALID_SERVICE_PROVIDER))
                .andExpect(status().isOk());
        List<Account> accounts = accountRepository.findAll();
        assertEquals(1, accounts.size());
        List<Customer> customers = customerRepository.findAll();
        assertTrue(customers.isEmpty());
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        assertEquals(1, serviceProviders.size());
        // Account is stored to the database
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertEquals(TEST_EMAIL, account.getEmail());
        assertTrue(passwordEncoder.matches(TEST2_PASSWORD, account.getPassword()));
        assertFalse(account.isVerified());
        assertNotNull(account.getVerificationToken());
        // An associated ServiceProvider is also stored
        ServiceProviderInfoModel serviceInfo = (ServiceProviderInfoModel) VALID_SERVICE_PROVIDER.accountInfo();
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccount(account);
        assertNotNull(serviceProvider);
        assertEquals(serviceInfo.name(), serviceProvider.getName());
        assertEquals(serviceInfo.description(), serviceProvider.getDescription());
        assertEquals(serviceInfo.services(), serviceProvider.getServices());
        assertEquals(serviceInfo.phoneNumber(), serviceProvider.getPhoneNumber());
        assertEquals(serviceInfo.address(), serviceProvider.getAddress());
        assertEquals(serviceInfo.contactEmailAddress(), serviceProvider.getContactEmailAddress());
    }

    @Test
    void registerValidation() throws Exception {
        this.mvc.perform(postJson("/api/register", new RegisterModel(
                        new AccountModel("notavalidemail", "short"),
                        AccountType.CUSTOMER,
                        new ServiceProviderInfoModel(null, null, List.of(), null, null, null)
                )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty())
                .andExpect(jsonPath("objectErrors").isArray())
                .andExpect(jsonPath("objectErrors").isNotEmpty());
    }

    private MockHttpServletRequestBuilder postJson(String url, Object content) throws JsonProcessingException {
        return post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
    }
}
