package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.HandlerHelper.RequestType;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import io.domainlifecycles.diagramviewer.webapp.views.SignInView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationSuccessHandler successHandler;
    private final NavigationAccessChecker navigationAccessChecker;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          ProjectAndDiagramNavigationAccessChecker navigationAccessChecker) {
        this.successHandler = successHandler;
        this.navigationAccessChecker = navigationAccessChecker;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .logout((logout) -> logout.logoutSuccessUrl(SignInView.VIEW_PATH))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(SignInView.VIEW_PATH)).anonymous()
                .requestMatchers(
                    PathPatternRequestMatcher.withDefaults().matcher("/actuator/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/api/domain-model/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/api/resources/view/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/VAADIN/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/favicon.ico"),
                    PathPatternRequestMatcher.withDefaults().matcher("/robots.txt"),
                    PathPatternRequestMatcher.withDefaults().matcher("/manifest.webmanifest"),
                    PathPatternRequestMatcher.withDefaults().matcher("/sw.js"),
                    PathPatternRequestMatcher.withDefaults().matcher("/offline-page.html"),
                    PathPatternRequestMatcher.withDefaults().matcher("/frontend/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/webjars/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/frontend-es5/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/frontend-es6/**"),
                    PathPatternRequestMatcher.withDefaults().matcher("/.well-known/**"),
                    this::isFrameworkInternalRequest)
                .permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login((login) -> login.successHandler(successHandler).loginPage(SignInView.VIEW_PATH))
            .exceptionHandling(
                httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(
                    (request, response, authException) -> response.sendRedirect(SignInView.VIEW_PATH)))
            .addFilterAfter(apiKeyAuthFilter, BasicAuthenticationFilter.class)
            .build();
    }

    private boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
            && Stream.of(RequestType.values())
            .anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }

    @Bean
    NavigationAccessControlConfigurer navigationAccessControlConfigurerCustomizer() {
        return new NavigationAccessControlConfigurer()
            .withAnnotatedViewAccessChecker()
            .withNavigationAccessChecker(navigationAccessChecker);
    }
}
