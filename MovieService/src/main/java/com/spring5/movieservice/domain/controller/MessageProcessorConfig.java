package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.Event;
import com.spring5.movieservice.domain.exception.EventProcessingException;
import com.spring5.movieservice.domain.service.User;
import com.spring5.movieservice.domain.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final UserService userService;

    @Autowired
    public MessageProcessorConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public Consumer<Event<Integer, User>> messageProcessor() {

        return userEvent -> {
            LOG.info("Process message created at {}...", userEvent.getEventCreatedAt());

            switch (userEvent.getEventType()) {
                case CREATE:
                    User user = userEvent.getData();
                    LOG.info("Create product with ID: {}", user.getUserID());
                    userService.createUser(user).block();
                    break;

                case DELETE:
                    int userId = userEvent.getKey();
                    LOG.info("Delete user with UserID: {}", userId);
                    userService.deleteUser(userId).block();
                    break;

                default:
                    String errorMsg = "Incorrect evet type: " + userEvent.getEventType() + "expected a CREATE or DELETE" +
                            "event";
                    LOG.warn(errorMsg);
                    throw new EventProcessingException(errorMsg);
            }

            LOG.info("Message Processing done!");
        };
    }
}
