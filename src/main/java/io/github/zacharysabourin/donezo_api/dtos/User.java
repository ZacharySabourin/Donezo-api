package io.github.zacharysabourin.donezo_api.dtos;

import java.sql.Timestamp;
import java.util.UUID;

public record User(UUID id, String username, String password, Timestamp createdAt) {

}
