package ru.danil.springtest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users", schema = "test")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Column(name = "username")
    private String username;

    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Order> orders;

    public void updateOrders(List<Order> newOrders) {
        this.orders.removeIf(thisOrder -> newOrders.stream()
                .filter(n -> n.getId() != null)
                .noneMatch(newOrder -> newOrder.getId().equals(thisOrder.getId())));

        for(Order newOrder : newOrders){
            if(newOrder.getId() == null) {
                newOrder.setOwner(this);
                this.orders.add(newOrder);
                continue;
            }
            Optional<Order> thisOrder = this.orders.stream()
                    .filter(o -> o.getId().equals(newOrder.getId())).findFirst();

            if (thisOrder.isPresent()) {
                Order orderToUpdate = thisOrder.get();
                orderToUpdate.setOrderDetails(newOrder.getOrderDetails());
            } else {
                newOrder.setOwner(this);
                this.orders.add(newOrder);
            }
        }
    }
}
