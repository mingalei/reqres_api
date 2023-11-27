package in.reqres.tests;

import in.reqres.models.CreateUserRequest;
import in.reqres.models.CreateUserResponse;
import in.reqres.models.RegisterUserRequest;
import in.reqres.models.UsersResponse;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static in.reqres.specs.SpecsReqres.request;
import static in.reqres.specs.SpecsReqres.responseSpec;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@Owner("Aydar Mingaleev")
@Feature("API test")
@Story("ReqresIn")
@Tags({@Tag("api")})

public class ReqresInTests {
    @Test
    @DisplayName("Получение списка пользователей")
    void getUserListWithLombok() {
        UsersResponse data = given().spec(request)
                .when()
                .get("/users?page=2")
                .then()
                .log().body()
                .extract().as(UsersResponse.class);
    }

    @Test
    @DisplayName("Проверка наличия пользователя в базе")
    void getSingleUserNotFound() {
        given()
                .spec(request)
                .when()
                .get("/users/23")
                .then()
                .statusCode(404);
    }
    @CsvFileSource(resources = "/users_data.csv")
    @ParameterizedTest
    @DisplayName("Создание пользователя")
    void createUserLombok(String name, String job) {
        CreateUserRequest body = new CreateUserRequest();
        body.setName(name);
        body.setJob(job);

        CreateUserResponse response = given().spec(request)
                .body(body)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().as(CreateUserResponse.class);

        assertEquals(body.getName(), response.getName());
        assertEquals(body.getJob(), response.getJob());
        assertFalse(response.getId().isEmpty());
        assertFalse(response.getCreatedAt().isEmpty());
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUserLombok() {
        CreateUserRequest body = new CreateUserRequest();
        body.setName("Gosling");
        body.setJob("Driver");

        CreateUserResponse response = given().spec(request)
                .body(body)
                .when()
                .put("/users/2")
                .then()
                .spec(responseSpec)
                .extract().as(CreateUserResponse.class);

        assertEquals(body.getName(), response.getName());
        assertEquals(body.getJob(), response.getJob());
    }

    @Test
    @DisplayName("Регистрация пользователя")
    void registerSuccessfulLombok() {
        RegisterUserRequest body = new RegisterUserRequest();
        body.setEmail("eve.holt@reqres.in");
        body.setPassword("qwerty");

        CreateUserResponse response = given().spec(request)
                .body(body)
                .when()
                .post("/register")
                .then()
                .spec(responseSpec)
                .extract().as(CreateUserResponse.class);

        assertEquals("4", response.getId());
        assertEquals("QpwL5tke4Pnpja7X4", response.getToken());
    }

    @CsvFileSource(resources = "/users_id_and_mail.csv")
    @ParameterizedTest
    @DisplayName("Проверка id и email пользователя")
    void checkIdAndEmailOfFeaturedUser(Integer id, String email) {
        UsersResponse userResponse = given().spec(request)
                .when()
                .pathParam("id", id)
                .get("/users/{id}")
                .then()
                .spec(responseSpec)
                .extract().jsonPath().getObject("data", UsersResponse.class);

        assertEquals(id, userResponse.getId());
        assertTrue(userResponse.getEmail().equals(email));
    }

    @Test
    @DisplayName("Проверка email пользователя с помощью Groovy")
    public void checkEmailUsingGroovy() {

        given()
                .spec(request)
                .when()
                .get("/users")
                .then()
                .log().body()
                .body("data.findAll{it.email =~/.*?@reqres.in/}.email.flatten()",
                        hasItem("eve.holt@reqres.in"));
    }
}
