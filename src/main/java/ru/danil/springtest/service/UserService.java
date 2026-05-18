package ru.danil.springtest.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.Order;
import ru.danil.springtest.model.User;
import ru.danil.springtest.repository.UserRepository;
import ru.danil.springtest.utill.UserExeption;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final OrderService orderService;

    public User getUsernameById(UUID id){
        return userRepository.findById(id).orElseThrow(() -> new UserExeption("Пользователь с таким id не найден", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public User createUser(User user) {
        user.linkOrders();
        return userRepository.save(user);
    }

    @Transactional
    public User userUpdate(UUID id, User updatedUser) {
        if(updatedUser.getOrders() != null) {
            Set<UUID> ids = new HashSet<>();
            for (Order updatedOrder : updatedUser.getOrders()){
                if(updatedOrder.getId() != null) {
                    ids.add(updatedOrder.getId());
                }
            }
            List<Order> ordersOfUser = orderService.findByIdIn(ids);
            if (ordersOfUser.size() != ids.size()) {
                throw new UserExeption("Неправльно ввели айди", HttpStatus.BAD_REQUEST);
            }
        }
        User user = getUsernameById(id);
        user.updateOrders(updatedUser.getOrders());
        user.setUsername(updatedUser.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = getUsernameById(id);
        userRepository.deleteById(id);
    }
}
