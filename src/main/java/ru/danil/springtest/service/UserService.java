package ru.danil.springtest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.User;
import ru.danil.springtest.repository.UserRepository;
import ru.danil.springtest.utill.ObjectNotFound;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUsernameById(UUID id){
        return userRepository.findByIdWithOrders(id).orElseThrow(() -> new ObjectNotFound("Пользователь с таким id не найден"));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = getUsernameById(id);
        userRepository.deleteById(id);
    }

    @Transactional
    public User createUser(User user){
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User updatedUser) {
        User user = getUsernameById(id);
        user.setUsername(updatedUser.getUsername());
        user.setOrders(updatedUser.getOrders());
        return userRepository.save(user);
    }
}
