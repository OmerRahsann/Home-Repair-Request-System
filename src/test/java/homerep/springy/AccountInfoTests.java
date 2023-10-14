package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestMailConfig;
import homerep.springy.entity.Account;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.ResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestMailConfig.class)
public class AccountInfoTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ResetService resetService;

    private static final String CUSTOMER_EMAIL = "example@example.com";
    private static final String SERVICE_PROVIDER_EMAIL = "test@localhost";
    private static final String UNVERIFIED_EMAIL = "unverified@localhost";

    @BeforeEach
    void reset() {
        resetService.resetAll();

        Account account = new Account();
        account.setEmail(CUSTOMER_EMAIL);
        account.setPassword(null); // Not testing auth
        account.setType(AccountType.CUSTOMER);
        account.setVerified(true);
        accountRepository.save(account);

        Account account2 = new Account();
        account2.setEmail(SERVICE_PROVIDER_EMAIL);
        account2.setPassword(null); // Not testing auth
        account2.setType(AccountType.SERVICE_PROVIDER);
        account2.setVerified(true);
        accountRepository.save(account2);

        Account account3 = new Account();
        account3.setEmail(UNVERIFIED_EMAIL);
        account3.setPassword(null); // Not testing auth
        account3.setType(AccountType.SERVICE_PROVIDER);
        account3.setVerified(false);
        accountRepository.save(account3);
    }

    @Test
    @WithMockUser(username = CUSTOMER_EMAIL)
    void customer() throws Exception {
        this.mvc.perform(get("/api/account/type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString())
                .andExpect(jsonPath("$").value(AccountType.CUSTOMER.toString()));
        this.mvc.perform(get("/api/account/verified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL)
    void serviceProvider() throws Exception {
        this.mvc.perform(get("/api/account/type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString())
                .andExpect(jsonPath("$").value(AccountType.SERVICE_PROVIDER.toString()));
        this.mvc.perform(get("/api/account/verified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @WithMockUser(username = UNVERIFIED_EMAIL)
    void unverified() throws Exception {
        this.mvc.perform(get("/api/account/type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString())
                .andExpect(jsonPath("$").value(AccountType.SERVICE_PROVIDER.toString()));
        this.mvc.perform(get("/api/account/verified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void notLoggedIn() throws Exception {
        this.mvc.perform(get("/api/account/type"))
                .andExpect(status().isForbidden());
        this.mvc.perform(get("/api/account/verified"))
                .andExpect(status().isForbidden());
    }
}
