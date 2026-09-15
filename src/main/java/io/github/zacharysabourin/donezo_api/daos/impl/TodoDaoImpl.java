package io.github.zacharysabourin.donezo_api.daos.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import io.github.zacharysabourin.donezo_api.daos.TodoDao;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.models.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdateRequest;

@Repository
public class TodoDaoImpl implements TodoDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoDaoImpl.class);

    private static final String COMPLETED = "completed";
    private static final String POSITION = "position";
    private static final String USER_ID = "user_id";
    private static final String TEXT = "text";
    private static final String ID = "id";


    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TodoDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<Todo> getTodosByUserId(UUID userId) {
        String sql = "select * from todos where user_id = :user_id";
        LOGGER.info("Querying DB: '{}' for userId: '{}'", sql, userId);
        MapSqlParameterSource params = new MapSqlParameterSource(USER_ID, userId);
        return jdbcTemplate.query(sql, params, this::getDefaultRowMapper);
    }

    @Override
    public Optional<Todo> createTodo(UUID userId, TodoRequest request) {
        String sql = """
                INSERT INTO todos (user_id, text, completed, position)
                VALUES (:user_id, :text, :completed, :position)
                RETURNING *
                """;
        LOGGER.info("Creating todo for userId: '{}'", userId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(USER_ID, userId)
                .addValue(TEXT, request.text())
                .addValue(COMPLETED, request.completed())
                .addValue(POSITION, request.position());

        // Single round-trip using RETURNING *
        Todo createdTodo = jdbcTemplate.queryForObject(sql, params, this::getDefaultRowMapper);
        return Optional.ofNullable(createdTodo);
    }

    @Override
    public int updateTodo(UUID userId, UUID todoId, TodoUpdateRequest updates) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(ID, todoId)
                .addValue(USER_ID, userId);

        List<String> setClauses = new ArrayList<>();

        updates.text().ifPresent(text -> {
            setClauses.add("text = :text");
            params.addValue(TEXT, text);
        });
        updates.completed().ifPresent(completed -> {
            setClauses.add("completed = :completed");
            params.addValue(COMPLETED, completed);
        });
        updates.position().ifPresent(position -> {
            setClauses.add("position = :position");
            params.addValue(POSITION, position);
        });

        if (setClauses.isEmpty()) {
            return 0;
        }

        String sql = "UPDATE todos SET " + String.join(", ", setClauses) + " WHERE id = :id AND user_id = :user_id";

        LOGGER.info("Updating todo '{}' for userId: '{}'", todoId, userId);
        int numRowsAffected = jdbcTemplate.update(sql, params);
        LOGGER.info("Updated {} rows", numRowsAffected);
        return numRowsAffected;
    }

    @Override
    public int updateTodos(UUID userId, List<BulkTodoUpdateRequest> updates) {
        if (updates == null || updates.isEmpty()) {
            LOGGER.info("No updates provided. Skipping batch update.");
            return 0;
        }

        String sql = "UPDATE todos SET position = :position WHERE id = :id AND user_id = :user_id";
        LOGGER.info("Executing batch update for {} items for userId: '{}'", updates.size(), userId);

        SqlParameterSource[] batchParams = updates.stream()
                .map(update -> new MapSqlParameterSource()
                        .addValue(ID, update.id())
                        .addValue(POSITION, update.position())
                        .addValue(USER_ID, userId))
                .toArray(SqlParameterSource[]::new);

        int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchParams);
        return Arrays.stream(updateCounts).sum();
    }

    @Override
    public int deleteTodo(UUID userId, UUID todoId) {
        String sql = "DELETE FROM todos WHERE user_id = :user_id AND id = :id";
        LOGGER.info("Deleting todo '{}' for userId: '{}'", todoId, userId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(USER_ID, userId)
                .addValue(ID, todoId);

        int numRowsAffected = jdbcTemplate.update(sql, params);
        LOGGER.info("Deleted {} rows", numRowsAffected);
        return numRowsAffected;
    }

    @Override
    public int deleteMultipleTodos(UUID userId, List<UUID> deletions) {
        if (deletions == null || deletions.isEmpty()) {
            LOGGER.info("No deletions provided. Skipping batch delete.");
            return 0;
        }

        String sql = "DELETE FROM todos WHERE id IN (:deletions) AND user_id = :user_id";
        LOGGER.info("Deleting {} todos for userId: '{}'", deletions.size(), userId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("deletions", deletions)
                .addValue(USER_ID, userId);

        int numRowsAffected = jdbcTemplate.update(sql, params);
        LOGGER.info("Deleted {} rows", numRowsAffected);
        return numRowsAffected;
    }

    /*
     * RowMapper callback function for mapping column names to a Todo entity.
     */
    private Todo getDefaultRowMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return new Todo(
                resultSet.getObject(ID, UUID.class),
                resultSet.getObject(USER_ID, UUID.class),
                resultSet.getString(TEXT),
                resultSet.getBoolean(COMPLETED),
                resultSet.getInt(POSITION),
                resultSet.getTimestamp("created_at"));
    }
}
