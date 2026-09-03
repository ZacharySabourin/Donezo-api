package io.github.zacharysabourin.donezo_api.daos.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.zacharysabourin.donezo_api.daos.TodoDao;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.models.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdateRequest;

@Repository
public class TodoDaoImpl implements TodoDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public TodoDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Todo> getTodos(UUID userId) {
        String sql = "select * from todos where user_id = ?";
        LOGGER.info("Querying DB: '{}' for userId: '{}'", sql, userId);
        return jdbcTemplate.query(sql, this::getDefaultRowMapper, userId);
    }

    @Override
    public Todo createTodo(TodoRequest request) {
        String sql = "insert into todos (user_id, text, completed, position) values (?, ?, ?, ?) RETURNING id";
        LOGGER.info("Updating DB: '{}' for userId: '{}'", sql, request.userId());

        // Using a RETURNING query mapped to a UUID to allow proper selecting
        UUID key = jdbcTemplate.queryForObject(sql, UUID.class,
                request.userId(),
                request.text(),
                request.completed(),
                request.position());

        // Do a quick query to retrieve the newly persisted Todo
        String selectSql = "select * from todos where id = ?";
        LOGGER.info("Querying DB: '{}' with key: '{}'", selectSql, key);
        return jdbcTemplate.query(selectSql, rs -> rs.next() ? getDefaultRSE(rs) : null, key);
    }

    @Override
    public int updateTodo(UUID todoId, TodoUpdateRequest updates) {
        String sqlFirstHalf = "update todos set ";
        String sqlSecondHalf = " where id = ?";
        List<Object> params = new ArrayList<>();

        // Add each value if present in the object and construct the query
        if (updates.text().isPresent()) {
            sqlFirstHalf = sqlFirstHalf.concat("text = ?, ");
            params.add(updates.text().get());
        }
        if (updates.completed().isPresent()) {
            sqlFirstHalf = sqlFirstHalf.concat("completed = ?, ");
            params.add(updates.completed().get());
        }
        if (updates.position().isPresent()) {
            sqlFirstHalf = sqlFirstHalf.concat("position = ?, ");
            params.add(updates.position().get());
        }

        params.add(todoId);

        // Trim the last 2 bytes of the first half and combine both strings
        String sqlFinal = sqlFirstHalf.substring(0, sqlFirstHalf.length() - 2).concat(sqlSecondHalf);

        LOGGER.info("Querying DB: '{}' with id: '{}' and values: '{}'", sqlFinal, todoId, params);
        int numRowsAffected = jdbcTemplate.update(sqlFinal, params.toArray());
        LOGGER.info("Updated {} rows", numRowsAffected);
        return numRowsAffected;
    }

    @Override
    public int updateTodos(List<BulkTodoUpdateRequest> updates) {
        if (updates.isEmpty()) {
            LOGGER.info("No updates to make. List size 0");
            return 0;
        }
        String sqlFirstHalf = "update todos as t set position = v.new_position from (values ";
        String sqlSecondHalf = ") as v(id, new_position) where t.id = v.id::uuid";

        List<Object> params = new ArrayList<>(updates.size() * 2);

        for (BulkTodoUpdateRequest update : updates) {
            sqlFirstHalf = sqlFirstHalf.concat("(?, ?), ");
            params.add(update.id());
            params.add(update.position());
        }

        String sqlFinal = sqlFirstHalf.substring(0, sqlFirstHalf.length() - 2).concat(sqlSecondHalf);
        LOGGER.info("Querying DB: '{}' with values: '{}'", sqlFinal, params);
        int numRowsAffected = jdbcTemplate.update(sqlFinal, params.toArray());
        LOGGER.info("Updated {} rows", numRowsAffected);
        return numRowsAffected;
    }

    @Override
    public int deleteTodo(UUID userId, UUID todoId) {
        String sql = "delete from todos where user_id = ? and id = ?";
        LOGGER.info("Updating DB: '{}' for userId: '{}'", sql, userId);
        int numRowsAffected = jdbcTemplate.update(sql, userId, todoId);
        LOGGER.info("Deleted {} rows", numRowsAffected);
        return numRowsAffected;
    }

    @Override
    public int deleteMultipleTodos(List<UUID> deletions) {
        if (deletions.isEmpty()) {
            LOGGER.info("No deletions to make. List size 0");
            return 0;
        }
        String sqlFirstHalf = "delete from todos where id in(";

        // Ensure same number of bind parameters as UUIDs
        for (int i = 0; i < deletions.size(); i++) {
            sqlFirstHalf = sqlFirstHalf.concat("?, ");
        }

        // Trim the last 2 bytes of the first half and combine both strings
        String sqlFinal = sqlFirstHalf.substring(0, sqlFirstHalf.length() - 2).concat(")");

        LOGGER.info("Querying DB: '{}' with values: '{}'", sqlFinal, deletions);
        int numRowsAffected = jdbcTemplate.update(sqlFinal, deletions.toArray());
        LOGGER.info("Updated {} rows", numRowsAffected);
        return numRowsAffected;
    }

    /*
     * ResultSetExtractor callback function for mapping column names to a Todo
     * entity.
     */
    private Todo getDefaultRSE(ResultSet resultSet) throws SQLException {
        return new Todo(resultSet.getObject("id", java.util.UUID.class),
                resultSet.getObject("user_id", java.util.UUID.class),
                resultSet.getString("text"),
                resultSet.getBoolean("completed"),
                resultSet.getInt("position"),
                resultSet.getTimestamp("created_at"));
    }

    /*
     * RowMapper callback function for mapping column names to a Todo entity.
     */
    private Todo getDefaultRowMapper(ResultSet resultSet, int rowNum) throws SQLException {
        return getDefaultRSE(resultSet);
    }
}
