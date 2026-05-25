package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import ru.danil.springtest.api.UserAndOrdersApi;
import ru.danil.springtest.dto.UserDTO;
import ru.danil.springtest.service.UserService;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class UserController implements UserAndOrdersApi {
    private final UserService userService;

    @Override
    public ResponseEntity<UserDTO> getUsernameById(UUID id) {
        return ResponseEntity.ok(userService.getUsernameById(id));
    }

    @Override
    public ResponseEntity<UserDTO> createUser(@Valid UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(userService.createUser(userDTO));
    }

    @Override
    public ResponseEntity<UserDTO> userUpdate(UUID id, @Valid UserDTO updatedUserDTO) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUserDTO));
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}