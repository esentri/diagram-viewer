package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

@Component("customAuthenticationSuccessHandler")
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private final SecurityService securityService;

    public CustomAuthenticationSuccessHandler(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            email = oAuth2User.getAttribute("preferred_username");
        }

        if (email == null) {
            throw DiagramViewerException.fail("Email or username not found in OAuth2 response.");
        }

        final String fullName = oAuth2User.getAttribute("name");

        securityService.acknowledgeUserAuthentication(email, fullName);

        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        response.sendRedirect(savedRequest.getRedirectUrl());
    }
}
