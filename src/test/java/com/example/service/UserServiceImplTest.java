package com.example.service;

import com.example.dto.UserRequest;
import com.example.entity.User;
import com.example.event.UserEvent;
import com.example.kafka.UserEventProducer;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserEventProducer producer; // Добавили мок продюсера

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void create_shouldReturnUserResponse() {
        // Given
        UserRequest request = new UserRequest();
        request.setName("Anna");
        request.setEmail("anna@test.com");
        request.setAge(25);

        User savedUser = new User("Anna", "anna@test.com", 25);
        // Эмулируем генерацию ID базой данных
        when(repository.save(any(User.class))).thenReturn(savedUser);

        // When
        var response = service.create(request);

        // Then
        assertEquals("Anna", response.getName());
        verify(repository, times(1)).save(any(User.class));
        verify(producer, times(1)).sendEvent(any(UserEvent.class)); // Проверяем вызов Кафки
    }

    @Test
    void getById_shouldReturnUser() {
        User user = new User("John", "john@test.com", 30);
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        var response = service.getById(1L);

        assertEquals("John", response.getName());
        verify(repository).findById(1L);
    }

    @Test
    void delete_shouldCallRepositoryAndProducer() {
        // Given
        User user = new User("John", "john@test.com", 30);
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        // When
        service.delete(1L);

        // Then
        verify(repository).delete(user);
        verify(producer).sendEvent(any(UserEvent.class)); // Проверяем вызов Кафки при удалении
    }
}