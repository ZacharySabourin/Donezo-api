package io.github.zacharysabourin.donezo_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import io.github.zacharysabourin.donezo_api.config.EmbeddedPostgresWithFlywayDataSourceConfiguration;
import io.github.zacharysabourin.donezo_api.dtos.BulkTodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.dtos.Todo;
import io.github.zacharysabourin.donezo_api.dtos.TodoRequest;
import io.github.zacharysabourin.donezo_api.dtos.TodoUpdateRequest;
import io.github.zacharysabourin.donezo_api.utils.JwtUtils;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(EmbeddedPostgresWithFlywayDataSourceConfiguration.class)
@Sql(scripts = "/test-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TodoControllerTest {

	private static final String BASE_URL = "/todos/";
	private static final UUID VALID_USER_ID = UUID.fromString("26248245-7afd-42b5-a65b-3e21ea693ce2");
	private static final UUID INVALID_USER_ID = UUID.fromString("c40a7cae-3135-4b6b-bdf3-6391e2b0f0e9");

	private static final String VALID_USERNAME = "testmctest";
	private static final String VALID_USERNAME_NO_TODOS = "john-test";

	@Autowired
	private RestTestClient client;

	@Autowired
	private JwtUtils jwtUtil;

	@Value("${donezo.jwt.cookie}")
	private String jwtCookieName;

	private String validJwtToken;
	private String invalidJwtToken;
	private String csrfTokenValue;

	@BeforeEach
	void setUp() {
		validJwtToken = jwtUtil.generateTokenFromUsername(VALID_USERNAME);
		invalidJwtToken = "invalid.jwt.token.here";
		csrfTokenValue = "test-csrf-token";
	}

	// =========================================================================
	// GET /todos/ - Authentication & CSRF Tests
	// =========================================================================

	@Test
	void getTodos_success() {
		client.get().uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<List<Todo>>() {
				})
				.consumeWith(response -> {
					List<Todo> body = response.getResponseBody();
					assertThat(body).isNotEmpty();
					assertThat(body.get(0)).isInstanceOf(Todo.class);
					assertThat(body).extracting(Todo::userId).contains(VALID_USER_ID);
				});
	}

	@Test
	void getTodos_failure_unauthorized_missingToken() {
		client.get().uri(BASE_URL)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void getTodos_failure_unauthorized_invalidToken() {
		client.get().uri(BASE_URL)
				.cookie(jwtCookieName, invalidJwtToken)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void getTodos_success_emptyList() {
		String noTodosToken = jwtUtil.generateTokenFromUsername(VALID_USERNAME_NO_TODOS);
		client.get().uri(BASE_URL)
				.cookie(jwtCookieName, noTodosToken)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Todo[].class)
				.consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
	}

	// =========================================================================
	// POST /todos/ - Authentication & CSRF Tests
	// =========================================================================

	@Test
	void createTodo_success() {
		TodoRequest clientBody = new TodoRequest("test task", false, 0);

		client.post().uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(clientBody)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Todo.class)
				.consumeWith(response -> {
					Todo created = response.getResponseBody();
					assertNotNull(created);
					assertNotNull(created.id());
					assertNotNull(created.createdAt());
					assertEquals(VALID_USER_ID, created.userId());
					assertEquals(clientBody.text(), created.text());
					assertEquals(clientBody.completed(), created.completed());
					assertEquals(clientBody.position(), created.position());
				});
	}

	@Test
	void createTodo_failure_unauthorized() {
		TodoRequest clientBody = new TodoRequest("test task", false, 0);

		client.post().uri(BASE_URL)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(clientBody)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	// =========================================================================
	// PATCH /todos/{id} - Authentication Tests
	// =========================================================================

	@Test
	void updateTodo_success() {
		Todo[] allTodos = fetchAllTodos();

		TodoUpdateRequest update = new TodoUpdateRequest(
				Optional.of("This is an update"),
				Optional.of(false),
				Optional.of(444));

		client.patch().uri(BASE_URL + allTodos[0].id())
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void updateTodo_failure_unauthorized() {
		TodoUpdateRequest update = new TodoUpdateRequest(
				Optional.of("This is an update"),
				Optional.of(false),
				Optional.of(444));

		client.patch().uri(BASE_URL + UUID.randomUUID())
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void updateTodo_failure_emptyFields() {
		Todo[] allTodos = fetchAllTodos();

		TodoUpdateRequest update = new TodoUpdateRequest(Optional.empty(), Optional.empty(), Optional.empty());
		client.patch().uri(BASE_URL + allTodos[0].id())
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void updateTodo_failure_invalidId() {
		TodoUpdateRequest update = new TodoUpdateRequest(
				Optional.of("This is an update"),
				Optional.of(false),
				Optional.of(444));

		client.patch().uri(BASE_URL + INVALID_USER_ID)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(update)
				.exchange()
				.expectStatus().isNotFound();
	}

	// =========================================================================
	// PATCH /todos/ - Bulk Update Authentication Tests
	// =========================================================================

	@Test
	void updateTodos_success() {
		Todo[] allTodos = fetchAllTodos();

		List<BulkTodoUpdateRequest> updates = Arrays.stream(allTodos)
				.map(todo -> new BulkTodoUpdateRequest(todo.id(), todo.position() + 1))
				.toList();

		client.patch().uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(updates)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void updateTodos_failure_unauthorized() {
		client.patch().uri(BASE_URL)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(new ArrayList<>())
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void updateTodos_failure_InvalidBody() {
		client.patch().uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(new ArrayList<>())
				.exchange()
				.expectStatus().isBadRequest();
	}

	// =========================================================================
	// DELETE /todos/{id} - Authentication Tests
	// =========================================================================

	@Test
	void deleteTodo_success() {
		Todo[] allTodos = fetchAllTodos();

		client.delete().uri(BASE_URL + allTodos[0].id())
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteTodo_failure_unauthorized() {
		client.delete().uri(BASE_URL + UUID.randomUUID())
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void deleteTodo_failure_invalidId() {
		client.delete().uri(BASE_URL + INVALID_USER_ID)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.exchange()
				.expectStatus().isNotFound();
	}

	// =========================================================================
	// DELETE /todos/ - Bulk Delete Authentication Tests
	// =========================================================================

	@Test
	void deleteTodos_success() {
		Todo[] allTodos = fetchAllTodos();
		List<Todo> body = Arrays.stream(allTodos).filter(Todo::completed).toList();

		client.method(HttpMethod.DELETE).uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(body)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Test
	void deleteTodos_failure_unauthorized() {
		client.method(HttpMethod.DELETE).uri(BASE_URL)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.accept(MediaType.APPLICATION_JSON)
				.body(new ArrayList<>())
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void deleteTodos_failure_missingBody() {
		client.delete().uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.cookie("XSRF-TOKEN", csrfTokenValue)
				.header("X-XSRF-TOKEN", csrfTokenValue)
				.exchange()
				.expectStatus().isBadRequest();
	}

	private Todo[] fetchAllTodos() {
		return client.get().uri(BASE_URL)
				.cookie(jwtCookieName, validJwtToken)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(Todo[].class).getResponseBody();
	}
}
