package io.github.zacharysabourin.donezo_api.dtos;

import java.util.Optional;

/**
 * Data Transfer Object (DTO) record representing a partial update (PATCH)
 * request for an existing todo item.
 * <p>
 * Fields are wrapped in {@link Optional} to support selective updates, allowing
 * clients to omit fields that should remain unchanged.
 * </p>
 * 
 * @param text      an {@link Optional} containing the new text content, or
 *                  empty if unchanged
 * @param completed an {@link Optional} containing the new completion status, or
 *                  empty if unchanged
 * @param position  an {@link Optional} containing the new display order
 *                  position, or empty if unchanged
 */
public record TodoUpdateRequest(Optional<String> text, Optional<Boolean> completed, Optional<Integer> position) {

}