package io.github.zacharysabourin.donezo_api.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) record representing a request payload to create a
 * new todo item.
 *
 * @param text      the title or description content of the todo item
 * @param completed {@code true} if the new item should be created in a
 *                  completed state; {@code false} otherwise
 * @param position  the display order position assigned to the new todo item
 */
public record TodoRequest(@NotBlank(message = "Text cannot be blank") String text, boolean completed, int position) {

}