package io.github.zacharysabourin.donezo_api.models;

/**
 * Entity received by the client. Used to create a new Todo.
 * 
 * @param text
 * @param completed
 * @param position
 */
public record TodoRequest(String text, boolean completed, int position) {

}
