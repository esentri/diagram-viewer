package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.HandlerHelper.RequestType;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import io.domainlifecycles.diagramviewer.webapp.views.SignInView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/VAADIN/**",
            "/favicon.ico",
            "/robots.txt",
            "/manifest.webmanifest",
            "/sw.js",
            "/offline-page.html",
            "/frontend/**",
            "/webjars/**",
            "/frontend-es5/**",
            "/frontend-es6/**",
            ".well-known/**"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .logout((logout) -> logout.logoutSuccessUrl(SignInView.VIEW_PATH))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SignInView.VIEW_PATH).anonymous()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/domain-model/**").permitAll()
                .requestMatchers("/api/resources/view/**").permitAll()
                .requestMatchers(this::isFrameworkInternalRequest).permitAll()
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
