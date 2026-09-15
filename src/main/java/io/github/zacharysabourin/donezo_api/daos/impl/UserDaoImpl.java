package io.github.zacharysabourin.donezo_api.daos.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.zacharysabourin.donezo_api.daos.UserDao;
import io.github.zacharysabourin.donezo_api.dtos.User;

/**
 * JDBC-based implementation of the {@link UserDao} interface using
 * {@link NamedParameterJdbcTemplate} for user persistence operations.
 */
@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password_hash";
    private static final String ID = "id";
    private static final String CREATED_AT = "created_at";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> findByUsername(String username) {
        LOGGER.info("Querying for user: '{}'", username);
        String sql = "SELECT * FROM users WHERE username = :username";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(USERNAME, username);
        return Optional.ofNullable(jdbcTemplate.query(sql, params, rs -> rs.next() ? getDefaultRSE(rs) : null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewUser(String username, String password) {
        LOGGER.info("Creating new user: '{}'", username);
        String sql = "INSERT INTO users (username, password_hash) VALUES (:username, :password_hash)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(USERNAME, username)
                .addValue(PASSWORD, password);
        jdbcTemplate.update(sql, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByUsername(String username) {
        LOGGER.info("Finding count of users with username: '{}'", username);
        String sql = "SELECT count(*) FROM users WHERE username = :username";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(USERNAME, username);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link User} domain object.
     *
     * @param rs the SQL result set positioned at a valid row
     * @return the mapped {@link User} instance
     * @throws SQLException if a database access error occurs or column names are
     *                      missing
     */
    private User getDefaultRSE(ResultSet rs) throws SQLException {
        return new User(rs.getObject(ID, UUID.class),
                rs.getString(USERNAME),
                rs.getString(PASSWORD),
                rs.getTimestamp(CREATED_AT));
    }
}