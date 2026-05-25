package ru.danil.springtest.dto;

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
 * PassportDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-25T12:04:16.409963300+03:00[Europe/Moscow]")
public class PassportDTO {

  private String passportNumber;

  public PassportDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PassportDTO(String passportNumber) {
    this.passportNumber = passportNumber;
  }

  public PassportDTO passportNumber(String passportNumber) {
    this.passportNumber = passportNumber;
    return this;
  }

  /**
   * Get passportNumber
   * @return passportNumber
  */
  @NotNull @Size(min = 6, max = 6) 
  @Schema(name = "passportNumber", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("passportNumber")
  public String getPassportNumber() {
    return passportNumber;
  }

  public void setPassportNumber(String passportNumber) {
    this.passportNumber = passportNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PassportDTO passportDTO = (PassportDTO) o;
    return Objects.equals(this.passportNumber, passportDTO.passportNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(passportNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PassportDTO {\n");
    sb.append("    passportNumber: ").append(toIndentedString(passportNumber)).append("\n");
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

