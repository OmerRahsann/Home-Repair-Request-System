package homerep.springy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.store.FolderException;
import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestDisableRateLimitConfig;
import homerep.springy.config.TestMailConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.type.Token;
import homerep.springy.model.resetpassword.ResetPasswordModel;
import homerep.springy.model.resetpassword.SendResetPasswordModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.AccountService;
import jakarta.mail.internet.MimeMessage;
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

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({TestMailConfig.class, TestDisableRateLimitConfig.class})
@TestDatabaseConfig
public class ResetPasswordTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GreenMailBean greenMailBean;

    private static final String VERIFIED_EMAIL = "test@localhost";
    private static final String UNVERIFIED_EMAIL = "test2@localhost";
    private static final String INITIAL_PASSWORD = "ProAsHeckZoey";
    private static final String NEW_PASSWORD = "Hunter42";
    private static final String UNUSED_EMAIL = "noreply@localhost";
    private static final String INVALID_EMAIL = "example";
    private static final String INVALID_TOKEN = "ObviouslyNotAToken";

    @BeforeEach
    void reset() throws FolderException {
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();

        Account account = new Account();
        account.setEmail(VERIFIED_EMAIL);
        account.setPassword(passwordEncoder.encode(INITIAL_PASSWORD));
        account.setType(AccountType.CUSTOMER);
        account.setVerified(true);
        account = accountRepository.save(account);

        Account account2 = new Account();
        account2.setEmail(UNVERIFIED_EMAIL);
        account2.setPassword(passwordEncoder.encode(INITIAL_PASSWORD));
        account2.setType(AccountType.CUSTOMER);
        account2.setVerified(false);
        account2 = accountRepository.save(account2);
    }

    @Test
    void resetPassword() throws Exception {
        // Initial state
        // No emails
        assertEquals(0, greenMailBean.getReceivedMessages().length);

        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        // No reset password token
        assertNull(account.getResetPasswordToken());

        // When requesting a password reset
        accountService.sendResetPassword(VERIFIED_EMAIL);
        // A reset password token is created
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        Token token = account.getResetPasswordToken();
        assertNotNull(token);
        // that is not expired and not refreshable
        assertFalse(token.canRefresh());
        assertFalse(token.isExpired());

        // An email is sent
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // only to the registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(VERIFIED_EMAIL, message.getAllRecipients()[0].toString());
        // with a link to reset the account's password
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        String tokenUrl = "http://localhost:3000/reset_password?token=" + account.getResetPasswordToken().getVal();
        assertTrue(content.contains(tokenUrl));

        // Token can be used to reset the password
        ResetPasswordModel model = new ResetPasswordModel(token.getVal(), NEW_PASSWORD);
        assertTrue(accountService.resetPassword(model));
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        // password is changed
        assertFalse(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
        // token is removed
        assertNull(account.getResetPasswordToken());
    }

    @Test
    void resetPasswordRefresh() {
        // Initial state
        // No emails
        assertEquals(0, greenMailBean.getReceivedMessages().length);

        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        // No reset password token
        assertNull(account.getResetPasswordToken());

        // When requesting a password reset
        accountService.sendResetPassword(VERIFIED_EMAIL);
        // A reset password token is created
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        Token token = account.getResetPasswordToken();
        String tokenValue = token.getVal();
        Instant refreshAt = token.getRefreshAt();
        Instant expireAt = token.getExpireAt();
        assertNotNull(token);
        // that is not expired and not refreshable
        assertFalse(token.canRefresh());
        assertFalse(token.isExpired());

        // Requesting a second password reset before the refresh duration does nothing
        assertFalse(token.canRefresh());
        accountService.sendResetPassword(VERIFIED_EMAIL);
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        assertEquals(tokenValue, account.getResetPasswordToken().getVal());
        assertEquals(refreshAt, account.getResetPasswordToken().getRefreshAt());
        assertEquals(expireAt, account.getResetPasswordToken().getExpireAt());

        // Force the token to be refreshable
        account.getResetPasswordToken().setRefreshAt(Instant.now().minus(Duration.ofMinutes(1)));
        account = accountRepository.save(account);
        assertTrue(account.getResetPasswordToken().canRefresh());
        // Requesting a password reset after the refresh duration works
        accountService.sendResetPassword(VERIFIED_EMAIL);
        assertEquals(2, greenMailBean.getReceivedMessages().length);
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        // and refreshes the token
        assertNotEquals(tokenValue, account.getResetPasswordToken().getVal());
        assertNotEquals(refreshAt, account.getResetPasswordToken().getRefreshAt());
        assertNotEquals(expireAt, account.getResetPasswordToken().getExpireAt());
        assertFalse(account.getResetPasswordToken().canRefresh());
        assertFalse(account.getResetPasswordToken().isExpired());
    }

    @Test
    void resetPasswordExpired() {
        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        // No reset password token
        assertNull(account.getResetPasswordToken());
        // When requesting a password reset
        accountService.sendResetPassword(VERIFIED_EMAIL);
        // A reset password token is created
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        Token token = account.getResetPasswordToken();
        assertNotNull(token);
        // Force the token to be expired
        account.getResetPasswordToken().setExpireAt(Instant.now().minus(Duration.ofMinutes(1)));
        assertTrue(account.getResetPasswordToken().isExpired());
        account = accountRepository.save(account);
        // Trying to use this expired token does not work
        ResetPasswordModel model = new ResetPasswordModel(token.getVal(), NEW_PASSWORD);
        assertFalse(accountService.resetPassword(model));
        // token is removed
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        assertNull(account.getResetPasswordToken());
        // and password is not changed
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
    }

    @Test
    void resetPasswordUnusedEmail() {
        // No emails
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        accountRepository.deleteAll();
        assertTrue(accountRepository.findAll().isEmpty());
        // Trying to send a password reset for an email that is not used for an account does nothing
        accountService.sendResetPassword(UNUSED_EMAIL);
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        assertTrue(accountRepository.findAll().isEmpty());
    }

    @Test
    void resetPasswordUnverifiedEmail() {
        // No emails
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // Trying to send a password reset for an unverified account does nothing
        accountService.sendResetPassword(UNVERIFIED_EMAIL);
        // no emails are sent
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // no password reset token is created
        Account account = accountRepository.findByEmail(UNVERIFIED_EMAIL);
        assertNull(account.getResetPasswordToken());
        // and the password is not changed
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
    }

    @Test
    void resetPasswordInvalidToken() {
        accountService.sendResetPassword(VERIFIED_EMAIL);
        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        String tokenValue = account.getResetPasswordToken().getVal();
        // Trying to password reset with an invalid token does not work
        ResetPasswordModel model = new ResetPasswordModel(INVALID_TOKEN, NEW_PASSWORD);
        assertFalse(accountService.resetPassword(model));
        // Password is not changed
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
        // token is not changed
        assertEquals(tokenValue, account.getResetPasswordToken().getVal());
    }

    @Test
    void resetPasswordEndpoint() throws Exception {
        // No emails
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // Can send a password reset to a verified account
        this.mvc.perform(postJson("/api/reset_password/send", new SendResetPasswordModel(VERIFIED_EMAIL)))
            .andExpect(status().isOk());
        // A reset password token is created
        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        Token token = account.getResetPasswordToken();
        assertNotNull(token);
        // and an email is sent
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // only to the registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(VERIFIED_EMAIL, message.getAllRecipients()[0].toString());

        // The token can be used to reset the password
        this.mvc.perform(postJson("/api/reset_password", new ResetPasswordModel(token.getVal(), NEW_PASSWORD)))
                .andExpect(status().isOk());
        // Token is removed
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        assertNull(account.getResetPasswordToken());
        // Password is changed
        assertFalse(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
    }

    @Test
    void resetPasswordEndpointUnverified() throws Exception {
        // No emails
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // Trying to send a password reset to an unverified account does nothing
        this.mvc.perform(postJson("/api/reset_password/send", new SendResetPasswordModel(UNVERIFIED_EMAIL)))
                .andExpect(status().isOk());
        // A reset password token is not created
        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        assertNull(account.getResetPasswordToken());
        // and no email is sent
        assertEquals(0, greenMailBean.getReceivedMessages().length);
    }

    @Test
    void resetPasswordEndpointValidation() throws Exception {
        // Trying to send garbage to the /send endpoint is rejected
        this.mvc.perform(postJson("/api/reset_password/send", new ResetPasswordModel(null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty());
        this.mvc.perform(postJson("/api/reset_password/send", new SendResetPasswordModel(INVALID_EMAIL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty());

        // Can send a password reset to a verified account
        this.mvc.perform(postJson("/api/reset_password/send", new SendResetPasswordModel(VERIFIED_EMAIL)))
                .andExpect(status().isOk());
        // A reset password token is created
        Account account = accountRepository.findByEmail(VERIFIED_EMAIL);
        Token token = account.getResetPasswordToken();
        assertNotNull(token);

        // Trying to use the token to change it to an invalid password does not work
        this.mvc.perform(postJson("/api/reset_password", new ResetPasswordModel(token.getVal(), "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty());
        // Token is not removed
        account = accountRepository.findByEmail(VERIFIED_EMAIL);
        assertNotNull(account.getResetPasswordToken());
        assertEquals(token.getVal(), account.getResetPasswordToken().getVal());
        // Password is not changed
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches("", account.getPassword()));
    }

    @Test
    void resetPasswordEndpointInvalidToken() throws Exception {
        // Trying to use an invalid token does nothing
        this.mvc.perform(postJson("/api/reset_password", new ResetPasswordModel(INVALID_TOKEN, NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("invalid_token"));
    }

    private MockHttpServletRequestBuilder postJson(String url, Object content) throws JsonProcessingException {
        return post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
    }
}
