package ru.danil.springtest.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.User;
import ru.danil.springtest.repository.UserRepository;
import ru.danil.springtest.utill.UserExeption;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getUsernameById(UUID id){
        return userRepository.findById(id).orElseThrow(() -> new UserExeption("Пользователь с таким id не найден", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public User createNewUser(User user) {
        user.addOwnerForOrders();
        return userRepository.save(user);
    }

    @Transactional
    public void userUpdate(UUID id, User updatedUser) {
        User user = getUsernameById(id);
        user.setUsername(updatedUser.getUsername());
        user.updateOrders(updatedUser.getOrders());
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = getUsernameById(id);
        userRepository.deleteById(id);
    }
}
