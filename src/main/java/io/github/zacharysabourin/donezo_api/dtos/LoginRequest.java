package io.github.zacharysabourin.donezo_api.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) record representing a user authentication request.
 *
 * @param username the user's login identifier; must not be blank
 * @param password the user's plaintext password; must not be blank
 */
public record LoginRequest(
                @NotBlank(message = "Username cannot be blank") String username,
                @NotBlank(message = "Password cannot be blank") String password) {
}