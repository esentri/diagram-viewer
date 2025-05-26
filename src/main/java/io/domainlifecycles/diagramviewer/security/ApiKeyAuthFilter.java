package io.domainlifecycles.diagramviewer.security;

import io.domainlifecycles.diagramviewer.model.viewer.RegisteredUser;
import io.domainlifecycles.diagramviewer.rest.api.DomainMirrorUploadController;
import io.domainlifecycles.diagramviewer.rest.api.ResourceController;
import io.domainlifecycles.diagramviewer.service.RegisteredUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER_NAME = "X-API-KEY";

    private final RegisteredUserService registeredUserService;

    public ApiKeyAuthFilter(RegisteredUserService registeredUserService) {
        this.registeredUserService = registeredUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith(DomainMirrorUploadController.UPLOAD_DOMAIN_MIRROR_API_PATH) || path.startsWith(
            ResourceController.RESOURCES_API_PATH + ResourceController.VIEW_API_PATH_SUFFIX)) {
            final String apiKey = request.getHeader(API_KEY_HEADER_NAME);
            final Optional<RegisteredUser> foundRegisteredUser = registeredUserService.findByApiKey(apiKey);

            if (foundRegisteredUser.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(foundRegisteredUser.get(), null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
