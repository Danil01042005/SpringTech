package ru.danil.springtest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SQLDelete(sql = "UPDATE test.users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "users", schema = "test")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    public void linkOrders(){
            this.orders.forEach(order -> order.setOwner(this));
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders.removeIf(thisOrder -> newOrders.stream()
                .filter(newOrder -> newOrder.getId() != null)
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
