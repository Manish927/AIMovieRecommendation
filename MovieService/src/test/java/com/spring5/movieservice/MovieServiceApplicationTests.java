package com.spring5.movieservice;

import com.spring5.movieservice.common.Event;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.repository.UserRepository;
import com.spring5.movieservice.domain.service.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.function.Consumer;

import static com.spring5.movieservice.common.Event.Type.CREATE;
import static com.spring5.movieservice.common.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MovieServiceApplicationTests {

    @Autowired
    WebTestClient client;

    @Autowired
    UserRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, User>> messageProcessor;

    @BeforeEach
    void setupDB() {
        repository.deleteAll().block();
    }

    @Test
    void getUserByID() {
        int userID = 1;

        assertNull(repository.findByUserID(userID).block());
        assertEquals(0, (long) repository.count().block());

        sendCreateUserEvent(userID);

        assertNotNull(repository.findByUserID(userID).block());
        assertEquals(1, (long)repository.count().block());

        getAndVerifyUser(userID, HttpStatus.OK).jsonPath("$.userID").isEqualTo(userID);

    }

    @Test
    void duplicateError() {
        int userID = 1;
        assertNull(repository.findByUserID(userID).block());
        sendCreateUserEvent(userID);

        assertNull(repository.findByUserID(userID).block());

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> sendCreateUserEvent(userID)
        ,"Expected a InvalidInputException here !");

        assertEquals("Duplicate Key, User ID :" + userID, thrown.getMessage());
    }
    @Test
    void deleteUser() {
        int userID = 1;
        sendCreateUserEvent(userID);
        assertNotNull(repository.findByUserID(userID).block());

        sendDeleteUserEvent(userID);
        assertNull(repository.findByUserID(userID).block());
        sendDeleteUserEvent(userID);
    }
    @Test
    void getUserInvalidParameterString() {
        getAndVerifyUser("/no-integer", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }
    private void sendCreateUserEvent(int userID) {
        User user = new User(userID, "Name", "email", "phone", "serviceAddress");
        Event<Integer, User> event = new Event<>(CREATE, userID, user);
        messageProcessor.accept(event);
    }

    private void sendDeleteUserEvent(int userID) {
        Event<Integer, User> event = new Event<>(DELETE, userID, null);
        messageProcessor.accept(event);
    }

    private WebTestClient.BodyContentSpec getAndVerifyUser(int userID, HttpStatus expectedStatus) {
        return getAndVerifyUser("/" + userID, expectedStatus);
    }
    private WebTestClient.BodyContentSpec getAndVerifyUser(String userIDPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/user/" + userIDPath)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

}
