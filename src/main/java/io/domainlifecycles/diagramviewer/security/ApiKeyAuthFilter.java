package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.model.AuthenticatedUser;
import io.domainlifecycles.diagramviewer.rest.DomainModelController;
import io.domainlifecycles.diagramviewer.service.AuthenticatedUserService;
import io.domainlifecycles.diagramviewer.service.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER_NAME = "X-API-KEY";

    private final AuthenticatedUserService authenticatedUserService;

    public ApiKeyAuthFilter(AuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith(DomainModelController.DOMAIN_MODEL_API_PATH)) {
            final String apiKey = request.getHeader(API_KEY_HEADER_NAME);
            final Optional<AuthenticatedUser> foundAuthenticatedUser = authenticatedUserService.findByApiKey(apiKey);

            if (foundAuthenticatedUser.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(foundAuthenticatedUser.get(), null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
