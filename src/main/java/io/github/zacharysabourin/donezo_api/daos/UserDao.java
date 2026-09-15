package io.github.zacharysabourin.donezo_api.daos;

import java.util.Optional;

import io.github.zacharysabourin.donezo_api.dtos.User;

/**
 * Data Access Object interface defining persistence operations for {@link User}
 * entities.
 */
public interface UserDao {

    /**
     * Retrieves a user entity by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the found {@link User}, or empty if no
     *         matching user exists
     */
    Optional<User> findByUsername(String username);

    /**
     * Persists a new user record with an encoded password hash.
     *
     * @param username the unique username for the new account
     * @param password the pre-hashed password string to store
     */
    void createNewUser(String username, String password);

    /**
     * Checks whether a user account exists with the given username.
     *
     * @param username the username to check for existence
     * @return {@code true} if a record exists with the specified username;
     *         {@code false} otherwise
     */
    boolean existsByUsername(String username);
}