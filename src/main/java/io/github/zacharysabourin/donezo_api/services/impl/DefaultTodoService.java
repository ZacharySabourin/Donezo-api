package io.github.zacharysabourin.donezo_api.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.zacharysabourin.donezo_api.daos.TodoDao;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.services.TodoService;

@Service
public class DefaultTodoService implements TodoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTodoService.class);
    private final TodoDao dao;

    public DefaultTodoService(TodoDao dao) {
        this.dao = dao;
    }

    @Override
    public List<Todo> getAllTodos(UUID userId) {
        List<Todo> results = dao.getTodos(userId);
        if (results == null || results.isEmpty()) {
            LOGGER.info("No Todos for user {}", userId);
            return Collections.emptyList();
        }

        return results;
    }

    @Override
    public Optional<Todo> createNewTodo(Todo newTodo) {
        Optional<Todo> createdTodo = Optional.ofNullable(dao.createTodo(newTodo));
        if (createdTodo.isEmpty()) {
            LOGGER.info("Failed to create new Todo for user: '{}'", newTodo.userId());
        }
        return createdTodo;
    }

    @Override
    public boolean updateTodo(UUID todoId, Map<String, Object> updates) {
        int numRowsAffected = dao.updateTodo(todoId, updates);
        if (numRowsAffected == 0) {
            LOGGER.info("Failed to Update any data using id: '{}' and values: '{}'", todoId, updates);
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteTodo(UUID userId, UUID todoId) {
        int numRowsDeleted = dao.deleteTodo(userId, todoId);
        if (numRowsDeleted == 0) {
            LOGGER.info("Failed to delete any data using id: '{}'", todoId);
            return false;
        }

        return true;
    }
}
