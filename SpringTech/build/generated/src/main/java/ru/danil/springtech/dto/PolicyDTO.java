package ru.danil.springtech.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * PolicyDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-29T22:11:18.723315900+03:00[Europe/Moscow]")
public class PolicyDTO {

  private String policyNumber;

  private UUID personId;

  public PolicyDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PolicyDTO(String policyNumber, UUID personId) {
    this.policyNumber = policyNumber;
    this.personId = personId;
  }

  public PolicyDTO policyNumber(String policyNumber) {
    this.policyNumber = policyNumber;
    return this;
  }

  /**
   * Get policyNumber
   * @return policyNumber
  */
  @NotNull @Size(min = 6, max = 6) 
  @Schema(name = "policyNumber", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("policyNumber")
  public String getPolicyNumber() {
    return policyNumber;
  }

  public void setPolicyNumber(String policyNumber) {
    this.policyNumber = policyNumber;
  }

  public PolicyDTO personId(UUID personId) {
    this.personId = personId;
    return this;
  }

  /**
   * Get personId
   * @return personId
  */
  @NotNull @Valid 
  @Schema(name = "personId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("personId")
  public UUID getPersonId() {
    return personId;
  }

  public void setPersonId(UUID personId) {
    this.personId = personId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyDTO policyDTO = (PolicyDTO) o;
    return Objects.equals(this.policyNumber, policyDTO.policyNumber) &&
        Objects.equals(this.personId, policyDTO.personId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyNumber, personId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyDTO {\n");
    sb.append("    policyNumber: ").append(toIndentedString(policyNumber)).append("\n");
    sb.append("    personId: ").append(toIndentedString(personId)).append("\n");
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

