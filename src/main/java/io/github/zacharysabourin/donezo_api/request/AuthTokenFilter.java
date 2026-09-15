package io.github.zacharysabourin.donezo_api.request;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.zacharysabourin.donezo_api.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom security filter that executes once per HTTP request to validate
 * incoming JWT cookies.
 * <p>
 * Extracts the JWT from request cookies, validates the token signature and
 * expiration, loads the corresponding {@link UserDetails}, and sets the
 * authentication context in Spring Security's {@link SecurityContextHolder}.
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * Constructs a new {@link AuthTokenFilter} with required security dependencies.
     *
     * @param jwtUtils           utility bean for parsing and validating JWT tokens
     * @param userDetailsService service bean for loading user account details
     */
    AuthTokenFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepts incoming HTTP requests to extract and validate the JWT
     * authentication cookie.
     * <p>
     * If a valid JWT is present, populates the {@link SecurityContextHolder} with
     * an authenticated {@link UsernamePasswordAuthenticationToken}. Always
     * delegates execution down the filter chain regardless of authentication
     * success or failure.
     * </p>
     * 
     * @param request     the incoming HTTP servlet request
     * @param response    the HTTP servlet response
     * @param filterChain the Spring Security filter chain
     * @throws ServletException if a servlet processing error occurs
     * @throws IOException      if an I/O error occurs during request processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = jwtUtils.getJwtFromCookies(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }
}