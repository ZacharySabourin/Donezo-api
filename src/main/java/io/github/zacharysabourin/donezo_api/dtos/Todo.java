package io.github.zacharysabourin.donezo_api.dtos;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * The basic Todo DTO.
 * 
 * @param id
 * @param userId
 * @param text
 * @param completed
 * @param position
 * @param createdAt
 */
public record Todo(UUID id, UUID userId, String text, boolean completed, int position, Timestamp createdAt) {

}
