package ru.danil.springtest.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.springtest.dto.OrderDTO;
import ru.danil.springtest.dto.UserDTO;
import ru.danil.springtest.model.Order;
import ru.danil.springtest.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T14:37:10+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toUserDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setUsername( user.getUsername() );
        userDTO.setOrders( orderListToOrderDTOList( user.getOrders() ) );

        return userDTO;
    }

    @Override
    public User toUser(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User user = new User();

        user.setOrders( orderDTOListToOrderList( userDTO.getOrders() ) );
        user.setUsername( userDTO.getUsername() );

        return user;
    }

    protected OrderDTO orderToOrderDTO(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDTO orderDTO = new OrderDTO();

        orderDTO.setOrderDetails( order.getOrderDetails() );

        return orderDTO;
    }

    protected List<OrderDTO> orderListToOrderDTOList(List<Order> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderDTO> list1 = new ArrayList<OrderDTO>( list.size() );
        for ( Order order : list ) {
            list1.add( orderToOrderDTO( order ) );
        }

        return list1;
    }

    protected Order orderDTOToOrder(OrderDTO orderDTO) {
        if ( orderDTO == null ) {
            return null;
        }

        Order order = new Order();

        order.setOrderDetails( orderDTO.getOrderDetails() );

        return order;
    }

    protected List<Order> orderDTOListToOrderList(List<OrderDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Order> list1 = new ArrayList<Order>( list.size() );
        for ( OrderDTO orderDTO : list ) {
            list1.add( orderDTOToOrder( orderDTO ) );
        }

        return list1;
    }
}
