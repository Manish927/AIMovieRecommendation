package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.UserEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.UserRepository;
import com.spring5.movieservice.domain.service.User;
import com.spring5.movieservice.domain.service.UserMapper;
import com.spring5.movieservice.domain.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import java.util.logging.Level;

@RestController
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository repository, UserMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.userMapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<User> createUser(User user) {

        if (user.getUserID() < 1) {
            throw new InvalidInputException("Invalid UserID: " + user.getUserID());
        }

        UserEntity entity = userMapper.apiToEntity(user);
        Mono<User> newUserEntity = repository.save(entity).log(LOG.getName(), Level.FINE)
                .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate Key, User ID: " + user.getUserID()))
                .map(user1 -> userMapper.entityToAPi(user1));

        return newUserEntity;
    }

    @Override
    public Mono<User> getUser(Integer userId) {

        if (userId < 1) {
            throw new InvalidInputException("Invalid UserID: " + userId);
        }

        LOG.info("getting user info for ID={}", userId);

        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("No User found for User ID: " + userId)))
                .log(LOG.getName(), Level.FINE)
                .map(entity -> userMapper.entityToAPi(entity))
                .map(entity -> setServiceAddress(entity));
    }

    @Override
    public Mono<Void> deleteUser(Integer userID) {
        if (userID < 1) {
            throw new InvalidInputException("Invalid productID: " + userID);
        }

        LOG.debug("deleteProduct: tried to delete an entity with userID: {}", userID);

        return repository.findByUserId(userID).log(LOG.getName(), Level.FINE)
                .map(userEntity -> repository.delete(userEntity)).flatMap(entity -> entity);
    }


    private User setServiceAddress(User e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
