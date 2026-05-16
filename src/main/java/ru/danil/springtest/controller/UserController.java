package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import ru.danil.springtest.api.UserAndOrdersApi;
import ru.danil.springtest.dto.UserDTO;
import ru.danil.springtest.model.User;
import ru.danil.springtest.service.UserService;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class UserController implements UserAndOrdersApi {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<UserDTO> getUsernameById(UUID id) {
        User user = userService.getUsernameById(id);
        return ResponseEntity.ok(convertToUserDTO(user));
    }


    @Override
    public ResponseEntity<UserDTO> createNewUser(@Valid UserDTO userDTO) {
        User user = userService.createNewUser(convertToUser(userDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToUserDTO(user));
    }


    @Override
    public ResponseEntity<UserDTO> userUpdate(UUID id,@Valid UserDTO userDTO) {
        userService.userUpdate(id, convertToUser(userDTO));
        return ResponseEntity.ok(userDTO);
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user , UserDTO.class);
    }

    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

}
