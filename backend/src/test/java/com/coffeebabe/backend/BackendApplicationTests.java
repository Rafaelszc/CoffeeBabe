package com.coffeebabe.backend;

import com.coffeebabe.backend.infra.security.SecurityConfig;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
		mockMvc.perform(get("/api/auth/me").cookie(adminCookie()))
				.andExpect(jsonPath("$.data.role").value("ADMIN"));
	}

	@Test
	void seedProductsHaveCategoryAndImage() throws Exception {
		var body = mockMvc.perform(get("/api/product").param("q", "cap").param("category", "HOT_COFFEE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items.length()").value(1))
				.andExpect(jsonPath("$.data.items[0].name").value("Cappuccino"))
				.andExpect(jsonPath("$.data.items[0].price").value(4.0))
				.andExpect(jsonPath("$.data.totalPages").value(1))
				.andReturn().getResponse().getContentAsString();
		var id = JsonPath.<String>read(body, "$.data.items[0].id");

		mockMvc.perform(get("/api/product/" + id + "/image"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("image/jpeg"))
				.andExpect(content().bytes(new ClassPathResource("seed/images/product-cappuccino.jpg").getContentAsByteArray()));

		mockMvc.perform(get("/api/product").param("category", "SNACK"))
				.andExpect(jsonPath("$.data.items[0].name").value("Cake"));
		mockMvc.perform(get("/api/product").param("size", "51")).andExpect(status().isBadRequest());
	}

	@Test
	void adminManagesProductAndImage() throws Exception {
		var admin = adminCookie();
		var png = new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 42};

		mockMvc.perform(post("/api/product").cookie(admin).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Iced Latte\",\"description\":\"gelado\",\"price\":6.5,\"category\":\"COLD_COFFEE\"}"))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/product").cookie(admin).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Iced Latte\",\"description\":\"x\",\"price\":1,\"category\":\"COLD_COFFEE\"}"))
				.andExpect(status().isBadRequest());

		var body = mockMvc.perform(get("/api/product").param("q", "iced latte"))
				.andReturn().getResponse().getContentAsString();
		var id = JsonPath.<String>read(body, "$.data.items[0].id");

		mockMvc.perform(multipart("/api/product/" + id + "/image").file(new MockMultipartFile("file", "x.png", "image/png", png)).cookie(admin))
				.andExpect(status().isOk());
		mockMvc.perform(multipart("/api/product/" + id + "/image")
						.file(new MockMultipartFile("file", "fake.png", "image/png", "not a png".getBytes())).cookie(admin))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/product/" + id + "/image"))
				.andExpect(content().bytes(png));

		mockMvc.perform(delete("/api/product/" + id).cookie(admin)).andExpect(status().isOk());
		mockMvc.perform(get("/api/product/" + id + "/image")).andExpect(status().isNotFound());
	}

	private Cookie adminCookie() throws Exception {
		return mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"admin@test.local\",\"password\":\"admin-password\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getCookie(SecurityConfig.TOKEN_COOKIE);
	}
}
