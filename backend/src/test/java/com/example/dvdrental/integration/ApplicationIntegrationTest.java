package com.example.dvdrental.integration;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.*;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.profiles.active=dev", "DEV_ADMIN_EMAIL=admin@example.test", "DEV_ADMIN_PASSWORD=Admin-password-123",
    "server.servlet.session.cookie.secure=false"
})
@Import(ApplicationIntegrationTest.TestTime.class)
class ApplicationIntegrationTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    @DynamicPropertySource static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", MYSQL::getJdbcUrl);
        properties.add("spring.datasource.username", MYSQL::getUsername);
        properties.add("spring.datasource.password", MYSQL::getPassword);
    }
    @TestConfiguration static class TestTime {
        @Bean @Primary Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneId.of("Europe/London"));
        }
    }
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    private final JsonMapper json = JsonMapper.builder().build();

    private class Browser {
        final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(10)).build();
        String token;
        String header;
        HttpResponse<String> send(String method, String path, String body, String contentType, boolean csrf) throws Exception {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(45));
            if (contentType != null) { request.header("Content-Type", contentType); }
            if (csrf && token != null) { request.header(header, token); }
            request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
        HttpResponse<String> get(String path) throws Exception { return send("GET", path, null, null, false); }
        void csrf() throws Exception {
            var response = get("/api/v1/auth/csrf"); assertEquals(200, response.statusCode(), response.body());
            JsonNode node = json.readTree(response.body()); token = node.get("token").asString(); header = node.get("headerName").asString();
        }
        HttpResponse<String> mutate(String method, String path, Object body) throws Exception {
            return send(method, path, json.writeValueAsString(body), "application/json", true);
        }
        JsonNode successful(String method, String path, Object body, int expected) throws Exception {
            var response = mutate(method, path, body); assertEquals(expected, response.statusCode(), response.body());
            return response.body().isEmpty() ? null : json.readTree(response.body());
        }
        void login(String email, String password) throws Exception {
            csrf();
            var response = send("POST", "/api/v1/auth/login", "email=" + URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, java.nio.charset.StandardCharsets.UTF_8), "application/x-www-form-urlencoded", true);
            assertEquals(204, response.statusCode(), response.body()); csrf();
        }
    }
    private Browser admin() throws Exception { Browser b = new Browser(); b.login("admin@example.test", "Admin-password-123"); return b; }
    private String email() { return UUID.randomUUID() + "@example.test"; }
    private Browser customer() throws Exception {
        Browser b = new Browser(); b.csrf(); String email = email();
        b.successful("POST", "/api/v1/auth/register", Map.of("email", email, "password", "Customer-password-123", "name", "Customer"), 201);
        b.login(email, "Customer-password-123"); return b;
    }
    private long movie(Browser admin) throws Exception {
        return admin.successful("POST", "/api/v1/staff/movies", movieBody("2.50"), 201).get("id").asLong();
    }
    private Map<String, Object> movieBody(String rate) {
        return Map.of("title", "Alien " + UUID.randomUUID(), "synopsis", "A science fiction film", "releaseYear", 1979,
            "dailyRate", rate, "genreIds", List.of());
    }
    private long copy(Browser admin, long movie) throws Exception {
        return admin.successful("POST", "/api/v1/staff/copies", Map.of("movieId", movie, "barcode", UUID.randomUUID().toString()), 201).get("id").asLong();
    }
    private JsonNode getJson(Browser browser, String path) throws Exception {
        var response = browser.get(path); assertEquals(200, response.statusCode(), response.body()); return json.readTree(response.body());
    }

    @Test void registrationSessionsCsrfAndLogout() throws Exception {
        Browser b = new Browser(); String email = email().toUpperCase(Locale.ROOT);
        Map<String, String> registration = Map.of("email", email, "name", "Test", "password", "Customer-password-123");
        assertEquals(403, b.mutate("POST", "/api/v1/auth/register", registration).statusCode());
        b.csrf();
        var user = b.successful("POST", "/api/v1/auth/register", registration, 201);
        assertEquals("CUSTOMER", user.get("role").asString());
        assertEquals(email.toLowerCase(Locale.ROOT), user.get("email").asString());
        assertFalse(user.has("passwordHash"));
        assertEquals(409, b.mutate("POST", "/api/v1/auth/register", registration).statusCode());
        assertEquals(400, b.mutate("POST", "/api/v1/auth/register", Map.of("email", email(), "name", "Test", "password", "short")).statusCode());
        assertEquals(400, b.mutate("POST", "/api/v1/auth/register", Map.of("email", email(), "name", "Test", "password", "界".repeat(30))).statusCode());
        String stored = jdbc.queryForObject("select password_hash from users where id = ?", String.class, user.get("id").asLong());
        assertNotEquals("Customer-password-123", stored); assertTrue(stored.startsWith("$2"));
        String oldSession = b.cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        String oldToken = b.token;
        b.login(email, "Customer-password-123");
        String newSession = b.cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        assertNotEquals(oldSession, newSession);
        assertEquals(user.get("id").asLong(), getJson(b, "/api/v1/auth/me").get("id").asLong());
        String newToken = b.token; b.token = oldToken;
        assertEquals(403, b.mutate("POST", "/api/v1/auth/logout", Map.of()).statusCode());
        b.token = newToken;
        b.successful("POST", "/api/v1/auth/logout", Map.of(), 204);
        assertEquals(401, b.get("/api/v1/auth/me").statusCode());
    }

    @Test void roleBoundariesAndCatalogValidation() throws Exception {
        Browser admin = admin(); Browser customer = customer(); String staffEmail = email();
        admin.successful("POST", "/api/v1/admin/staff", Map.of("email", staffEmail, "password", "Staff-password-123"), 201);
        Browser staff = new Browser(); staff.login(staffEmail, "Staff-password-123");
        assertEquals(200, staff.get("/api/v1/staff/customers").statusCode());
        staff.successful("POST", "/api/v1/staff/movies", movieBody("2.50"), 201);
        assertEquals(403, staff.mutate("POST", "/api/v1/admin/staff", Map.of("email", email(), "password", "Staff-password-123")).statusCode());
        assertEquals(403, customer.mutate("POST", "/api/v1/staff/movies", movieBody("2.50")).statusCode());
        assertEquals(401, new Browser().get("/api/v1/staff/copies").statusCode());
        assertEquals(400, admin.mutate("POST", "/api/v1/staff/movies", movieBody("-1.00")).statusCode());
        assertEquals(400, new Browser().get("/api/v1/movies?size=101").statusCode());
        assertEquals(400, new Browser().get("/api/v1/movies?sort=synopsis").statusCode());
        var genre = admin.successful("POST", "/api/v1/staff/genres", Map.of("name", "Genre " + UUID.randomUUID()), 201);
        var body = new HashMap<>(movieBody("2.50")); body.put("genreIds", List.of(genre.get("id").asLong()));
        var film = admin.successful("POST", "/api/v1/staff/movies", body, 201);
        assertEquals(1, film.get("genres").size());
        assertEquals(409, admin.mutate("DELETE", "/api/v1/staff/genres/" + genre.get("id").asLong(), Map.of()).statusCode());
        Map<String, Object> inventory = Map.of("movieId", film.get("id").asLong(), "barcode", UUID.randomUUID().toString());
        admin.successful("POST", "/api/v1/staff/copies", inventory, 201);
        assertEquals(409, admin.mutate("POST", "/api/v1/staff/copies", inventory).statusCode());
        assertEquals(1, getJson(new Browser(), "/api/v1/movies/" + film.get("id").asLong() + "/availability").get("totalElements").asInt());
    }

    @Test void rentalOwnershipPriceSnapshotsAndInspectedReturns() throws Exception {
        Browser admin = admin(), owner = customer(), stranger = customer();
        long movie = movie(admin), first = copy(admin, movie), second = copy(admin, movie);
        var rental = owner.successful("POST", "/api/v1/rentals", Map.of("copyIds", List.of(first, second), "days", 3), 201);
        long id = rental.get("id").asLong();
        assertEquals(0, new java.math.BigDecimal("15.00").compareTo(rental.get("rentalTotal").decimalValue()));
        assertEquals("2026-11-19", rental.get("items").get(0).get("dueDate").asString());
        admin.successful("PUT", "/api/v1/staff/movies/" + movie, movieBody("9.99"), 200);
        assertEquals(0, new java.math.BigDecimal("15.00").compareTo(getJson(owner, "/api/v1/rentals/" + id).get("rentalTotal").decimalValue()));
        assertEquals(404, stranger.get("/api/v1/rentals/" + id).statusCode());
        assertEquals(409, owner.mutate("POST", "/api/v1/rentals", Map.of("copyIds", List.of(first), "days", 3)).statusCode());
        long firstItem = rental.get("items").get(0).get("id").asLong(), secondItem = rental.get("items").get(1).get("id").asLong();
        String firstReturn = "/api/v1/staff/rentals/" + id + "/items/" + firstItem + "/return";
        assertEquals(403, owner.mutate("POST", firstReturn, Map.of("inspectionResult", "AVAILABLE")).statusCode());
        admin.successful("POST", firstReturn, Map.of("inspectionResult", "AVAILABLE"), 200);
        assertEquals(409, admin.mutate("POST", firstReturn, Map.of("inspectionResult", "AVAILABLE")).statusCode());
        var resolved = admin.successful("POST", "/api/v1/staff/rentals/" + id + "/items/" + secondItem + "/return",
            Map.of("inspectionResult", "DAMAGED"), 200);
        assertEquals("CLOSED", resolved.get("status").asString());
        assertEquals(1, getJson(new Browser(), "/api/v1/movies/" + movie + "/availability").get("totalElements").asInt());
        assertEquals("DAMAGED", jdbc.queryForObject("select status from dvd_copies where id = ?", String.class, second));
    }

    @Test void failedMultiCopyAllocationRollsBackAllCopies() throws Exception {
        Browser admin = admin(), owner = customer(); long movie = movie(admin);
        long available = copy(admin, movie), maintenance = copy(admin, movie);
        admin.successful("PUT", "/api/v1/staff/copies/" + maintenance + "/status", Map.of("status", "MAINTENANCE"), 200);
        assertEquals(409, owner.mutate("POST", "/api/v1/rentals", Map.of("copyIds", List.of(available, maintenance), "days", 1)).statusCode());
        assertEquals("AVAILABLE", jdbc.queryForObject("select status from dvd_copies where id = ?", String.class, available));
        assertEquals(400, owner.mutate("POST", "/api/v1/rentals", Map.of("copyIds", List.of(available, available), "days", 1)).statusCode());
    }

    @Test void twoConcurrentRequestsCannotRentTheSamePhysicalCopy() throws Exception {
        Browser admin = admin(), first = customer(), second = customer(); long copy = copy(admin, movie(admin));
        var start = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Integer> one = () -> { start.await(10, TimeUnit.SECONDS); return first.mutate("POST", "/api/v1/rentals", Map.of("copyIds", List.of(copy), "days", 1)).statusCode(); };
            Callable<Integer> two = () -> { start.await(10, TimeUnit.SECONDS); return second.mutate("POST", "/api/v1/rentals", Map.of("copyIds", List.of(copy), "days", 1)).statusCode(); };
            var a = executor.submit(one); var b = executor.submit(two);
            var statuses = new ArrayList<>(List.of(a.get(60, TimeUnit.SECONDS), b.get(60, TimeUnit.SECONDS)));
            statuses.sort(Integer::compareTo); assertEquals(List.of(201, 409), statuses);
        }
        assertEquals(1, jdbc.queryForObject("select count(*) from rental_items where copy_id = ? and status = 'ACTIVE'", Integer.class, copy));
    }

    @Test void overdueChargesUseSnapshotRateAndFinalizeOnStaffReturn() throws Exception {
        Browser admin = admin(), owner = customer(); long copy = copy(admin, movie(admin));
        var rental = owner.successful("POST", "/api/v1/rentals", Map.of("copyIds", List.of(copy), "days", 1), 201);
        long item = rental.get("items").get(0).get("id").asLong();
        jdbc.update("update rental_items set due_date = ? where id = ?", java.sql.Date.valueOf("2026-11-13"), item);
        var estimate = getJson(owner, "/api/v1/rentals/" + rental.get("id").asLong());
        assertEquals(0, new java.math.BigDecimal("4.50").compareTo(estimate.get("overdueTotal").decimalValue()));
        var result = admin.successful("POST", "/api/v1/staff/rentals/" + rental.get("id").asLong() + "/items/" + item + "/return",
            Map.of("inspectionResult", "LOST"), 200);
        assertFalse(result.get("items").get(0).get("overdueEstimate").asBoolean());
        assertEquals("LOST", result.get("items").get(0).get("status").asString());
        assertEquals(0, new java.math.BigDecimal("4.50").compareTo(result.get("overdueTotal").decimalValue()));
    }
}
