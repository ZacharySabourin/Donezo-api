package io.github.zacharysabourin.donezo_api.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.exceptions.models.InternalServerErrorException;
import io.github.zacharysabourin.donezo_api.exceptions.models.NotFoundException;
import io.github.zacharysabourin.donezo_api.exceptions.models.BadRequestException;
import io.github.zacharysabourin.donezo_api.models.TodoUpdate;
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
    @GetMapping("/{userId}")
    public List<Todo> getTodos(@PathVariable UUID userId) {
        LOGGER.info("Fetching all Todos for user: '{}'", userId);
        return todoService.getAllTodos(userId);
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
    @PostMapping("/{userId}")
    public Todo createTodo(@RequestBody Todo todo) throws InternalServerErrorException {
        LOGGER.info("Creating new Todo with values: '{}'", todo);
        Optional<Todo> createdTodo = todoService.createNewTodo(todo);
        if (createdTodo.isEmpty()) {
            throw new InternalServerErrorException("Error creating new Todo", HttpMethod.POST);
        }

        return createdTodo.get();
    }

    /**
     * Updates a specific Todo given the incoming TodoUpdate request body.
     * <code>PATCH</code> request.
     * 
     * @param id      The specific Todo to update.
     * @param updates The entity used to update an existing Todo.
     * @return A <code>204 No Content</code> if the update was successful. A
     *         <code>404 Not Found</code> if the id was not a valid Todo. A
     *         <code>400 Bad Request</code> if no valid values were provided
     * @throws NotFoundException Exception thrown if the todo could not be found.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<HttpStatusCode> updateTodo(@PathVariable UUID id, @RequestBody TodoUpdate updates)
            throws NotFoundException, BadRequestException {

        if (updates.completed().isEmpty() && updates.text().isEmpty() && updates.position().isEmpty()) {
            throw new BadRequestException("No valid updates provided", HttpMethod.PATCH);
        }

        LOGGER.info("Updating Todo: '{}' with values: '{}'", id, updates);
        if (!todoService.updateTodo(id, updates)) {
            throw new NotFoundException("No Todo with id: '" + id + "'", HttpMethod.PATCH);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Deletes a specific Todo given the user and Todo id. <code>DELETE</code>
     * request.
     * 
     * @param userId The given user id.
     * @param todoId The given Todo id to delete.
     * @return A <code>204 No Content</code> if the deletion was successful. A
     *         <code>404 Not Found</code> if the id was not a valid Todo. A
     *         <code>400 Bad Request</code> if no todId is provided.
     * @throws NotFoundException Exception thrown if the todo could not be found.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<HttpStatusCode> deleteTodo(@PathVariable UUID userId, @RequestParam UUID todoId)
            throws NotFoundException{
    
        LOGGER.info("Deleting Todo '{}'' for user: '{}'", todoId, userId);
        if (!todoService.deleteTodo(userId, todoId)) {
            throw new NotFoundException("No Todo with id: '" + todoId + "'", HttpMethod.DELETE);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
