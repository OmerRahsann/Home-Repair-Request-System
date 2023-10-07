package homerep.springy.config;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.TestSocketUtils;

import java.util.List;

@TestConfiguration
public class TestMailConfig {
    @Bean
    public GreenMailBean greenMailBean() {
        GreenMailBean mailBean = new GreenMailBean();
        // Avoid conflicts between tests
        mailBean.setPortOffset(TestSocketUtils.findAvailableTcpPort() - ServerSetup.PORT_SMTP);
        mailBean.setPop3Protocol(false); // No need to retrieve mail
        mailBean.setUsers(List.of("noreply:abc123@localhost", "test:abc123@localhost", "test2:abc123@localhost"));
        return mailBean;
    }

    @Bean
    public JavaMailSender greenMailSender(GreenMailBean greenMailBean) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(greenMailBean.getGreenMail().getSmtp().getPort());
        mailSender.setUsername("noreply");
        mailSender.setPassword("abc123");
        return mailSender;
    }
}
