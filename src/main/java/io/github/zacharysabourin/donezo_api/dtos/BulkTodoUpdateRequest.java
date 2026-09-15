package io.github.zacharysabourin.donezo_api.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) record representing a positional update for an
 * individual todo item during bulk reordering operations.
 *
 * @param id       the unique identifier of the todo item to update
 * @param position the target display position for the item
 */
public record BulkTodoUpdateRequest(@NotNull(message = "ID cannot be null") UUID id, int position) {

}