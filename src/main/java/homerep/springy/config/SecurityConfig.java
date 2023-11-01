package homerep.springy.config;

import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Objects;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${server.allowed-origin-patterns:#{null}}")
    private String allowedOriginPatterns;

    @Bean
    public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc, AuthenticationManager authenticationManager) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable); // TODO figure out CSRF
        http.authenticationManager(authenticationManager);
        http.authorizeHttpRequests(
                authorize -> authorize
                        .requestMatchers(mvc.pattern("/api/register")).permitAll()
                        .requestMatchers(mvc.pattern("/api/verify")).permitAll()
                        .requestMatchers(mvc.pattern("/api/login")).permitAll()
                        .requestMatchers(mvc.pattern("/api/logout")).permitAll()
                        .requestMatchers(mvc.pattern("/api/customer/**")).access(
                                AuthorizationManagers.allOf(
                                        AuthorityAuthorizationManager.hasAuthority(AccountType.CUSTOMER.getAuthority()),
                                        AuthorityAuthorizationManager.hasAuthority(Verified.INSTANCE.getAuthority())
                                )        
                        ) // Argh, this is not nice
                        .requestMatchers(mvc.pattern("/api/provider/**")).access(
                                AuthorizationManagers.allOf(
                                        AuthorityAuthorizationManager.hasAuthority(AccountType.SERVICE_PROVIDER.getAuthority()),
                                        AuthorityAuthorizationManager.hasAuthority(Verified.INSTANCE.getAuthority())
                                )
                        )
                        .anyRequest().authenticated()
        );
        http.logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
        http.securityContext(securityContext -> securityContext
                .requireExplicitSave(true)
                .securityContextRepository(securityContextRepository()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOriginPattern(Objects.requireNonNullElse(allowedOriginPatterns, "http://localhost:[*]"));
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.setAllowCredentials(true);
        // Allow all headers and default max age
        corsConfiguration.applyPermitDefaultValues();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder, UserDetailsService service) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider(encoder);
        daoProvider.setUserDetailsService(service);
        builder.authenticationProvider(daoProvider);

        return builder.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}
