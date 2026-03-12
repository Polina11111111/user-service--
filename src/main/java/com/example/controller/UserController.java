package com.example.controller;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "Управление пользователями")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Сохраняет пользователя и отправляет событие в Kafka")
    public UserResponse create(@RequestBody @Valid UserRequest request) {
        return addLinks(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public UserResponse get(@PathVariable Long id) {
        return addLinks(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Список всех пользователей")
    public List<UserResponse> getAll() {
        return service.getAll().stream()
                .map(this::addLinks)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public UserResponse update(@PathVariable Long id, @RequestBody @Valid UserRequest request) {
        return addLinks(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private UserResponse addLinks(UserResponse response) {

        response.add(linkTo(methodOn(UserController.class).get(response.getId())).withSelfRel());

        response.add(linkTo(UserController.class).slash(response.getId()).withRel("delete"));

        response.add(linkTo(methodOn(UserController.class).getAll()).withRel("all-users"));

        return response;
    }
}