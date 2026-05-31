package ru.danil.springtech.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * MedicalPolicyDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-28T12:03:18.464502500+03:00[Europe/Moscow]")
public class MedicalPolicyDTO {

  private String poleNumber;

  public MedicalPolicyDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public MedicalPolicyDTO(String poleNumber) {
    this.poleNumber = poleNumber;
  }

  public MedicalPolicyDTO poleNumber(String poleNumber) {
    this.poleNumber = poleNumber;
    return this;
  }

  /**
   * Get poleNumber
   * @return poleNumber
  */
  @NotNull @Size(min = 6, max = 6) 
  @Schema(name = "poleNumber", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("poleNumber")
  public String getPoleNumber() {
    return poleNumber;
  }

  public void setPoleNumber(String poleNumber) {
    this.poleNumber = poleNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MedicalPolicyDTO medicalPolicyDTO = (MedicalPolicyDTO) o;
    return Objects.equals(this.poleNumber, medicalPolicyDTO.poleNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(poleNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MedicalPolicyDTO {\n");
    sb.append("    poleNumber: ").append(toIndentedString(poleNumber)).append("\n");
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

