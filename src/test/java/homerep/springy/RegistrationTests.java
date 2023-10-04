package homerep.springy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.store.FolderException;
import homerep.springy.config.TestMailConfig;
import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;
import homerep.springy.repository.AccountRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestMailConfig.class)
class RegistrationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private GreenMailBean greenMailBean;

	private static final String TEST_EMAIL = "example@example.com";
	private static final String TEST_PASSWORD = "ProAsHeckZoey";
	private static final String TEST2_EMAIL = "example2@example.com";
	private static final String TEST2_PASSWORD = "Hunter42";
	private static final String INVALID_TOKEN = "ObviouslyNotAToken";

	@BeforeEach
	void reset() throws FolderException {
		// Clean slate for each test
		greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
		accountRepository.deleteAll();
	}

	@Test
	void verificationEmailTest() throws Exception {
		assertEquals(0, greenMailBean.getReceivedMessages().length);
		// Register an account
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
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
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
				.andExpect(status().isBadRequest());
		// and no verification email is sent
		assertEquals(1, greenMailBean.getReceivedMessages().length);
	}

	@Test
	void registerLoginTest() throws Exception {
		// Register an account
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
				.andExpect(status().isOk());
		// An account is created and stored to the database
		Account account = accountRepository.findByEmail(TEST_EMAIL);
		assertEquals(TEST_EMAIL, account.getEmail());
		assertTrue(passwordEncoder.matches(TEST_PASSWORD, account.getPassword()));
		assertFalse(account.isVerified());
		assertNotNull(account.getVerificationToken());

		// Invalid verification tokens are rejected
		this.mvc.perform(get("/api/verify?token={token}", INVALID_TOKEN))
				.andExpect(status().isBadRequest());
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
				.andExpect(status().isBadRequest());
		// Authenticated after login
		this.mvc.perform(postJson("/api/login", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(authenticated());
	}

	@Test
	void registerMultipleTest() throws Exception {
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
				.andExpect(status().isOk());
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST2_EMAIL, TEST2_PASSWORD)))
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
	void registerEmailValidation() throws Exception {
		// Valid email addresses
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST_EMAIL, TEST_PASSWORD)))
				.andExpect(status().isOk());
		this.mvc.perform(postJson("/api/register", new AccountModel(TEST2_EMAIL, TEST_PASSWORD)))
				.andExpect(status().isOk());
		// Valid email addresses taken from Wikipedia
		this.mvc.perform(postJson("/api/register", new AccountModel("a@localhost", TEST_PASSWORD)))
				.andExpect(status().isOk());
		this.mvc.perform(postJson("/api/register", new AccountModel("long.email-address-with-hyphens@and.subdomains.example.com", TEST_PASSWORD)))
				.andExpect(status().isOk());
		this.mvc.perform(postJson("/api/register", new AccountModel("user.name+tag+sorting@example.com", TEST_PASSWORD)))
				.andExpect(status().isOk());
		// Invalid email addresses taken from Wikipedia
		this.mvc.perform(postJson("/api/register", new AccountModel("abc.example.com", TEST_PASSWORD)))
				.andExpect(status().isBadRequest());
		this.mvc.perform(postJson("/api/register", new AccountModel("a@b@c@example.com", TEST_PASSWORD)))
				.andExpect(status().isBadRequest());
	}

	private MockHttpServletRequestBuilder postJson(String url, Object content) throws JsonProcessingException {
		return post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(content));
	}
}
