package io.github.zacharysabourin.donezo_api.models;

import java.util.UUID;

/**
 * Entity received by the client. Used to create a new Todo.
 * 
 * @param userId
 * @param text
 * @param completed
 * @param position
 */
public record TodoRequest(UUID userId, String text, boolean completed, int position) {

}
