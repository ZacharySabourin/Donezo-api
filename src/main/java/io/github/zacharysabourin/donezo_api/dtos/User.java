package io.github.zacharysabourin.donezo_api.dtos;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Domain model record representing a User entity.
 *
 * @param id        the unique identifier of the user
 * @param username  the unique account name of the user
 * @param password  the encoded password hash
 * @param createdAt the timestamp when the user account was created in the
 *                  database
 */
public record User(UUID id, String username, String password, Timestamp createdAt) {

}