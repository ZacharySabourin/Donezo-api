package io.github.zacharysabourin.donezo_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient.RequestBodySpec;

import io.github.zacharysabourin.donezo_api.config.EmbeddedPostgresWithFlywayDataSourceConfiguration;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.models.TodoRequest;
import io.github.zacharysabourin.donezo_api.models.TodoUpdateRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(EmbeddedPostgresWithFlywayDataSourceConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoControllerTest {

	private static final String BASE_URL = "/todos/";
	private static final UUID VALID_USER_ID = UUID.fromString("26248245-7afd-42b5-a65b-3e21ea693ce2");
	private static final UUID INVALID_USER_ID = UUID.fromString("c40a7cae-3135-4b6b-bdf3-6391e2b0f0e9");

	@Autowired
	private RestTestClient client;

	@Test
	@Order(1) // Ensure this runs before any deletion tests
	void getTodos_success() {
		client.get().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(new ParameterizedTypeReference<List<Todo>>() {
				})
				.consumeWith(response -> {
					List<Todo> body = response.getResponseBody();
					assertThat(body).isNotEmpty();
					assertThat(body.get(0)).isInstanceOf(Todo.class);
					assertThat(body).extracting(todo -> todo.userId()).contains(VALID_USER_ID);
				});
	}

	@Test
	void getTodos_success_emptyList() {
		client.get().uri(BASE_URL + INVALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(Todo[].class)
				.consumeWith(response -> {
					assertThat(response.getResponseBody()).isEmpty();
				});
	}

	@Test
	void getTodos_failure_userIdNotUUID() {
		client.get().uri(BASE_URL + "junkValue")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void createTodo_success() {
		TodoRequest clientBody = new TodoRequest(VALID_USER_ID, "test task", false, 0);

		client.post().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.body(clientBody)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(Todo.class)
				.consumeWith(response -> {
					Todo created = response.getResponseBody();
					assertNotNull(created);
					assertNotNull(created.id());
					assertNotNull(created.createdAt());
					assertEquals(clientBody.userId(), created.userId());
					assertEquals(clientBody.text(), created.text());
					assertEquals(clientBody.completed(), created.completed());
					assertEquals(clientBody.position(), created.position());
				});
	}

	@Test
	@Order(2) // Ensure this runs before any deletion tests
	void updateTodo_success() {
		Todo[] allTodos = client.get().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Todo[].class).getResponseBody();

		TodoUpdateRequest update = new TodoUpdateRequest(Optional.ofNullable("This is an update"), Optional.ofNullable(false),
				Optional.ofNullable(444));
		client.patch().uri(BASE_URL + allTodos[0].id().toString())
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNoContent();

		// Single value updates
		update = new TodoUpdateRequest(Optional.ofNullable("Another update"), Optional.ofNullable(null),
				Optional.ofNullable(null));
		client.patch().uri(BASE_URL + allTodos[4].id().toString())
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNoContent();

		update = new TodoUpdateRequest(Optional.ofNullable(null), Optional.ofNullable(true), Optional.ofNullable(null));
		client.patch().uri(BASE_URL + allTodos[2].id().toString())
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNoContent();

		update = new TodoUpdateRequest(Optional.ofNullable(null), Optional.ofNullable(null), Optional.ofNullable(1234));
		client.patch().uri(BASE_URL + allTodos[8].id().toString())
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	@Order(3) // Ensure this runs before any deletion tests
	void updateTodo_failure_emptyFields() {
		Todo[] allTodos = client.get().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Todo[].class).getResponseBody();

		TodoUpdateRequest update = new TodoUpdateRequest(Optional.ofNullable(null), Optional.ofNullable(null),
				Optional.ofNullable(null));
		client.patch().uri(BASE_URL + allTodos[0].id().toString())
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void updateTodo_failure_invalidId() {

		TodoUpdateRequest update = new TodoUpdateRequest(Optional.ofNullable("This is an update"), Optional.ofNullable(false),
				Optional.ofNullable(444));
		client.patch().uri(BASE_URL + INVALID_USER_ID)
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void deleteTodo_success() {
		Todo[] allTodos = client.get().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Todo[].class).getResponseBody();

		client.delete().uri(BASE_URL + VALID_USER_ID.toString() + "?todoId=" + allTodos[0].id().toString())
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteTodo_failure_invalidId() {
		client.delete().uri(BASE_URL + INVALID_USER_ID + "?todoId=" + INVALID_USER_ID)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void deleteTodo_failure_missingTodoId() {
		client.delete().uri(BASE_URL + INVALID_USER_ID)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void deleteTodos_success() {
		Todo[] allTodos = client.get().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Todo[].class).getResponseBody();
		List<Todo> body = Arrays.stream(allTodos).filter(Todo::completed).toList();
		((RequestBodySpec) client.delete().uri(BASE_URL)
				.accept(MediaType.APPLICATION_JSON))
				.body(body)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteTodos_failure_invalidTodoIds() {
		Todo[] allTodos = client.get().uri(BASE_URL + VALID_USER_ID.toString())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Todo[].class).getResponseBody();

		// Change the id of each to ensure they won't exist
		String update = "aaaa";
		List<Todo> body = Arrays.stream(allTodos).filter(todo -> todo.position() < 5).map(todo -> {
			String current = todo.id().toString();
			UUID newUUid = UUID
					.fromString(current.replace(current.subSequence(current.length() - 4, current.length()), update));
			return new Todo(newUUid, todo.userId(), todo.text(), todo.completed(), todo.position(), todo.createdAt());
		}).toList();

		((RequestBodySpec) client.delete().uri(BASE_URL)
				.accept(MediaType.APPLICATION_JSON))
				.body(body)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void deleteTodos_failure_missingBody() {
		client.delete().uri(BASE_URL)
				.exchange()
				.expectStatus().isBadRequest();
	}
}
