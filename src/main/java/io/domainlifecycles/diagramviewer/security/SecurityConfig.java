package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import io.domainlifecycles.diagramviewer.rest.DomainModelController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationSuccessHandler successHandler;
    private final NavigationAccessChecker navigationAccessChecker;

    public SecurityConfig(@Qualifier("customAuthenticationSuccessHandler") AuthenticationSuccessHandler successHandler,
                          @Qualifier("projectAndDiagramNavigationAccessChecker") NavigationAccessChecker navigationAccessChecker) {
        this.successHandler = successHandler;
        this.navigationAccessChecker = navigationAccessChecker;
    }

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        http
            .securityMatcher(DomainModelController.DOMAIN_MODEL_API_PATH)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/actuator/**").permitAll();
                auth.requestMatchers(DomainModelController.DOMAIN_MODEL_API_PATH + "/**").permitAll();
                auth.anyRequest().authenticated();
            })
            .oauth2Login((login) -> login.successHandler(successHandler));

        return http.build();
    }

    @Bean
    NavigationAccessControlConfigurer navigationAccessControlConfigurerCustomizer() {
        return new NavigationAccessControlConfigurer()
            .withAnnotatedViewAccessChecker()
            .withNavigationAccessChecker(navigationAccessChecker);
    }
}
