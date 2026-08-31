package io.github.zacharysabourin.donezo_api.daos;

import java.util.List;
import java.util.UUID;

import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdate;

/**
 * DAO that allows creating, reading, updating, and deleting Todo entities.
 */
public interface TodoDao {

    /**
     * Gets all todos given a specific user id.
     * 
     * @param userId The given user id.
     * @return A List containing all Todos for a given user. May be empty.
     */
    public List<Todo> getTodos(UUID userId);

    /**
     * Persists a new Todo entity given the TodoRequest object.
     * 
     * @param request The incoming TodoRequest.
     * @return Returns the newly persisted Todo once saved.
     */
    public Todo createTodo(TodoRequest request);

    /**
     * Updates a specific Todo entity using the provided update values.
     * 
     * @param todoId  The id of the Todo to update.
     * @param updates The column names and new values to persist in the DB.
     * @return The number of rows affected by the update. Should be 1.
     */
    public int updateTodo(UUID todoId, TodoUpdate updates);

    /**
     * Deletes a specific Todo given the user and Todo ids.
     * 
     * @param userId The user id of the Todo to delete.
     * @param todoId The id of the Todo to delete.
     * @return The number of rows affected by the update. Should be 1.
     */
    public int deleteTodo(UUID userId, UUID todoId);

    /**
     * Deletes all Todos given the ids.
     * 
     * @param deletions The list of ids of Todo to delete.
     * @return The number of rows affected by the update. Should be the same length
     *         as the incoming list.
     */
    public int deleteMultipleTodos(List<UUID> deletions);
}
