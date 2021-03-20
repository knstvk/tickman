package com.haulmont.tickman.screen.login;

import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.TextField;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.annotation.Nullable;
import java.util.Locale;

@UiController("tickman_LoginScreen")
@UiDescriptor("tickman-login-screen.xml")
@Route(path = "login", root = true)
public class TickmanLoginScreen extends Screen {

    @Autowired
    private TextField<String> usernameField;

    @Autowired
    private PasswordField passwordField;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Messages messages;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Subscribe
    private void onInit(InitEvent event) {
        usernameField.focus();
        initDefaultCredentials();
    }

    private void initDefaultCredentials() {
        usernameField.setValue("admin");
        passwordField.setValue("admin");
    }

    @Subscribe("submit")
    private void onSubmitActionPerformed(Action.ActionPerformedEvent event) {
        login();
    }

    private void login() {
        String username = usernameField.getValue();
        String password = passwordField.getValue() != null ? passwordField.getValue() : "";

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "emptyUsernameOrPassword"))
                    .show();
            return;
        }

        try {
            Authentication authenticationToken = createAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHelper.setAuthentication(authentication);
            showMainScreen();
        } catch (BadCredentialsException | DisabledException e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(getClass(), "loginFailed"))
                    .withDescription(e.getMessage())
                    .show();
        }
    }

    protected Authentication createAuthenticationToken(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        ClientDetails clientDetails = ClientDetails.builder()
                .locale(Locale.ENGLISH)
                .build();

        authenticationToken.setDetails(clientDetails);

        return authenticationToken;
    }

    protected void showMainScreen() {
        screenBuilders.screen(this)
                .withScreenId("tickman_MainScreen")
                .withOpenMode(OpenMode.ROOT)
                .build()
                .show();
    }
}
