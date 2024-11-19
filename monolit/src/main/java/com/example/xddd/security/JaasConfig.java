package com.example.xddd.security;

import com.example.xddd.repositories.UserRepository;
import com.example.xddd.security.jaas.LoginModuleClass;
import com.example.xddd.security.jaas.UserRepositoryAuthorityGranter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.memory.InMemoryConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AppConfigurationEntry;

import java.util.Map;

import static javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;

@Configuration
public class JaasConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public javax.security.auth.login.Configuration configuration(
            final UserDetailsService userDetailsService, final PasswordEncoder passwordEncoder) {
        final var configurationEntries =
                new AppConfigurationEntry[] {
                        new AppConfigurationEntry(
                                LoginModuleClass.class.getCanonicalName(),
                                REQUIRED,
                                Map.of("userDetailsService", userDetailsService, "passwordEncoder", passwordEncoder))
                };
        return new InMemoryConfiguration(Map.of("SPRINGSECURITY", configurationEntries));
    }

    @Bean
    public AbstractJaasAuthenticationProvider jaasAuthenticationProvider(
            final javax.security.auth.login.Configuration configuration,
            final UserRepository userRepository) {
        final var defaultJaasAuthenticationProvider = new DefaultJaasAuthenticationProvider();
        defaultJaasAuthenticationProvider.setConfiguration(configuration);
        defaultJaasAuthenticationProvider.setAuthorityGranters(
                new AuthorityGranter[] {new UserRepositoryAuthorityGranter(userRepository)});
        return defaultJaasAuthenticationProvider;
    }
}
