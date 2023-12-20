package homerep.springy;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.model.AccountModel;
import homerep.springy.model.RegisterModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.resetpassword.SendResetPasswordModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestDatabaseConfig
public class RateLimitTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private static final RegisterModel REGISTER_MODEL = new RegisterModel(
            new AccountModel("test@localhost", "ProAsHeckZoey"),
            AccountType.CUSTOMER,
            new CustomerInfoModel("Zoey", "", "Proasheck",
                    "201 Mullica Hill Rd, Glassboro, NJ 08028", "8562564000")
    );
    private static final SendResetPasswordModel SEND_RESET_PASSWORD_MODEL = new SendResetPasswordModel("test@localhost");

    @Test
    void registrationRateLimit() throws Exception {
        this.mvc.perform(postJson("/api/register", REGISTER_MODEL))
                .andExpect(status().isOk());
        this.mvc.perform(postJson("/api/register", REGISTER_MODEL))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void resetPasswordRateLimit() throws Exception {
        this.mvc.perform(postJson("/api/reset_password/send", SEND_RESET_PASSWORD_MODEL))
                .andExpect(status().isOk());
        this.mvc.perform(postJson("/api/reset_password/send", SEND_RESET_PASSWORD_MODEL))
                .andExpect(status().isTooManyRequests());
    }

    private MockHttpServletRequestBuilder postJson(String url, Object content) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(content));
    }
}
