package homerep.springy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;
import homerep.springy.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class LoginTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "example@example.com";
    private static final String TEST_PASSWORD = "ProAsHeckZoey";

    private static final String TEST2_EMAIL = "test@localhost";
    private static final String TEST2_PASSWORD = "Hunter42";

    private static final String WRONG_EMAIL = "newperson@localhost";
    private static final String WRONG_PASSWORD = "OpenSesame!";

    private static final String INVALID_EMAIL = "localhost";

    @BeforeEach
    void reset() {
        // Clean slate for each test
        accountRepository.deleteAll();

        Account account = new Account();
        account.setEmail(TEST_EMAIL);
        account.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        account.setType(AccountType.SERVICE_REQUESTER);
        account.setVerified(true);
        accountRepository.save(account);

        Account account2 = new Account();
        account2.setEmail(TEST2_EMAIL);
        account2.setPassword(passwordEncoder.encode(TEST2_PASSWORD));
        account2.setType(AccountType.SERVICE_PROVIDER);
        account2.setVerified(true);
        accountRepository.save(account2);
    }

    @Test
    void loginLogoutTest() throws Exception {
        // Successful login as service requester
        this.mvc.perform(login(TEST_EMAIL, TEST_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(authenticated()
                        .withUsername(TEST_EMAIL)
                        .withAuthorities(List.of(AccountType.SERVICE_REQUESTER, Verified.INSTANCE)))
                .andExpect(cookie().exists("SESSION"))
                .andExpect(cookie().httpOnly("SESSION", true))
                .andExpect(cookie().attribute("SESSION", "SameSite", "Lax"));
        // Can logout
        this.mvc.perform(get("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());
        // Can log out multiple times without issue
        this.mvc.perform(get("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(unauthenticated());

        // Successful login as service provider
        this.mvc.perform(login(TEST2_EMAIL, TEST2_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(authenticated()
                        .withUsername(TEST2_EMAIL)
                        .withAuthorities(List.of(AccountType.SERVICE_PROVIDER, Verified.INSTANCE)));
        // Can log in to another account without logout
        this.mvc.perform(login(TEST_EMAIL, TEST_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(authenticated()
                        .withUsername(TEST_EMAIL)
                        .withAuthorities(List.of(AccountType.SERVICE_REQUESTER, Verified.INSTANCE)));
    }

    @Test
    void invalidLoginTest() throws Exception {
        // Wrong password
        this.mvc.perform(login(TEST_EMAIL, WRONG_PASSWORD))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
        // Non existent account
        this.mvc.perform(login(WRONG_EMAIL, TEST_PASSWORD))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
        this.mvc.perform(login(WRONG_EMAIL, TEST2_PASSWORD))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
    }

    @Test
    void loginValidationTest() throws Exception {
        this.mvc.perform(login(INVALID_EMAIL, TEST_PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty())
                .andExpect(jsonPath("objectErrors").isArray())
                .andExpect(jsonPath("objectErrors").isEmpty());
    }

    private MockHttpServletRequestBuilder login(String email, String password) throws JsonProcessingException {
        return post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new AccountModel(email, password)));
    }
}
