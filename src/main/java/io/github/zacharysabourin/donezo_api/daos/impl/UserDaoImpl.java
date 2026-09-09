package io.github.zacharysabourin.donezo_api.daos.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.zacharysabourin.donezo_api.daos.UserDao;
import io.github.zacharysabourin.donezo_api.dtos.User;

@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "select * from users where username = ?";
        LOGGER.info("Querying DB: '{}' for username: '{}'", sql, username);
        return Optional.of(jdbcTemplate.query(sql, rs -> rs.next() ? getDefaultRSE(rs) : null, username));
    }

    @Override
    public void createNewUser(String username, String password) {
        String sql = "insert into users (username, password_hash) values (?, ?)";
        LOGGER.info("Updating DB: '{}' for userId: '{}'", sql, username);
        jdbcTemplate.update(sql, username, password);
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "select count(*) from users where username = ?";
        LOGGER.info("Querying DB: '{}' for username: '{}'", sql, username);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    private User getDefaultRSE(ResultSet rs) throws SQLException {
        return new User(rs.getObject("id", java.util.UUID.class),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at"));
    }
}
