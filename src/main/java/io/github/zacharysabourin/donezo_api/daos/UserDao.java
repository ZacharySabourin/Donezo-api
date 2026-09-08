package io.github.zacharysabourin.donezo_api.daos;

import java.util.Optional;

import io.github.zacharysabourin.donezo_api.dtos.User;

public interface UserDao {

    public Optional<User> findByUsername(String username);

    public void createNewUser(String username, String password);

    public boolean existsByUsername(String username);
}
