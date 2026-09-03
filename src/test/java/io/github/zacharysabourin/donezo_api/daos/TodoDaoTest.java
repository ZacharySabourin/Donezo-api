package io.github.zacharysabourin.donezo_api.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import io.github.zacharysabourin.donezo_api.config.EmbeddedPostgresWithFlywayDataSourceConfiguration;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.models.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdateRequest;

@SpringBootTest
@Import(EmbeddedPostgresWithFlywayDataSourceConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoDaoTest {

    private static final UUID VALID_USER_ID = UUID.fromString("26248245-7afd-42b5-a65b-3e21ea693ce2");
    private static final UUID INVALID_USER_ID = UUID.fromString("c40a7cae-3135-4b6b-bdf3-6391e2b0f0e9");
    private static final UUID INVALID_TODO_ID = UUID.fromString("4c2d228c-1523-4ad3-a629-946077923c4b");

    @Autowired
    private TodoDao dao;

    @Test
    @Order(1) // Ensure this runs before any deletion tests
    void getTodos_success() {
        List<Todo> results = dao.getTodos(VALID_USER_ID);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10, results.size());
    }

    @Test
    void getTodos_empty() {
        List<Todo> results = dao.getTodos(INVALID_USER_ID);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        assertEquals(0, results.size());
    }

    @Test
    void createTodo_success() {
        TodoRequest todo = new TodoRequest(VALID_USER_ID, "Testing the todo creation", false, 200);
        Todo result = dao.createTodo(todo);

        assertNotNull(result);
        assertNotNull(result.id());
        assertNotNull(result.createdAt());
        assertEquals(todo.userId(), result.userId());
        assertEquals(todo.text(), result.text());
        assertEquals(todo.completed(), result.completed());
        assertEquals(todo.position(), result.position());
    }

    @Test
    @Order(2) // Ensure this runs before any deletion tests
    void updateTodo_success() {
        // Fetch all todos to allow use of id values
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        UUID validTodoId = allTodos.get(0).id();

        // Test single value updates
        TodoUpdateRequest update = new TodoUpdateRequest(Optional.ofNullable(null), Optional.ofNullable(null),
                Optional.ofNullable(22));
        int numRowsAffected = dao.updateTodo(validTodoId, update);
        assertEquals(1, numRowsAffected);

        // next single value update
        update = new TodoUpdateRequest(Optional.ofNullable("Some text to change"), Optional.ofNullable(null),
                Optional.ofNullable(null));
        validTodoId = allTodos.get(2).id();
        numRowsAffected = dao.updateTodo(validTodoId, update);
        assertEquals(1, numRowsAffected);

        // next single value update
        update = new TodoUpdateRequest(Optional.ofNullable(null), Optional.ofNullable(true),
                Optional.ofNullable(null));
        validTodoId = allTodos.get(4).id();
        numRowsAffected = dao.updateTodo(validTodoId, update);
        assertEquals(1, numRowsAffected);

        // Test all potential changes at once
        update = new TodoUpdateRequest(Optional.ofNullable("Some more text to change"), Optional.ofNullable(true),
                Optional.ofNullable(33));
        validTodoId = allTodos.get(8).id();
        numRowsAffected = dao.updateTodo(validTodoId, update);
        assertEquals(1, numRowsAffected);
    }

    @Test
    void updateTodo_failure_invalidTodoId() {
        TodoUpdateRequest update = new TodoUpdateRequest(Optional.ofNullable("Test"), Optional.ofNullable(true),
                Optional.ofNullable(44));
        int numRowsAffected = dao.updateTodo(INVALID_TODO_ID, update);
        assertEquals(0, numRowsAffected);
    }

    @Test
    @Order(3) // Ensure this runs before any deletion tests
    void updateTodos_success() {
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        List<BulkTodoUpdateRequest> updates = allTodos.stream().map(todo -> {
            return new BulkTodoUpdateRequest(todo.id(), todo.position() + 1);
        }).toList();

        assertEquals(allTodos.size(), dao.updateTodos(updates));
    }

    @Test
    void updateTodos_failure() {
        int numRowsAffected = dao.updateTodos(new ArrayList<>());
        assertEquals(0, numRowsAffected);

        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        List<BulkTodoUpdateRequest> updates = allTodos.stream().map(todo -> {
            // Use invalid id for each update
            return new BulkTodoUpdateRequest(INVALID_TODO_ID, todo.position() + 1);
        }).toList();

        assertEquals(0, dao.updateTodos(updates));
    }

    @Test
    void deleteTodo_success() {
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        UUID validTodoId = allTodos.get(0).id();
        assertEquals(1, dao.deleteTodo(VALID_USER_ID, validTodoId));
    }

    @Test
    void deleteTodo_failure() {
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        UUID validTodoId = allTodos.get(8).id();
        assertEquals(0, dao.deleteTodo(VALID_USER_ID, INVALID_TODO_ID));
        assertEquals(0, dao.deleteTodo(INVALID_USER_ID, validTodoId));
        assertEquals(0, dao.deleteTodo(INVALID_USER_ID, INVALID_TODO_ID));
    }

    @Test
    void deleteTodos_success() {
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        List<UUID> deletions = allTodos.stream().filter(Todo::completed).map(Todo::id).toList();
        int deleteCount = dao.deleteMultipleTodos(deletions);
        assertEquals(deletions.size(), deleteCount);

        // Fetch again to ensure they're missing
        int originalSize = allTodos.size();
        allTodos = dao.getTodos(VALID_USER_ID);
        assertEquals(allTodos.size(), originalSize - deleteCount);
    }

    @Test
    void deleteTodos_failure() {
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);

        // Change the id of each to ensure they won't exist
        String update = "aaaa";
        List<UUID> deletions = allTodos.stream().filter(todo -> todo.position() < 5).map(todo -> {
            String current = todo.id().toString();
            return UUID
                    .fromString(current.replace(current.subSequence(current.length() - 4, current.length()), update));
        }).toList();

        assertEquals(0, dao.deleteMultipleTodos(deletions));
    }
}
