package ru.danil.springtest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.dto.UserDTO;
import ru.danil.springtest.mapper.UserMapper;
import ru.danil.springtest.model.User;
import ru.danil.springtest.repository.UserRepository;
import ru.danil.springtest.exeption.ObjectNotFound;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDTO getUsernameById(UUID id){
        User user = userRepository.findByIdWithOrders(id).orElseThrow(() -> {
            log.error("Пользователь с таким айди не найде {}", id);
            return new ObjectNotFound("Пользователь с таким id не найден " + id);
        });
        log.debug("Найден User: id={}, username={}, createdAt={}, updatedAt={}, isDeleted={}, ordersCount={}",
                user.getId(), user.getUsername(), user.getCreatedAt(),
                user.getUpdatedAt(), user.getIsDeleted(),
                user.getOrders() != null ? user.getOrders().size() : 0);
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userMapper.toUser(getUsernameById(id));
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO){
        return userMapper.toUserDTO(userRepository.save(userMapper.toUser(userDTO)));
    }

    @Transactional
    public UserDTO updateUser(UUID id, UserDTO updatedUserDTO) {
        User user = userMapper.toUser(getUsernameById(id));
        userMapper.updateUser(updatedUserDTO, user);
        return userMapper.toUserDTO(userRepository.save(user));
    }
}
