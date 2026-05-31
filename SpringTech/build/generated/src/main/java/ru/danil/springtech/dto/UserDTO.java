package ru.danil.springtech.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * UserDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-29T22:11:18.723315900+03:00[Europe/Moscow]")
public class UserDTO {

  private String username;

  @Valid
  private List<@Valid OrderDTO> orders = new ArrayList<>();

  public UserDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public UserDTO(String username, List<@Valid OrderDTO> orders) {
    this.username = username;
    this.orders = orders;
  }

  public UserDTO username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get username
   * @return username
  */
  @NotNull @Size(min = 2, max = 30) 
  @Schema(name = "username", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserDTO orders(List<@Valid OrderDTO> orders) {
    this.orders = orders;
    return this;
  }

  public UserDTO addOrdersItem(OrderDTO ordersItem) {
    if (this.orders == null) {
      this.orders = new ArrayList<>();
    }
    this.orders.add(ordersItem);
    return this;
  }

  /**
   * Get orders
   * @return orders
  */
  @NotNull @Valid 
  @Schema(name = "orders", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("orders")
  public List<@Valid OrderDTO> getOrders() {
    return orders;
  }

  public void setOrders(List<@Valid OrderDTO> orders) {
    this.orders = orders;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserDTO userDTO = (UserDTO) o;
    return Objects.equals(this.username, userDTO.username) &&
        Objects.equals(this.orders, userDTO.orders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, orders);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserDTO {\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    orders: ").append(toIndentedString(orders)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

