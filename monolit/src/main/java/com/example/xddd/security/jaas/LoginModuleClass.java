package com.example.xddd.security.jaas;

import com.sun.security.auth.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

public class LoginModuleClass implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    private String username;
    private boolean loginSucceeded;

    @Override
    public void initialize(
            Subject subject,
            CallbackHandler callbackHandler,
            Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.userDetailsService = (UserDetailsService) options.get("userDetailsService");
        this.passwordEncoder = (PasswordEncoder) options.get("passwordEncoder");
    }

    @Override
    public boolean login() {
        final var nameCallback = new NameCallback("login");
        final var passwordCallback = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
            username = nameCallback.getName();
            final var password = String.valueOf(passwordCallback.getPassword());
            final var user = userDetailsService.loadUserByUsername(username);
            loginSucceeded = passwordEncoder.matches(password, user.getPassword());
        } catch (UsernameNotFoundException e) {
//            log.warn("User with name = {} was not found during authentication", nameCallback.getName());
            loginSucceeded = false;
        } catch (IOException | UnsupportedCallbackException e) {
//            log.error("Error occured during invocation of callback handler = {}", e.getMessage());
            loginSucceeded = false;
        }
        return loginSucceeded;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }
        if (username == null) {
            throw new LoginException("Username is null during the commit");
        }
        final var principal = new UserPrincipal(username);
        subject.getPrincipals().add(principal);
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return false;
    }
}