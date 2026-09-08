package io.github.zacharysabourin.donezo_api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.zacharysabourin.donezo_api.daos.UserDao;
import io.github.zacharysabourin.donezo_api.models.LoginRequest;
import io.github.zacharysabourin.donezo_api.models.SignupRequest;
import io.github.zacharysabourin.donezo_api.models.UserDetailsImpl;
import io.github.zacharysabourin.donezo_api.utils.JwtUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final PasswordEncoder encoder;
	private final UserDao dao;

	AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDao dao,
			PasswordEncoder encoder) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.dao = dao;
		this.encoder = encoder;
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (dao.existsByUsername(signUpRequest.username())) {
			return ResponseEntity.badRequest().body("Error: Username is already taken!");
		}

		dao.createNewUser(signUpRequest.username(), encoder.encode(signUpRequest.password()));

		return ResponseEntity.ok("User registered successfully!");
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body("User signed in successfully!");
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logoutUser() {
		ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body("You've been signed out!");
	}
}