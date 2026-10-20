package com.coffeebabe.backend;

import com.coffeebabe.backend.core.gateways.CartCrudGateway;
import com.coffeebabe.backend.infra.security.SecurityConfig;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Needs Docker: real Postgres with all Flyway migrations
@SpringBootTest(properties = {"app.admin.email=admin@test.local", "app.admin.password=admin-password"})
@AutoConfigureMockMvc
class BackendApplicationTests {
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	// refresh tokens (ADR-0008)
	static GenericContainer<?> redis = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

	static {
		postgres.start();
		redis.start();
	}

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@Autowired MockMvc mockMvc;
	@MockitoSpyBean CartCrudGateway cartCrudGateway;

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

	@Test
	void cartsAreIsolatedPerUser() throws Exception {
		var alice = signUpAndSignIn("alice");
		var bob = signUpAndSignIn("bob");
		var productId = productIdByName("Mocha");

		for (int i = 0; i < 2; i++) {
			mockMvc.perform(post("/api/cart/add").cookie(alice).contentType(MediaType.APPLICATION_JSON)
							.content("{\"productId\":\"" + productId + "\"}"))
					.andExpect(status().isOk());
		}

		mockMvc.perform(get("/api/cart/get").cookie(alice))
				.andExpect(jsonPath("$.data.products[0].quantity").value(2))
				.andExpect(jsonPath("$.data.subtotal").value(9.0))
				.andExpect(jsonPath("$.data.tax").value(0.27))
				.andExpect(jsonPath("$.data.total").value(9.27));

		// a userId param changes nothing: the user always comes from the token
		var otherUserId = UUID.randomUUID().toString();
		mockMvc.perform(delete("/api/cart/delete").cookie(bob).param("productId", productId).param("userId", otherUserId))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/cart/get").cookie(bob).param("userId", otherUserId))
				.andExpect(jsonPath("$.data.products.length()").value(0));
		mockMvc.perform(get("/api/cart/get").cookie(alice))
				.andExpect(jsonPath("$.data.products[0].quantity").value(2));

		mockMvc.perform(post("/api/cart/add").cookie(alice).contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":\"" + UUID.randomUUID() + "\"}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/cart/get")).andExpect(status().isUnauthorized());
	}

	@Test
	void v8GroupsExistingCartRows() {
		var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		Flyway.configure().dataSource(dataSource).schemas("v8check").target("7").load().migrate();

		var jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("SET search_path TO v8check");
		var user = jdbc.queryForObject("INSERT INTO users (username, email, password, role) VALUES ('u', 'u@x.com', 'h', 'USER') RETURNING id", UUID.class);
		var product = jdbc.queryForObject("SELECT id FROM product WHERE name = 'Cake'", UUID.class);
		for (int i = 0; i < 3; i++) {
			jdbc.update("INSERT INTO cart (user_id, product_id) VALUES (?, ?)", user, product);
		}

		Flyway.configure().dataSource(dataSource).schemas("v8check").load().migrate();

		var rows = jdbc.queryForList("SELECT quantity FROM v8check.cart", Integer.class);
		assertThat(rows).containsExactly(3);
	}

	@Test
	void orderEndToEndWithServerPrices() throws Exception {
		var carol = signUpAndSignIn("carol");
		var dave = signUpAndSignIn("dave");
		var admin = adminCookie();
		addToCart(carol, productIdByName("Cappuccino"), 2);
		addToCart(carol, productIdByName("Mocha"), 1);

		// the client-sent total is ignored; the PRD example totals R$ 32,88
		var body = mockMvc.perform(post("/api/orders").cookie(carol).contentType(MediaType.APPLICATION_JSON)
						.content("{\"fulfillment\":\"DELIVERY\",\"address\":\"Rua das Flores, 10\",\"total\":0.01}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.subtotal").value(12.5))
				.andExpect(jsonPath("$.data.tax").value(0.38))
				.andExpect(jsonPath("$.data.deliveryFee").value(20.0))
				.andExpect(jsonPath("$.data.total").value(32.88))
				.andReturn().getResponse().getContentAsString();
		var orderId = JsonPath.<String>read(body, "$.data.id");

		mockMvc.perform(get("/api/cart/get").cookie(carol)).andExpect(jsonPath("$.data.products.length()").value(0));
		mockMvc.perform(post("/api/orders").cookie(carol).contentType(MediaType.APPLICATION_JSON)
						.content("{\"fulfillment\":\"PICKUP\"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/orders/" + orderId).cookie(dave)).andExpect(status().isNotFound());
		mockMvc.perform(post("/api/orders/" + orderId + "/cancel").cookie(dave)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/orders").cookie(carol)).andExpect(jsonPath("$.data.items[0].id").value(orderId));

		// a catalog change does not alter the past order
		mockMvc.perform(put("/api/product/" + productIdByName("Mocha")).cookie(admin).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Mocha\",\"description\":\"novo\",\"price\":9.99,\"category\":\"HOT_COFFEE\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/orders/" + orderId).cookie(carol))
				.andExpect(jsonPath("$.data.items[?(@.productName == 'Mocha')].unitPrice").value(4.5))
				.andExpect(jsonPath("$.data.total").value(32.88));
		// other tests rely on the seed price
		mockMvc.perform(put("/api/product/" + productIdByName("Mocha")).cookie(admin).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Mocha\",\"description\":\"A rich, chocolate-flavored coffee drink.\",\"price\":4.5,\"category\":\"HOT_COFFEE\"}"))
				.andExpect(status().isOk());

		for (var next : new String[]{"CONFIRMED", "READY"}) {
			setStatus(admin, orderId, next).andExpect(status().isOk());
		}
		mockMvc.perform(post("/api/orders/" + orderId + "/cancel").cookie(carol)).andExpect(status().isConflict());
		setStatus(admin, orderId, "COMPLETED").andExpect(status().isConflict());
		setStatus(admin, orderId, "OUT_FOR_DELIVERY").andExpect(status().isOk());
		setStatus(admin, orderId, "COMPLETED").andExpect(jsonPath("$.data.status").value("COMPLETED"));
	}

	@Test
	void ownerCancelsPendingPickupOrder() throws Exception {
		var erin = signUpAndSignIn("erin");
		addToCart(erin, productIdByName("Cake"), 1);
		var body = mockMvc.perform(post("/api/orders").cookie(erin).contentType(MediaType.APPLICATION_JSON)
						.content("{\"fulfillment\":\"PICKUP\"}"))
				.andExpect(jsonPath("$.data.deliveryFee").value(0.0))
				.andReturn().getResponse().getContentAsString();

		mockMvc.perform(post("/api/orders/" + JsonPath.read(body, "$.data.id") + "/cancel").cookie(erin))
				.andExpect(jsonPath("$.data.status").value("CANCELLED"));
	}

	@Test
	void failingToClearTheCartRollsBackTheOrder() throws Exception {
		var frank = signUpAndSignIn("frank");
		addToCart(frank, productIdByName("Cake"), 1);
		doThrow(new IllegalStateException("boom")).when(cartCrudGateway).clearCart(any());

		assertThatThrownBy(() -> mockMvc.perform(post("/api/orders").cookie(frank).contentType(MediaType.APPLICATION_JSON)
				.content("{\"fulfillment\":\"PICKUP\"}"))).hasRootCauseInstanceOf(IllegalStateException.class);

		mockMvc.perform(get("/api/orders").cookie(frank)).andExpect(jsonPath("$.data.totalItems").value(0));
		mockMvc.perform(get("/api/cart/get").cookie(frank)).andExpect(jsonPath("$.data.products[0].quantity").value(1));
	}

	private void addToCart(Cookie user, String productId, int times) throws Exception {
		for (int i = 0; i < times; i++) {
			mockMvc.perform(post("/api/cart/add").cookie(user).contentType(MediaType.APPLICATION_JSON)
							.content("{\"productId\":\"" + productId + "\"}"))
					.andExpect(status().isOk());
		}
	}

	private ResultActions setStatus(Cookie admin, String orderId, String status) throws Exception {
		return mockMvc.perform(patch("/api/orders/" + orderId + "/status").cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"));
	}

	@Test
	void bestSellersCountOnlyCompletedOrders() throws Exception {
		var admin = adminCookie();
		var gina = signUpAndSignIn("gina");
		var hugo = signUpAndSignIn("hugo");

		// 10 Cakes completed: Cake must lead whatever other tests completed
		addToCart(gina, productIdByName("Cake"), 10);
		var completed = placePickupOrder(gina);
		for (var next : new String[]{"CONFIRMED", "READY", "COMPLETED"}) {
			setStatus(admin, completed, next).andExpect(status().isOk());
		}

		// big pending and cancelled orders do not count
		addToCart(hugo, productIdByName("Mocha"), 30);
		placePickupOrder(hugo);
		addToCart(hugo, productIdByName("Cappuccino"), 40);
		setStatus(admin, placePickupOrder(hugo), "CANCELLED").andExpect(status().isOk());

		mockMvc.perform(get("/api/product/get/best"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].name").value("Cake"))
				.andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.allOf(
						org.hamcrest.Matchers.greaterThanOrEqualTo(3), org.hamcrest.Matchers.lessThanOrEqualTo(6))));
	}

	@Test
	void v10LinksOldOrderItemsAndDropsBestSeller() {
		var dataSource = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
		Flyway.configure().dataSource(dataSource).schemas("v10check").target("9").load().migrate();

		var jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("SET search_path TO v10check");
		var user = jdbc.queryForObject("INSERT INTO users (username, email, password, role) VALUES ('u', 'u@x.com', 'h', 'USER') RETURNING id", UUID.class);
		var order = jdbc.queryForObject("""
				INSERT INTO orders (user_id, fulfillment, status, subtotal, tax, delivery_fee, total)
				VALUES (?, 'PICKUP', 'COMPLETED', 3.75, 0.11, 0, 3.86) RETURNING id""", UUID.class, user);
		jdbc.update("INSERT INTO order_item (order_id, product_name, unit_price, quantity) VALUES (?, 'Cake', 3.75, 1)", order);
		jdbc.update("INSERT INTO order_item (order_id, product_name, unit_price, quantity) VALUES (?, 'Gone', 1, 1)", order);

		Flyway.configure().dataSource(dataSource).schemas("v10check").load().migrate();

		assertThat(jdbc.queryForObject("""
				SELECT COUNT(*) FROM v10check.order_item oi JOIN v10check.product p ON p.id = oi.product_id
				WHERE oi.product_name = 'Cake'""", Integer.class)).isEqualTo(1);
		assertThat(jdbc.queryForObject("SELECT product_id FROM v10check.order_item WHERE product_name = 'Gone'", UUID.class)).isNull();
		assertThat(jdbc.queryForObject("SELECT to_regclass('v10check.best_seller')", String.class)).isNull();
	}

	private String placePickupOrder(Cookie user) throws Exception {
		var body = mockMvc.perform(post("/api/orders").cookie(user).contentType(MediaType.APPLICATION_JSON)
						.content("{\"fulfillment\":\"PICKUP\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.data.id");
	}

	@Test
	void refreshRotatesAndReuseRevokesEverySession() throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"ivan\",\"email\":\"ivan@mail.com\",\"password\":\"senha-forte\"}"))
				.andExpect(status().isCreated());
		var signIn = mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@mail.com\",\"password\":\"senha-forte\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		var firstRefresh = signIn.getCookie(SecurityConfig.REFRESH_COOKIE);
		assertThat(firstRefresh.getPath()).isEqualTo("/api/auth");
		assertThat(firstRefresh.isHttpOnly()).isTrue();

		// rotation: the old token is spent and a working pair comes back
		var rotated = mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh))
				.andExpect(status().isOk())
				.andReturn().getResponse();
		var secondRefresh = rotated.getCookie(SecurityConfig.REFRESH_COOKIE);
		assertThat(secondRefresh.getValue()).isNotEqualTo(firstRefresh.getValue());
		mockMvc.perform(get("/api/auth/me").cookie(rotated.getCookie(SecurityConfig.TOKEN_COOKIE)))
				.andExpect(jsonPath("$.data.email").value("ivan@mail.com"));

		// replaying the spent token (grace is 0 in tests) kills the newer one as well
		mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh)).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/refresh").cookie(secondRefresh)).andExpect(status().isUnauthorized());

		// sign out revokes the token of this browser
		var again = mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"ivan@mail.com\",\"password\":\"senha-forte\"}"))
				.andReturn().getResponse().getCookie(SecurityConfig.REFRESH_COOKIE);
		mockMvc.perform(post("/api/auth/signout").cookie(again)).andExpect(status().isOk());
		mockMvc.perform(post("/api/auth/refresh").cookie(again)).andExpect(status().isUnauthorized());
	}

	private Cookie signUpAndSignIn(String name) throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"" + name + "\",\"email\":\"" + name + "@mail.com\",\"password\":\"senha-forte\"}"))
				.andExpect(status().isCreated());
		return mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + name + "@mail.com\",\"password\":\"senha-forte\"}"))
				.andReturn().getResponse().getCookie(SecurityConfig.TOKEN_COOKIE);
	}

	private String productIdByName(String name) throws Exception {
		var body = mockMvc.perform(get("/api/product").param("q", name)).andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.data.items[0].id");
	}

	private Cookie adminCookie() throws Exception {
		return mockMvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"admin@test.local\",\"password\":\"admin-password\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getCookie(SecurityConfig.TOKEN_COOKIE);
	}
}
