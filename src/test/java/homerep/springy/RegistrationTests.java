package homerep.springy;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;
import homerep.springy.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RegistrationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String TEST_EMAIL = "example@example.com";
	private static final String TEST_PASSWORD = "example@example.com";
	private static final String INVALID_TOKEN = "ObviouslyNotAToken";

	@Test
	void registerLoginTest() throws Exception {
		// Register an account
		this.mvc.perform(post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new AccountModel(TEST_EMAIL, TEST_PASSWORD))))
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
		this.mvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new AccountModel(TEST_EMAIL, TEST_PASSWORD))))
				.andExpect(status().isOk())
				.andExpect(authenticated());
	}

}
