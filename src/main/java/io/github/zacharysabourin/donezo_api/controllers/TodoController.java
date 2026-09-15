package io.github.zacharysabourin.donezo_api.controllers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.zacharysabourin.donezo_api.daos.TodoDao;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.exceptions.models.BadRequestException;
import io.github.zacharysabourin.donezo_api.exceptions.models.InternalServerErrorException;
import io.github.zacharysabourin.donezo_api.exceptions.models.NotFoundException;
import io.github.zacharysabourin.donezo_api.models.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.UserDetailsImpl;

/**
 * REST controller for managing {@link Todo} entity read, write, update, and
 * delete requests.
 */
@RestController
@RequestMapping("/todos")
public class TodoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoController.class);

    private final TodoDao dao;

    public TodoController(TodoDao dao) {
        this.dao = dao;
    }

    /**
     * Retrieves all todo items belonging to the currently authenticated user.
     *
     * @param currentUser the authenticated user principal
     * @return a list of todos owned by the user, or an empty list if none exist
     */
    @GetMapping({ "/", "" })
    public List<Todo> getTodos(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        LOGGER.info("Fetching all Todos for user: '{}'", currentUser.getUsername());

        List<Todo> results = dao.getTodosByUserId(currentUser.getId());
        if (results == null || results.isEmpty()) {
            LOGGER.warn("No Todos for user {}", currentUser.getUsername());
            return Collections.emptyList();
        }

        return results;

    }

    /**
     * Creates a new todo item for the currently authenticated user.
     *
     * @param currentUser the authenticated user principal
     * @param request     the request body containing details for the new todo
     * @return the newly created {@link Todo} entity
     * @throws InternalServerErrorException if the todo cannot be persisted
     */
    @PostMapping({ "/", "" })
    public Todo createTodo(@AuthenticationPrincipal UserDetailsImpl currentUser, @RequestBody TodoRequest request)
            throws InternalServerErrorException {
        LOGGER.info("Creating new Todo for user: {}, with values: '{}'", currentUser.getUsername(), request);
        return dao.createTodo(currentUser.getId(), request)
                .orElseThrow(() -> new InternalServerErrorException("Error creating new Todo", HttpMethod.POST));
    }

    /**
     * Updates specific fields of an existing todo item.
     *
     * @param todoId      the unique identifier of the todo to update
     * @param updates     the fields to update
     * @param currentUser the authenticated user principal
     * @return {@link ResponseEntity} with status {@code 204 No Content} on success
     * @throws BadRequestException if no valid fields are provided for update
     * @throws NotFoundException   if no todo matching the given ID exists for the
     *                             user
     */
    @PatchMapping({ "/{todoId}", "/{todoId}/" })
    public ResponseEntity<Void> updateTodo(@PathVariable UUID todoId, @RequestBody TodoUpdateRequest updates,
            @AuthenticationPrincipal UserDetailsImpl currentUser) throws NotFoundException, BadRequestException {

        if (updates.completed().isEmpty() && updates.text().isEmpty() && updates.position().isEmpty()) {
            throw new BadRequestException("No valid updates provided", HttpMethod.PATCH);
        }
        LOGGER.info("Updating Todo: '{}' with values: '{}'", todoId, updates);
        int numRowsAffected = dao.updateTodo(currentUser.getId(), todoId, updates);
        if (numRowsAffected == 0) {
            throw new NotFoundException("No Todo with id: '" + todoId + "'", HttpMethod.PATCH);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Updates multiple todo items in a single request.
     *
     * @param updates     the list of todo updates to apply
     * @param currentUser the authenticated user principal
     * @return {@link ResponseEntity} with status {@code 204 No Content} on success
     * @throws BadRequestException if the updates list is null or empty
     */
    @PatchMapping({ "/", "" })
    public ResponseEntity<Void> updateTodos(@RequestBody List<BulkTodoUpdateRequest> updates,
            @AuthenticationPrincipal UserDetailsImpl currentUser) throws BadRequestException {

        if (updates == null || updates.isEmpty()) {
            throw new BadRequestException("No updates provided", HttpMethod.PATCH);
        }

        LOGGER.info("Updating Todos for user: {}, with updates: '{}'", currentUser.getUsername(), updates);

        int numRowsAffected = dao.updateTodos(currentUser.getId(), updates);
        if (numRowsAffected < updates.size()) {
            LOGGER.warn("Bulk delete mismatch. Expected: {}, Actual: {}", updates.size(), numRowsAffected);

        }
        return ResponseEntity.noContent().build();

    }

    /**
     * Deletes a specific todo item.
     *
     * @param todoId      the unique identifier of the todo to delete
     * @param currentUser the authenticated user principal
     * @return {@link ResponseEntity} with status {@code 204 No Content} on success
     * @throws NotFoundException if no todo matching the given ID exists for the
     *                           user
     */
    @DeleteMapping({ "/{todoId}", "/{todoId}/" })
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID todoId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) throws NotFoundException {
        LOGGER.info("Deleting Todo '{}'", todoId);
        int numRowsDeleted = dao.deleteTodo(currentUser.getId(), todoId);
        if (numRowsDeleted == 0) {
            LOGGER.error("Failed to delete any data using id: '{}'", todoId);
            throw new NotFoundException("No Todo with id: '" + todoId + "'", HttpMethod.DELETE);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes multiple todo items provided in the request body.
     *
     * @param deletions   the list of todos to delete
     * @param currentUser the authenticated user principal
     * @return {@link ResponseEntity} with status {@code 204 No Content} on success
     * @throws BadRequestException if the deletions list is null or empty
     */
    @DeleteMapping({ "/", "" })
    public ResponseEntity<Void> deleteMultipleTodos(@RequestBody List<Todo> deletions,
            @AuthenticationPrincipal UserDetailsImpl currentUser) throws BadRequestException {

        if (deletions == null || deletions.isEmpty()) {
            throw new BadRequestException("No updates provided", HttpMethod.DELETE);
        }

        LOGGER.info("Deleting multiple Todos: '{}'", deletions);

        // Extract all ids into a list for deletion
        List<UUID> uuids = deletions.stream().map(Todo::id).toList();

        int numRowsDeleted = dao.deleteMultipleTodos(currentUser.getId(), uuids);
        int difference = uuids.size() - numRowsDeleted;
        if (difference != 0) {
            LOGGER.warn("Bulk delete mismatch. Expected: {}, Actual: {}", uuids.size(), numRowsDeleted);
        }
        return ResponseEntity.noContent().build();
    }
}
