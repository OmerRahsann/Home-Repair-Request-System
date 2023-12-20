package homerep.springy;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.store.FolderException;
import homerep.springy.config.TestMailConfig;
import homerep.springy.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestMailConfig.class)
public class EmailServiceTest {
    @Autowired
    private GreenMailBean greenMailBean;

    @Autowired
    private Map<String, String> emailTemplates;

    @Autowired
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@localhost";

    @BeforeEach
    void reset() throws FolderException {
        // Clean slate for each test
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
        emailTemplates.clear();
        emailTemplates.put("test-single-html", "classpath:email-templates/test-single-html.eml");
    }

    @Test
    void sendEmailTest() throws Exception {
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        emailService.sendEmail(TEST_EMAIL, "test-single-html", Map.of(
                "subject-var", "This goes in the subject",
                "body", "Pancakes"
        ));
        // Email was sent
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // only to the Email address specified
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(TEST_EMAIL, message.getAllRecipients()[0].toString());
        // Content-type stayed the same
        assertEquals("text/html; charset=UTF-8", message.getContentType());
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        // Variables that were specified were replaced
        assertEquals("Start This goes in the subject End", message.getSubject());
        assertTrue(content.contains("Pancakes"));
        assertFalse(content.contains("{{body}}"));
        assertTrue(content.contains("body"));
        assertTrue(content.contains("{{unspecified-var}}"));
    }

    @Test
    void sanitizeVariablesTest() throws MessagingException, IOException {
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        String testHtml = "<p><script>wow</script><a href='https://example.com'>Sanitized</p>";
        emailService.sendEmail(TEST_EMAIL, "test-single-html", Map.of(
                "subject-var", testHtml,
                "body", testHtml
        ));
        // Email was sent
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        assertEquals("text/html; charset=UTF-8", message.getContentType());
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        // Variables are replaced with sanitized values
        assertEquals("Start Sanitized End", message.getSubject());
        assertTrue(content.contains("Sanitized"));
        assertFalse(content.contains(testHtml));
        assertFalse(content.contains("{{subject-var}}}"));
        assertFalse(content.contains("{{body}}}"));
    }
}
