package com.coffeebabe.backend;

import com.coffeebabe.backend.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Needs Docker: real Postgres with all Flyway migrations
@SpringBootTest(properties = {"app.admin.email=admin@test.local", "app.admin.password=admin-password"})
@AutoConfigureMockMvc
class BackendApplicationTests {
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	static {
		postgres.start();
	}

	@Autowired MockMvc mockMvc;

	@Test
	void signUpSignInAndMe() throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"ana\",\"email\":\"Ana@Mail.com\",\"password\":\"senha-forte\"}"))
				.andExpect(status().isCreated());

		// same email in another case is a duplicate
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"ana2\",\"email\":\"ana@mail.com\",\"password\":\"senha-forte\"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ana@mail.com\",\"password\":\"errada-123\"}"))
				.andExpect(status().isUnauthorized());

		var cookie = mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ana@mail.com\",\"password\":\"senha-forte\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getCookie(SecurityConfig.TOKEN_COOKIE);

		mockMvc.perform(get("/api/auth/me").cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value("ana@mail.com"))
				.andExpect(jsonPath("$.data.role").value("USER"));
	}

	@Test
	void adminIsSeeded() throws Exception {
		var cookie = mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"admin@test.local\",\"password\":\"admin-password\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getCookie(SecurityConfig.TOKEN_COOKIE);

		mockMvc.perform(get("/api/auth/me").cookie(cookie))
				.andExpect(jsonPath("$.data.role").value("ADMIN"));
	}
}
