package io.github.zacharysabourin.donezo_api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.zacharysabourin.donezo_api.daos.UserDao;
import io.github.zacharysabourin.donezo_api.dtos.LoginRequest;
import io.github.zacharysabourin.donezo_api.dtos.SignupRequest;
import io.github.zacharysabourin.donezo_api.exceptions.models.BadRequestException;
import io.github.zacharysabourin.donezo_api.models.UserDetailsImpl;
import io.github.zacharysabourin.donezo_api.models.UserProfile;
import io.github.zacharysabourin.donezo_api.utils.JwtUtils;
import jakarta.validation.Valid;

/**
 * REST controller handling authentication workflows, including user
 * registration,
 * session authentication (login/logout), and retrieving user profile metadata.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final PasswordEncoder encoder;
	private final UserDao dao;

	/**
	 * Constructs a new {@link AuthController} with required authentication and user
	 * persistence dependencies.
	 *
	 * @param authenticationManager the Spring Security authentication manager
	 * @param jwtUtils              utility for generating and clearing JWT security
	 *                              cookies
	 * @param dao                   data access object for user records
	 * @param encoder               password encoder for hashing user credentials
	 */
	AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDao dao,
			PasswordEncoder encoder) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.dao = dao;
		this.encoder = encoder;
	}

	/**
	 * Retrieves the profile metadata for the currently authenticated user.
	 *
	 * @param currentUser the authenticated user principal injected from the
	 *                    security context
	 * @return a {@link ResponseEntity} containing the user's {@link UserProfile}
	 */
	@GetMapping("/profile")
	public ResponseEntity<UserProfile> getUser(@AuthenticationPrincipal UserDetailsImpl currentUser) {
		return ResponseEntity.ok(new UserProfile(currentUser.getId(), currentUser.getUsername()));
	}

	/**
	 * Registers a new user account with an encoded password.
	 *
	 * @param signUpRequest payload containing requested username and plaintext
	 *                      password
	 * @return a {@link ResponseEntity} with status {@code 201 Created} upon
	 *         successful registration
	 * @throws BadRequestException if the requested username is already registered
	 */
	@PostMapping("/signup")
	public ResponseEntity<Void> registerUser(@Valid @RequestBody SignupRequest signUpRequest)
			throws BadRequestException {
		if (dao.existsByUsername(signUpRequest.username())) {
			throw new BadRequestException("Error: Username is already taken!", HttpMethod.POST);
		}

		dao.createNewUser(signUpRequest.username(), encoder.encode(signUpRequest.password()));

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Authenticates user credentials and issues an HTTP-only JWT cookie upon
	 * success.
	 *
	 * @param loginRequest payload containing login credentials (username and
	 *                     password)
	 * @return a {@link ResponseEntity} containing the {@link UserProfile} body and
	 *         a {@code Set-Cookie} header with the JWT
	 */
	@PostMapping("/login")
	public ResponseEntity<UserProfile> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new UserProfile(userDetails.getId(), userDetails.getUsername()));
	}

	/**
	 * Clears the active session by returning a clearing HTTP-only JWT cookie.
	 *
	 * @return a {@link ResponseEntity} with status {@code 200 OK} and a
	 *         {@code Set-Cookie} header invalidating the JWT
	 */
	@PostMapping("/logout")
	public ResponseEntity<Void> logoutUser() {
		ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.build();
	}
}