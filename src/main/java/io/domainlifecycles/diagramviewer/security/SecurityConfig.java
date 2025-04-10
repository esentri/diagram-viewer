package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final ProjectAndDiagramNavigationAccessChecker navigationAccessChecker;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler, ProjectAndDiagramNavigationAccessChecker navigationAccessChecker) {
        this.successHandler = successHandler;
        this.navigationAccessChecker = navigationAccessChecker;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(request -> {
                request.requestMatchers(mvc.servletPath("/").pattern("/actuator/**")).permitAll();
                request.anyRequest().authenticated();
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
