package io.github.zacharysabourin.donezo_api.models;

import java.util.UUID;

/**
 * Entity received by the client. Used to update multiple existing Todo
 * positions.
 * 
 * @param id
 * @param position
 */
public record BulkTodoUpdateRequest(UUID id, int position) {

}
