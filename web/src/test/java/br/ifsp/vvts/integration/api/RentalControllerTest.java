package br.ifsp.vvts.integration.api;

import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.vvts.rental.requests.PostRequest;
import br.ifsp.vvts.utils.BaseApiIntegrationTest;
import br.ifsp.vvts.utils.EntityBuilder;
import io.restassured.response.Response;
import jdk.jfr.Description;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RentalControllerTest extends BaseApiIntegrationTest {

    @Nested
    class CreateRental {

        private Response createAndSendRentalRequest(UUID propertyId, LocalDate startDate, LocalDate endDate, String token) {
            PostRequest postRequest = new PostRequest(propertyId, startDate, endDate);

            var request = given().contentType("application/json").port(port).body(postRequest);

            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }

            return request.when().post("/api/v1/rental");
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should create a rental when the request is valid")
        void shouldCreateRentalWhenRequestIsValid() {
            UserEntity owner = registerAdminUser("x56das!p01A");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity user = registerUser("j783as!p0BA");
            String token = authenticate(user.getEmail(), "j783as!p0BA");

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    token
            );
            assertEquals(201, response.getStatusCode());
            assertEquals(user.getId().toString(), response.getBody().jsonPath().get("tenantId"));
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 400 Bad Request when propertyId is missing or null")
        void shouldReturnBadRequestWhenPropertyIdIsMissing() {
            UserEntity user = registerUser("j783as!p0BB");
            String token = authenticate(user.getEmail(), "j783as!p0BB");

            Response response = createAndSendRentalRequest(
                    null,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    token
            );

            assertEquals(400, response.getStatusCode());
        }

        @ParameterizedTest
        @MethodSource("invalidDatesProvider")
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 400 Bad Request for invalid date combinations")
        void shouldReturnBadRequestForInvalidDates(LocalDate startDate, LocalDate endDate) {
            UserEntity owner = registerAdminUser("x56das!paramA");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity user = registerUser("x56das!paramB");
            String token = authenticate(user.getEmail(), "x56das!paramB");

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    startDate,
                    endDate,
                    token
            );

            assertEquals(400, response.getStatusCode(), "Esperado 400 para datas inválidas");
        }

        static Stream<Arguments> invalidDatesProvider() {
            return Stream.of(
                    Arguments.of(null, LocalDate.now().plusDays(5)),                      // startDate null
                    Arguments.of(LocalDate.now().plusDays(1), null),                      // endDate null
                    Arguments.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(1)), // startDate > endDate
                    Arguments.of(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5)) // startDate no passado
            );
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 404 Not Found when the property does not exist")
        void shouldReturnNotFoundWhenPropertyDoesNotExist() {
            UserEntity user = registerUser("j783as!p0BG");
            String token = authenticate(user.getEmail(), "j783as!p0BG");

            Response response = createAndSendRentalRequest(
                    UUID.randomUUID(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    token
            );

            assertEquals(404, response.getStatusCode());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 401 Unauthorized when no authentication token is provided")
        void shouldReturnUnauthorizedWhenNoTokenIsProvided() {
            UserEntity owner = registerAdminUser("x56das!authA");
            PropertyEntity property = createRandomProperty(owner);

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    null
            );

            assertEquals(401, response.getStatusCode());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 401 Unauthorized when the token is invalid")
        void shouldReturnUnauthorizedWhenTokenIsInvalid() {
            UserEntity owner = registerAdminUser("x56das!authB");
            PropertyEntity property = createRandomProperty(owner);

            String invalidToken = "invalid.token.12345";

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    invalidToken
            );

            assertEquals(401, response.getStatusCode());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 400 Bad Request when the owner tries to rent their own property")
        void shouldReturnBadRequestWhenOwnerTriesToRentOwnProperty() {
            UserEntity owner = registerAdminUser("x56das!p08A");
            PropertyEntity property = createRandomProperty(owner);

            String ownerToken = authenticate(owner.getEmail(), "x56das!p08A");

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5),
                    ownerToken
            );

            assertEquals(400, response.getStatusCode());
        }

        @Disabled
        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 400 if the value is too high to the Big Decimal")
        void shouldReturnBadRequestWhenValueIsTooHighToBigDecimal() {
            UserEntity owner = registerAdminUser("x56das!p08A");
            PropertyEntity property = EntityBuilder.createPropertyWithPrice(owner, Double.MAX_VALUE + 100);
            propertyRepository.save(property);

            String ownerToken = authenticate(owner.getEmail(), "x56das!p08A");

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3),
                    ownerToken
            );
            List<RentalEntity> rental = rentalRepository.findByPropertyEntityId(property.getId());
            assertEquals(400, response.getStatusCode());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return bad request when rental duration is greater than one year")
        void shouldReturnBadRequestWhenRentalDurationIsGreaterThan1Year() {
            UserEntity owner = registerAdminUser("x56das!p01A");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity user = registerUser("j783as!p0BA");
            String token = authenticate(user.getEmail(), "j783as!p0BA");

            Response response = createAndSendRentalRequest(
                    property.getId(),
                    LocalDate.now(),
                    LocalDate.now().plusYears(1).plusDays(1),
                    token
            );
            assertEquals(400, response.getStatusCode());
            assertEquals("Bad Request", response.jsonPath().getString("message"));
            assertEquals("Rental duration must be 1 year or less", response.jsonPath().getString("detail"));
        }
    }

    @Nested
    class FindAll {
        private Response findAllRequest(String token) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            return request.when().get("/api/v1/rental");
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 401 when user try to access other user rentals")
        void userShouldNotAccessOtherUserRentals() {
            UserEntity owner1 = registerAdminUser("x56das!p08A");
            PropertyEntity property = createRandomProperty(owner1);
            createRentalEntity(owner1, property,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(7));

            UserEntity owner2 = registerAdminUser("b75kes!k07B");
            String token = authenticate(owner2.getEmail(), "b75kes!k07B");
            Response response = findAllRequest(token);

            System.out.println("Response: " + response.prettyPrint());
            assertEquals(401, response.getStatusCode());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 204 when there are no rentals registered")
        void shouldReturn204WhenThereAreNoRentalsRegistered() {
            UserEntity owner = registerAdminUser("x56das!p08A");
            String token = authenticate(owner.getEmail(), "x56das!p08A");
            Response response = findAllRequest(token);
            assertEquals(204, response.getStatusCode());
        }

        @Test
        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Description("Should return 401 when token is invalid")
        void shouldReturn401WhenTokenIsInvalid() {
            Response response = findAllRequest(null);
            assertEquals(401, response.getStatusCode());

            Response response1 = findAllRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTYiLCJlbWFpbCI6InVzZXJAbWFpbC5jb20iLCJyb2xlIjoiVVNFUiIsImlhdCI6MTcxODcwMDAwMCwiZXhwIjoxNzE4NzA0MDAwfQ.fake-signature");
            assertEquals(401, response1.getStatusCode());
        }
    }

    @Nested
    class FindAllByPropertyId {
        private Response findAllByPropertyIdRequest(String token, String propertyId) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            String path = "/api/v1/rental/properties/" + propertyId;
            return request.when().get(path);
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 and list all rentals by property ID")
        void shouldReturn200AndListAllRentalsByPropertyId() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String ownerToken = authenticate(owner.getEmail(), "validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity tenant = registerUser("validPassword123!");
            createRentalEntity(tenant, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
            createRentalEntity(tenant, property, LocalDate.now().plusDays(30), LocalDate.now().plusDays(35));

            Response response = findAllByPropertyIdRequest(ownerToken, String.valueOf(property.getId()));
            assertEquals(200, response.statusCode());

            var rentals = response.jsonPath().getList("$");
            assertEquals(2, rentals.size());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 403 when user is not the owner of the property")
        void shouldReturn403WhenUserIsNotTheOwnerOfTheProperty() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity user1 = registerUser("validPassword123!");
            createRentalEntity(user1, property,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
            createRentalEntity(user1, property,
                    LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));

            UserEntity user2 = registerUser("validPassword123!");
            String token = authenticate(user2.getEmail(), "validPassword123!");
            Response response = findAllByPropertyIdRequest(token, String.valueOf(property.getId()));

            assertEquals(403, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 with empty list when property has no rentals")
        void shouldReturnEmptyListWhenPropertyHasNoRentals() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String token = authenticate(owner.getEmail(), "validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            Response response = findAllByPropertyIdRequest(token, String.valueOf(property.getId()));

            assertEquals(200, response.getStatusCode());
            var rentals = response.jsonPath().getList("$");
            assertTrue(rentals.isEmpty());
        }
    }

    @Nested
    class FindRentalHistoryByTenantId {
        private Response findRentalHistoryByTenantIdRequest(String token, String tenantId) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            String path = "/api/v1/rental/tenants/" + tenantId;
            return request.when().get(path);
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 and list all rentals by tenant id")
        void shouldReturn200AndListAllRentalsByTenantId() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property1 = createRandomProperty(owner);
            PropertyEntity property2 = createRandomProperty(owner);

            UserEntity tenant = registerUser("validPassword123!");
            createRentalEntity(tenant, property1, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5));
            createRentalEntity(tenant, property2, LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(15));

            String token = authenticate(tenant.getEmail(), "validPassword123!");
            Response response = findRentalHistoryByTenantIdRequest(token, String.valueOf(tenant.getId()));

            var rentals = response.jsonPath().getList("$");
            assertEquals(200, response.getStatusCode());
            assertEquals(2, rentals.size());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 403 when user tries to access another tenant rental history")
        void shouldReturn403WhenUserTriesToAccessAnotherTenantRentalHistory() {
            UserEntity owner = registerUser("validPassword123!");
            PropertyEntity property1 = createRandomProperty(owner);

            UserEntity user1 = registerUser("validPassword123!");
            createRentalEntity(user1, property1, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5));

            UserEntity user2 = registerUser("validPassword123!");
            String token = authenticate(user2.getEmail(), "validPassword123!");

            Response response = findRentalHistoryByTenantIdRequest(token, String.valueOf(user1.getId()));
            assertEquals(403, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 and empty list when tenant has no rentals")
        void shouldReturn200AndEmptyListWhenTenantHasNoRentals() {
            UserEntity tenant = registerUser("validPassword123!");
            String token = authenticate(tenant.getEmail(), "validPassword123!");

            Response response = findRentalHistoryByTenantIdRequest(token, String.valueOf(tenant.getId()));

            var rentals = response.jsonPath().getList("$");
            assertEquals(200, response.getStatusCode());
            assertTrue(rentals.isEmpty());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 404 when tenant id does not exist")
        void shouldReturn404WhenTenantIdDoesNotExist() {
            UserEntity tenant = registerUser("validPassword123!");
            String token = authenticate(tenant.getEmail(), "validPassword123!");

            Response response = findRentalHistoryByTenantIdRequest(token, String.valueOf(UUID.randomUUID()));

            assertEquals(404, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 401 when no token is invalid")
        void shouldReturn401WhenTokenIsInvalid() {
            UserEntity tenant = registerUser("validPassword123!");
            Response response1 = findRentalHistoryByTenantIdRequest(null, String.valueOf(tenant.getId()));
            Response response2 = findRentalHistoryByTenantIdRequest("", String.valueOf(tenant.getId()));
            Response response3 = findRentalHistoryByTenantIdRequest("invalid-token", String.valueOf(tenant.getId()));
            assertAll(
                    () -> assertEquals(401, response1.getStatusCode()),
                    () -> assertEquals(401, response2.getStatusCode()),
                    () -> assertEquals(401, response3.getStatusCode())
            );
        }
    }

    @Nested
    class ConfirmRental {

        private Response confirmRentalRequest(String token, String rentalId) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            String path = "/api/v1/rental/" + rentalId + "/owner/confirm";
            return request.when().put(path);
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 when rental is successfully confirmed")
        void shouldReturn200WhenRentalIsSuccessfullyConfirmed() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);
            String token = authenticate(owner.getEmail(), "validPassword123!");

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5));

            Response response = confirmRentalRequest(token, String.valueOf(rental.getId()));

            assertEquals(owner.getId(), UUID.fromString(response.jsonPath().getString("ownerId")));
            assertEquals(tenant.getId(), UUID.fromString(response.jsonPath().getString("tenantId")));
            RentalEntity savedRental = rentalRepository.findByPropertyEntityId(property.getId()).getFirst();
            assertEquals(savedRental.getState(), RentalState.CONFIRMED);
            assertEquals(200, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 404 when rental not found")
        void shouldReturn404WhenRentalNotFound() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String token = authenticate(owner.getEmail(), "validPassword123!");

            Response response = confirmRentalRequest(token, String.valueOf(UUID.randomUUID()));

            assertEquals(404, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 403 when some user try to accept a rental that he is not owner")
        void shouldReturn403WhenAuthenticatedUserIsNotTheOwner() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity user1 = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(user1, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5));

            UserEntity user2 = registerUser("validPassword123!");
            String token = authenticate(user2.getEmail(), "validPassword123!");

            Response response = confirmRentalRequest(token, String.valueOf(rental.getId()));
            assertEquals(403, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 400 when rental ID format is invalid")
        void shouldReturn400WhenRentalIdIsInvalid() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String token = authenticate(owner.getEmail(), "validPassword123!");
            String invalidRentalId = "not-a-valid-uuid";
            Response response = confirmRentalRequest(token, invalidRentalId);
            assertEquals(400, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 409 Conflict when the rental has already been confirmed")
        void shouldReturn409WhenRentalIsAlreadyConfirmed() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);
            String token = authenticate(owner.getEmail(), "validPassword123!");

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5));

            Response firstResponse = confirmRentalRequest(token, String.valueOf(rental.getId()));
            assertEquals(200, firstResponse.getStatusCode());

            Response secondResponse = confirmRentalRequest(token, String.valueOf(rental.getId()));
            assertEquals(409, secondResponse.getStatusCode());
        }
    }
    @Nested
    class DenyRental {

        private Response denyRentalRequest(String token, String rentalId) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            String path = "/api/v1/rental/" + rentalId + "/owner/deny";
            return request.when().put(path);
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 when rental is successfully denied by the owner")
        void shouldReturn200WhenRentalIsSuccessfullyDenied() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);
            String token = authenticate(owner.getEmail(), "validPassword123!");

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

            Response response = denyRentalRequest(token, String.valueOf(rental.getId()));

            assertEquals(200, response.getStatusCode());
            RentalEntity updated = rentalRepository.findById(rental.getId()).orElseThrow();
            assertEquals(RentalState.DENIED, updated.getState());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 404 when rental not found")
        void shouldReturn404WhenRentalNotFound() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String token = authenticate(owner.getEmail(), "validPassword123!");

            Response response = denyRentalRequest(token, UUID.randomUUID().toString());

            assertEquals(404, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 403 when user is not the owner of the property")
        void shouldReturn403WhenUserIsNotOwner() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

            UserEntity otherUser = registerUser("validPassword123!");
            String token = authenticate(otherUser.getEmail(), "validPassword123!");

            Response response = denyRentalRequest(token, rental.getId().toString());

            assertEquals(403, response.getStatusCode());
        }

        @ParameterizedTest
        @MethodSource("invalidStatesProvider")
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 400 when rental is in invalid state for denial")
        void shouldReturn400WhenRentalIsInInvalidState(RentalState state) {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);
            String token = authenticate(owner.getEmail(), "validPassword123!");

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), state);

            Response response = denyRentalRequest(token, rental.getId().toString());

            assertEquals(400, response.getStatusCode());
        }

        static Stream<RentalState> invalidStatesProvider() {
            return Stream.of(
                    RentalState.CONFIRMED,
                    RentalState.DENIED,
                    RentalState.CANCELLED,
                    RentalState.EXPIRED
            );
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 401 when no token is provided")
        void shouldReturn401WhenNoTokenProvided() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property,
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

            Response response = denyRentalRequest(null, rental.getId().toString());

            assertEquals(401, response.getStatusCode());
        }
    }
    @Nested
    class CancelRental {
        private Response cancelRentalRequest(String token, String rentalId, String cancelDate) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            if (cancelDate != null) {
                request.queryParam("cancelDate", cancelDate);
            }

            String path = "/api/v1/rental/" + rentalId + "/owner/cancel";
            return request.when().put(path);
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 when rental is successfully cancelled")
        void shouldReturn200WhenRentalIsSuccessfullyCancelled() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);
            String token = authenticate(owner.getEmail(), "validPassword123!");

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5), RentalState.CONFIRMED);

            Response response = cancelRentalRequest(token, String.valueOf(rental.getId()), null);

            assertEquals(owner.getId(), UUID.fromString(response.jsonPath().getString("ownerId")));
            assertEquals(tenant.getId(), UUID.fromString(response.jsonPath().getString("tenantId")));

            RentalEntity updatedRental = rentalRepository.findById(rental.getId()).orElseThrow();
            assertEquals(RentalState.CANCELLED, updatedRental.getState());
            assertEquals(200, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 404 when rental not found")
        void shouldReturn404WhenRentalNotFound() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String token = authenticate(owner.getEmail(), "validPassword123!");
            Response response = cancelRentalRequest(token, UUID.randomUUID().toString(), null);
            assertEquals(404, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 403 when authenticated user is not the property owner")
        void shouldReturn403WhenUserIsNotOwner() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5), RentalState.CONFIRMED);

            UserEntity otherUser = registerUser("validPassword123!");
            String token = authenticate(otherUser.getEmail(), "validPassword123!");

            Response response = cancelRentalRequest(token, rental.getId().toString(), null);
            assertEquals(403, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 400 when rental ID is invalid")
        void shouldReturn400WhenRentalIdIsInvalid() {
            UserEntity owner = registerAdminUser("validPassword123!");
            String token = authenticate(owner.getEmail(), "validPassword123!");
            Response response = cancelRentalRequest(token, "invalid-uuid", null);
            assertEquals(400, response.getStatusCode());
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 400 when rental is already cancelled")
        void shouldReturn400WhenRentalIsAlreadyCancelled() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);
            String token = authenticate(owner.getEmail(), "validPassword123!");

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5), RentalState.CANCELLED);

            Response response = cancelRentalRequest(token, rental.getId().toString(), null);
            assertEquals(400, response.getStatusCode());
        }
    }

    @Nested
    class TenantCancelRental {
        private Response tenantCancelRentalRequest(String token, String rentalId) {
            var request = given().contentType("application/json").port(port);
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            String path = "/api/v1/rental/" + rentalId + "/tenant/cancel";
            return request.when().put(path);
        }

        @Test
        @Tag("IntegrationTest")
        @Tag("ApiTest")
        @Description("Should return 200 when rental is successfully cancelled")
        void shouldReturn200WhenRentalIsSuccessfullyCancelled() {
            UserEntity owner = registerAdminUser("validPassword123!");
            PropertyEntity property = createRandomProperty(owner);

            UserEntity tenant = registerUser("validPassword123!");
            RentalEntity rental = createRentalEntity(tenant, property, LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5), RentalState.CONFIRMED);

            String token = authenticate(tenant.getEmail(), "validPassword123!");

            Response response = tenantCancelRentalRequest(token, String.valueOf(rental.getId()));

            RentalEntity updatedRental = rentalRepository.findById(rental.getId()).orElseThrow();
            assertEquals(RentalState.CANCELLED, updatedRental.getState());
            assertEquals(200, response.getStatusCode());
        }
    }
}
