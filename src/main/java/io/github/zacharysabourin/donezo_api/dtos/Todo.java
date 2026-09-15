package io.github.zacharysabourin.donezo_api.dtos;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Domain model representing a Todo item entity.
 *
 * @param id        the unique identifier of the todo item
 * @param userId    the unique identifier of the owning user
 * @param text      the title or description content of the todo item
 * @param completed {@code true} if the item is marked as finished;
 *                  {@code false} otherwise
 * @param position  the display order position of the todo item
 * @param createdAt the timestamp when the todo item was created in the database
 */
public record Todo(UUID id, UUID userId, String text, boolean completed, int position, Timestamp createdAt) {

}