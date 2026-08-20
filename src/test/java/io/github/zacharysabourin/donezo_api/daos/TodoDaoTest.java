package io.github.zacharysabourin.donezo_api.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;

import io.github.zacharysabourin.donezo_api.config.EmbeddedPostgresWithFlywayDataSourceConfiguration;
import io.github.zacharysabourin.donezo_api.dtos.Todo;

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
        Todo todo = new Todo(null, VALID_USER_ID, "Testing the todo creation", false, 200, null);
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
    void updateTodo_success() {
        // Fetch all todos to allow use of id values
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        UUID validTodoId = allTodos.get(0).id();

        // Test single value updates
        Map<String, Object> updates = new HashMap<>(3);
        updates.put("position", 22);
        int numRowsAffected = dao.updateTodo(validTodoId, updates);
        assertEquals(1, numRowsAffected);

        // next single value update
        updates.clear();
        updates.put("text", "Some text to change");
        validTodoId = allTodos.get(2).id();
        numRowsAffected = dao.updateTodo(validTodoId, updates);
        assertEquals(1, numRowsAffected);

        // next single value update
        updates.clear();
        updates.put("completed", true);
        validTodoId = allTodos.get(4).id();
        numRowsAffected = dao.updateTodo(validTodoId, updates);
        assertEquals(1, numRowsAffected);

        // Test all potential changes at once
        updates.clear();
        updates.put("position", 33);
        updates.put("text", "Some more text to change");
        updates.put("completed", true);
        validTodoId = allTodos.get(8).id();
        numRowsAffected = dao.updateTodo(validTodoId, updates);
        assertEquals(1, numRowsAffected);
    }

    @Test
    void updateTodo_failure_malformedSQL() {
        // Fetch all todos to allow use of id values
        List<Todo> allTodos = dao.getTodos(VALID_USER_ID);
        UUID validTodoId = allTodos.get(0).id();

        // This column name is invalid
        Map<String, Object> updates = new HashMap<>(1);
        updates.put("invalid_column", 22);
        assertThrows(DataAccessException.class, () -> dao.updateTodo(validTodoId, updates));

        // The column value is not the right type
        updates.clear();
        updates.put("position", "fail");
        assertThrows(DataAccessException.class, () -> dao.updateTodo(validTodoId, updates));
    }

    @Test
    void updateTodo_failure_invalidTodoId() {
        Map<String, Object> updates = new HashMap<>(1);
        updates.put("position", 22);
        int numRowsAffected = dao.updateTodo(INVALID_TODO_ID, updates);
        assertEquals(0, numRowsAffected);
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
}
