package io.github.zacharysabourin.donezo_api.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.models.TodoUpdate;

/**
 * Service layer interface. Used to interact with the DAO layer and handle all
 * subsequent processing of that data.
 */
public interface TodoService {

    /**
     * Retrieves all Todos for a given user id.
     * 
     * @param userId The id of the user
     * @return A List of all user's Todos. May be empty.
     */
    public List<Todo> getAllTodos(UUID userId);

    /**
     * Creates a new Todo given the provided object. Returns an Optional that
     * contains the new Todo entity if successful, and empty if not.
     * 
     * @param newTodo The Todo to persist.
     * @return An {@link Optional} that may or may not contain the newly persisted
     *         entity.
     */
    public Optional<Todo> createNewTodo(Todo newTodo);

    /**
     * Updates the Todo that matches the given id using the provided update entity.
     * Returns a boolean that indicates update success.
     * 
     * @param todoId  The id of the Todo to update.
     * @param updates All column names and values to update.
     * @return True if successful, false if not.
     */
    public boolean updateTodo(UUID todoId, TodoUpdate updates);

    /**
     * Deletes the Todo that matches the given user id and Todo id.
     * 
     * @param userId The user id of the Tod to delete.
     * @param todoId The id of the Todo to delete.
     * @return True if successful, false if not.
     */
    public boolean deleteTodo(UUID userId, UUID todoId);

    public boolean deleteMultipleTodos(List<Todo> deletions);
}
