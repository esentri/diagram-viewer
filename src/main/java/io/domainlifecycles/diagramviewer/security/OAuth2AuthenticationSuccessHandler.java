package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.exception.DiagramViewerException;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final SecurityService securityService;

    public OAuth2AuthenticationSuccessHandler(SecurityService securityService) {
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

        final String firstName = oAuth2User.getAttribute("given_name");
        final String lastName = oAuth2User.getAttribute("family_name");
        final String sub = oAuth2User.getAttribute("sub");

        securityService.acknowledgeOAuth2UserAuthentication(email, firstName, lastName, sub);

        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);

        String redirectUrl = savedRequest == null ? "/" : savedRequest.getRedirectUrl();
        response.sendRedirect(redirectUrl);
    }
}
