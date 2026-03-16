package com.yrris.coddy.config;

import com.yrris.coddy.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Value("${app.auth.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            response.sendRedirect(buildRedirectUrl("error", "Unsupported OAuth principal"));
            return;
        }

        String email = getAttribute(oauth2User, "email");
        String displayName = getAttribute(oauth2User, "name");
        String avatarUrl = getAttribute(oauth2User, "picture");
        String providerUserId = getAttribute(oauth2User, "sub");

        if (!StringUtils.hasText(email)) {
            response.sendRedirect(buildRedirectUrl("error", "Google account email is required"));
            return;
        }

        userService.loginWithGoogle(email, displayName, avatarUrl, providerUserId, request);
        response.sendRedirect(buildRedirectUrl("success", null));
    }

    private String getAttribute(OAuth2User oauth2User, String key) {
        Object value = oauth2User.getAttributes().get(key);
        return value == null ? null : value.toString();
    }

    private String buildRedirectUrl(String status, String message) {
        StringBuilder builder = new StringBuilder(frontendUrl).append("/login?oauth=").append(status);
        if (StringUtils.hasText(message)) {
            builder.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }
}
