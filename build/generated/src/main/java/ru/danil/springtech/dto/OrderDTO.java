package ru.danil.springtech.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * OrderDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-08T18:26:47.140048+03:00[Europe/Moscow]")
public class OrderDTO {

  private String orderDetails;

  public OrderDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OrderDTO(String orderDetails) {
    this.orderDetails = orderDetails;
  }

  public OrderDTO orderDetails(String orderDetails) {
    this.orderDetails = orderDetails;
    return this;
  }

  /**
   * Get orderDetails
   * @return orderDetails
  */
  @NotNull @Size(min = 2, max = 30) 
  @Schema(name = "orderDetails", example = "Вишня", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("orderDetails")
  public String getOrderDetails() {
    return orderDetails;
  }

  public void setOrderDetails(String orderDetails) {
    this.orderDetails = orderDetails;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderDTO orderDTO = (OrderDTO) o;
    return Objects.equals(this.orderDetails, orderDTO.orderDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrderDTO {\n");
    sb.append("    orderDetails: ").append(toIndentedString(orderDetails)).append("\n");
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

