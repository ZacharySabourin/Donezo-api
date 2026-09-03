package io.github.zacharysabourin.donezo_api.models;

import java.util.Optional;

/**
 * Entity received by the client. Used to update an existing Todo.
 * Fields are Optional to allow for the client to send any number of field
 * updates.
 * 
 * @param text
 * @param completed
 * @param position
 */
public record TodoUpdateRequest(Optional<String> text, Optional<Boolean> completed, Optional<Integer> position) {

}
