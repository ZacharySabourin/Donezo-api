package io.github.zacharysabourin.donezo_api.daos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.zacharysabourin.donezo_api.dtos.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.dtos.TodoRequest;
import io.github.zacharysabourin.donezo_api.dtos.TodoUpdateRequest;

/**
 * Data Access Object interface for managing {@link Todo} persistence
 * operations.
 */
public interface TodoDao {

    /**
     * Retrieves all todos associated with a specific user.
     * 
     * @param userId the unique identifier of the user
     * @return a list of todos owned by the user, or an empty list if none exist
     */
    List<Todo> getTodosByUserId(UUID userId);

    /**
     * Persists a new todo entity for a user.
     * 
     * @param userId  the unique identifier of the user creating the todo
     * @param request the request object containing details for the new todo
     * @return an {@link Optional} containing the persisted todo, or empty if
     *         creation failed
     */
    Optional<Todo> createTodo(UUID userId, TodoRequest request);

    /**
     * Updates fields on an existing todo entity.
     * 
     * @param userId  the unique identifier of the owning user
     * @param todoId  the unique identifier of the todo to update
     * @param updates the request object containing fields to update
     * @return the number of rows affected (typically {@code 1} on success,
     *         {@code 0} if not found)
     */
    int updateTodo(UUID userId, UUID todoId, TodoUpdateRequest updates);

    /**
     * Performs a batch update on multiple todo entities for a user.
     * 
     * @param userId  the unique identifier of the owning user
     * @param updates a list of batch update requests containing todo IDs and new
     *                field values
     * @return the total number of rows affected across all updates
     */
    int updateTodos(UUID userId, List<BulkTodoUpdateRequest> updates);

    /**
     * Deletes a specific todo entity belonging to a user.
     * 
     * @param userId the unique identifier of the owning user
     * @param todoId the unique identifier of the todo to delete
     * @return the number of rows deleted (typically {@code 1} on success, {@code 0}
     *         if not found)
     */
    int deleteTodo(UUID userId, UUID todoId);

    /**
     * Deletes multiple todo entities belonging to a user.
     * 
     * @param userId    the unique identifier of the owning user
     * @param deletions a list of todo IDs to delete
     * @return the total number of rows deleted
     */
    int deleteMultipleTodos(UUID userId, List<UUID> deletions);
}