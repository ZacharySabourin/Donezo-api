package io.github.zacharysabourin.donezo_api.models;

import java.util.UUID;

/**
 * Outgoing response record representing public profile metadata for an
 * authenticated user.
 *
 * @param id       the unique identifier of the user
 * @param username the user's registered account name
 */
public record UserProfile(UUID id, String username) {

}