package io.github.zacharysabourin.donezo_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.github.zacharysabourin.donezo_api.request.AuthTokenFilter;
import io.github.zacharysabourin.donezo_api.request.CsrfCookieFilter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Main Spring Security configuration class.
 * <p>
 * Configures HTTP security rules, CORS policies, CSRF protection, stateless
 * session management, custom JWT authentication filters, and core security
 * beans.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AccessControlConfiguration config;

    /**
     * Constructs a new {@link SecurityConfig} instance with access control
     * configurations.
     *
     * @param config the configuration properties for CORS and security rules
     */
    public SecurityConfig(AccessControlConfiguration config) {
        this.config = config;
    }

    /**
     * Configures the primary {@link SecurityFilterChain} for the application.
     *
     * @param http            the {@link HttpSecurity} object to build
     *                        configurations upon
     * @param authTokenFilter the custom JWT authentication filter
     * @return the fully configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while configuring HTTP security
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthTokenFilter authTokenFilter) {

        // Handle unauthenticated requests with a 401 JSON response instead of
        // redirection
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \""
                            + authException.getMessage() + "\"}");
                }));

        // Apply global CORS settings defined in corsConfigurationSource()
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Defer token generation/resolution until requested
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Setting to null ensures the token is loaded directly from the request
        requestHandler.setCsrfRequestAttributeName(null);

        // Store CSRF tokens in a XSRF-TOKEN cookie, http-only set to false
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");

        // Configure CSRF protection using cookie-based token repository
        http.csrf(csrf -> csrf.csrfTokenRepository(tokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/auth/**"));

        // No HTTP Session creation, authentication is derived per-request via JWT
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Set path-based access control rules
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**").permitAll().anyRequest().authenticated());

        // Ensure the XSRF-TOKEN cookie is populated on response
        http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        // Validate JWT tokens before Spring's default username/password filter executes
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the global Cross-Origin Resource Sharing (CORS) rules.
     *
     * @return a configured {@link CorsConfigurationSource} applied to all endpoints
     *         ({@code /**})
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Populate CORS options from injected AccessControlConfiguration properties
        configuration.setAllowedOrigins(config.origin());
        configuration.setAllowedMethods(config.methods());
        configuration.setAllowedHeaders(config.headers());
        configuration.setAllowCredentials(config.credentials());
        configuration.setMaxAge(config.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Exposes the {@link AuthenticationManager} bean from Spring Security's
     * configuration.
     *
     * @param authConfig the authentication configuration delegate
     * @return the active {@link AuthenticationManager}
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Provides the password encoder bean used for hashing and verifying passwords.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}