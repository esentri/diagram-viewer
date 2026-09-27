/*
 *     ________  .__
 *     \______ \ |__|____     ________________    _____
 *      |    |  \|  \__  \   / ___\_  __ \__  \  /     \
 *      |    |   \  |/ __ \_/ /_/  >  | \// __ \|  Y Y  \
 *     /_______  /__(____  /\___  /|__|  (____  /__|_|  /
 *             \/        \//_____/            \/      \/
 *     ____   ____.__
 *     \   \ /   /|__| ______  _  __ ___________
 *      \   Y   / |  |/ __ \ \/ \/ // __ \_  __ \
 *       \     /  |  \  ___/\     /\  ___/|  | \/
 *        \___/   |__|\___  >\/\_/  \___  >__|
 *                        \/            \/
 *
 *  Copyright 2025-2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.domainlifecycles.diagramviewer.security;

import com.vaadin.flow.server.HandlerHelper.RequestType;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.spring.security.NavigationAccessControlConfigurer;
import io.domainlifecycles.diagramviewer.webapp.views.SignInView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.features.okta-login.enabled:false}")
    private boolean oktaEnabled;

    private final AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final AuthenticationSuccessHandler selfServiceSuccessHandler;
    private final NavigationAccessChecker navigationAccessChecker;

    public SecurityConfig(OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler,
                          SelfServiceAuthenticationSuccessHandler selfServiceSuccessHandler,
                          ProjectAndDiagramNavigationAccessChecker navigationAccessChecker) {
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.selfServiceSuccessHandler = selfServiceSuccessHandler;
        this.navigationAccessChecker = navigationAccessChecker;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        if (oktaEnabled) {
            http.oauth2Login(login -> login
                    .loginPage(SignInView.VIEW_PATH)
                    .successHandler(oAuth2SuccessHandler)
                    .permitAll()
            );
        } else {
            http.oauth2Login(AbstractHttpConfigurer::disable);
        }

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .logout((logout) -> logout.logoutSuccessUrl(SignInView.VIEW_PATH))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher(SignInView.VIEW_PATH)).anonymous()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/register")).permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/login")).permitAll()
                        .requestMatchers(
                                // only the health check is public (e.g. for container health checks); any other
                                // actuator endpoint, if exposed at all, requires a signed in user
                                PathPatternRequestMatcher.withDefaults().matcher("/actuator/health"),
                                PathPatternRequestMatcher.withDefaults().matcher("/actuator/health/**"),
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
                .formLogin(form -> form
                        .loginPage(SignInView.VIEW_PATH)
                        .loginProcessingUrl("/login")
                        .successHandler(selfServiceSuccessHandler)
                        .failureUrl(SignInView.VIEW_PATH + "?error")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) -> response.sendRedirect(SignInView.VIEW_PATH))
                )
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
