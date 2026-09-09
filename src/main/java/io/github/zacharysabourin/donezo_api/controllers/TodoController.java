package io.github.zacharysabourin.donezo_api.controllers;

import java.util.List;
import java.util.Optional;
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

import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.exceptions.models.BadRequestException;
import io.github.zacharysabourin.donezo_api.exceptions.models.InternalServerErrorException;
import io.github.zacharysabourin.donezo_api.exceptions.models.NotFoundException;
import io.github.zacharysabourin.donezo_api.models.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.UserDetailsImpl;
import io.github.zacharysabourin.donezo_api.services.TodoService;

/**
 * Main entry point for this application. Handles all Todo entity read, write,
 * update and delete requests.
 */
@RestController
@RequestMapping("/todos")
public class TodoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Returns all Todo entities given the user id. <code>GET</code> request.
     * 
     * @param userId The given user id.
     * @return A List of all todos bound to the given user.
     */
    @GetMapping({ "/", "" })
    public List<Todo> getTodos(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        LOGGER.info("Fetching all Todos for user: '{}'", currentUser.getUsername());
        return todoService.getAllTodos(currentUser.getId());
    }

    /**
     * Given the user id, will create a Todo using the values in the body of the
     * request. Will throw an <code>InternalServerErrorException</code> if there is
     * a failure to do so. <code>POST</code> request.
     * 
     * @param todo The given Todo to create.
     * @return The Todo entity that was persisted in the data layer.
     * @throws InternalServerErrorException Exception thrown if the new todo is not
     *                                      successfully created.
     */
    @PostMapping({ "/", "" })
    public Todo createTodo(@AuthenticationPrincipal UserDetailsImpl currentUser, @RequestBody TodoRequest request)
            throws InternalServerErrorException {
        LOGGER.info("Creating new Todo for user: {}, with values: '{}'", currentUser.getUsername(), request);
        Optional<Todo> createdTodo = todoService.createNewTodo(currentUser.getId(), request);
        if (createdTodo.isEmpty()) {
            throw new InternalServerErrorException("Error creating new Todo", HttpMethod.POST);
        }

        return createdTodo.get();
    }

    /**
     * Updates a specific Todo given the incoming TodoUpdateRequest request body.
     * <code>PATCH</code> request.
     * 
     * @param id      The specific Todo to update.
     * @param updates The entity used to update an existing Todo.
     * @return A <code>204 No Content</code> if the update was successful. A
     *         <code>404 Not Found</code> if the id was not a valid Todo. A
     *         <code>400 Bad Request</code> if no valid values were provided
     * @throws NotFoundException   Exception thrown if the todo could not be found.
     * @throws BadRequestException Exception thrown if not valid updates were
     *                             provided.
     */
    @PatchMapping({ "/{todoId}", "/{todoId}/" })
    public ResponseEntity<Void> updateTodo(@PathVariable UUID todoId, @RequestBody TodoUpdateRequest updates,
            @AuthenticationPrincipal UserDetailsImpl currentUser)
            throws NotFoundException, BadRequestException {

        if (updates.completed().isEmpty() && updates.text().isEmpty() && updates.position().isEmpty()) {
            throw new BadRequestException("No valid updates provided", HttpMethod.PATCH);
        }

        LOGGER.info("Updating Todo: '{}' with values: '{}'", todoId, updates);
        if (!todoService.updateTodo(currentUser.getId(), todoId, updates)) {
            throw new NotFoundException("No Todo with id: '" + todoId + "'", HttpMethod.PATCH);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates any number of Todos given the incoming List of BulkTodoUpdateRequest
     * in the request body. <code>PATCH</code> request.
     * 
     * @param updates The objects used to update existing Todos.
     * @return A <code>204 No Content</code> if the update was successful. A
     *         <code>500 Internal Server Error</code> if the updates were not
     *         successful. A <code>400 Bad Request</code> if no valid values were
     *         provided
     * @throws BadRequestException          Exception thrown no valid updates
     *                                      provided.
     * @throws InternalServerErrorException Exception thrown if the updates weren't
     *                                      successful.
     */
    @PatchMapping({ "/", "" })
    public ResponseEntity<Void> updateTodos(@RequestBody List<BulkTodoUpdateRequest> updates,
            @AuthenticationPrincipal UserDetailsImpl currentUser)
            throws InternalServerErrorException, BadRequestException {

        if (updates.isEmpty()) {
            throw new BadRequestException("No updates provided", HttpMethod.PATCH);
        }

        LOGGER.info("Updating Todos for user: {}, with updates: '{}'", currentUser.getUsername(), updates);
        if (!todoService.updateTodos(currentUser.getId(), updates)) {
            throw new InternalServerErrorException("Failed to update all Todos", HttpMethod.PATCH);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a specific Todo given the user and Todo id. <code>DELETE</code>
     * request.
     * 
     * @param todoId The given Todo id to delete.
     * @return A <code>204 No Content</code> if the deletion was successful. A
     *         <code>404 Not Found</code> if the id was not a valid Todo. A
     *         <code>400 Bad Request</code> if no todId is provided.
     * @throws NotFoundException Exception thrown if the todo could not be found.
     */
    @DeleteMapping({ "/{todoId}", "/{todoId}/" })
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID todoId,
            @AuthenticationPrincipal UserDetailsImpl currentUser)
            throws NotFoundException {

        LOGGER.info("Deleting Todo '{}'", todoId);
        if (!todoService.deleteTodo(currentUser.getId(), todoId)) {
            throw new NotFoundException("No Todo with id: '" + todoId + "'", HttpMethod.DELETE);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a list Todo entities given the body of the request.
     * <code>DELETE</code> request.
     * 
     * @param deletions The list of Todos to delete
     * @return A <code>204 No Content</code> if the deletion was successful. A
     *         <code>404 Not Found</code> if no Todos were deleted. A
     *         <code>400 Bad Request</code> if no body is provided.
     * @throws InternalServerErrorException Exception thrown if the todos could not
     *                                      be deleted.
     */
    @DeleteMapping({ "/", "" })
    public ResponseEntity<Void> deleteMultipleTodos(@RequestBody List<Todo> deletions,
            @AuthenticationPrincipal UserDetailsImpl currentUser)
            throws InternalServerErrorException {

        LOGGER.info("Deleting multiple Todos: '{}'", deletions);
        if (!todoService.deleteMultipleTodos(currentUser.getId(), deletions)) {
            throw new InternalServerErrorException("Failed to delete all Todos", HttpMethod.DELETE);
        }
        return ResponseEntity.noContent().build();
    }
}
