package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import ru.danil.springtest.api.UserAndOrdersApi;
import ru.danil.springtest.dto.UserDTO;
import ru.danil.springtest.mapper.UserMapper;
import ru.danil.springtest.service.UserService;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class UserController implements UserAndOrdersApi {
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<UserDTO> getUsernameById(UUID id) {
        return ResponseEntity.ok(userMapper.toUserDTO(userService.getUsernameById(id)));
    }

    @Override
    public ResponseEntity<UserDTO> createUser(@Valid UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(userMapper.toUserDTO(userService.createUser(userMapper.toUser(userDTO))));
    }

    @Override
    public ResponseEntity<UserDTO> userUpdate(UUID id, @Valid UserDTO userDTO) {
        return ResponseEntity.ok(userMapper.toUserDTO(userService.updateUser(id, userMapper.toUser(userDTO))));
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}