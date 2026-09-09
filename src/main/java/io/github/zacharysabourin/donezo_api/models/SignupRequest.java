package io.github.zacharysabourin.donezo_api.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 40, message = "Username must be between 3 and 40 characters") String username,
        @NotBlank(message = "Password is required") @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters") String password) {
}