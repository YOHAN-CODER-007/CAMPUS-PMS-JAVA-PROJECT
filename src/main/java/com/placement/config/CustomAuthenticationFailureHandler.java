package com.placement.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles login failures and redirects to /login with specific error codes.
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorParam = "invalid";

        Throwable cause = (exception.getCause() != null) ? exception.getCause() : exception;

        if (cause instanceof DisabledException || exception instanceof DisabledException) {
            String message = cause.getMessage();
            if ("COMPANY_PENDING".equals(message)) {
                errorParam = "pending";
            } else if ("COMPANY_REJECTED".equals(message)) {
                errorParam = "rejected";
            } else if ("ACCOUNT_INACTIVE".equals(message)) {
                errorParam = "disabled";
            }
        }

        setDefaultFailureUrl("/login?error=" + errorParam);
        super.onAuthenticationFailure(request, response, exception);
    }
}
