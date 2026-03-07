package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.entity.User;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.event.UserEvent;
import com.example.kafka.UserEventProducer;

import com.example.event.EventType;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserEventProducer producer;

    public UserServiceImpl(UserRepository repository,
                           UserEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Override
    public UserResponse create(UserRequest request) {

        User user = UserMapper.toEntity(request);
        User savedUser = repository.save(user);

        producer.sendEvent(
                new UserEvent(savedUser.getEmail(), EventType.USER_CREATED)
        );

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getById(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());

        return UserMapper.toResponse(repository.save(user));
    }

    @Override
    public void delete(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        repository.delete(user);

        producer.sendEvent(
                new UserEvent(user.getEmail(), EventType.USER_DELETED)
        );
    }
}