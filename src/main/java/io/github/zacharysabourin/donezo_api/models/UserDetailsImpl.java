package io.github.zacharysabourin.donezo_api.models;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.zacharysabourin.donezo_api.dtos.User;

/**
 * Custom implementation of Spring Security's {@link UserDetails} interface.
 * Serves as the principal object representing an authenticated user within the
 * security context.
 */
public class UserDetailsImpl implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String username;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a new {@link UserDetailsImpl} instance.
     *
     * @param id          the unique identifier of the user
     * @param username    the username of the account
     * @param password    the encoded password hash
     * @param authorities the granted security authorities or roles assigned to the
     *                    user
     */
    public UserDetailsImpl(UUID id, String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory method to adapt a domain {@link User} object into a Spring Security
     * {@link UserDetailsImpl}. Assigns the default {@code ROLE_USER} authority.
     *
     * @param user the domain user model
     * @return a fully populated {@link UserDetailsImpl} principal
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserDetailsImpl(
                user.id(),
                user.username(),
                user.password(),
                authorities);
    }

    /**
     * Gets the unique database identifier of the user.
     *
     * @return the user {@link UUID}
     */
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always; account expiration is not enforced
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always; account locking is not enforced
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always; credentials expiration is not enforced
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always; account disabling is not enforced
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Compares two {@link UserDetailsImpl} instances for equality based strictly on
     * their unique {@link #id}.
     *
     * @param o the object to compare
     * @return {@code true} if both instances represent the same user ID;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
