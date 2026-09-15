package io.github.zacharysabourin.donezo_api.services.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.zacharysabourin.donezo_api.daos.UserDao;
import io.github.zacharysabourin.donezo_api.dtos.User;
import io.github.zacharysabourin.donezo_api.models.UserDetailsImpl;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * Responsible for loading user authentication data from the database via
 * {@link UserDao} and converting domain entities into {@link UserDetailsImpl}
 * principals for the security context.
 */
@Service
public class DefaultUserDetailsService implements UserDetailsService {

    private final UserDao dao;

    /**
     * Constructs a new {@link DefaultUserDetailsService} with the required data
     * access object.
     *
     * @param dao the {@link UserDao} used for database queries
     */
    public DefaultUserDetailsService(UserDao dao) {
        this.dao = dao;
    }

    /**
     * Locates the user based on the provided username.
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated {@link UserDetails} security principal
     * @throws UsernameNotFoundException if the user cannot be found in the database
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = dao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return UserDetailsImpl.build(user);
    }
}