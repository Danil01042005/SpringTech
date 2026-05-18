package ru.danil.springtest.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.Order;
import ru.danil.springtest.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> findByIdIn(Iterable<UUID> ids) {
        return orderRepository.findByidIn(ids);
    }
}
