package ru.danil.springtest.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import ru.danil.springtest.dto.PassportDTO;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PersonDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-25T12:04:16.409963300+03:00[Europe/Moscow]")
public class PersonDTO {

  private String fullName;

  private Integer age;

  private PassportDTO passport;

  public PersonDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public PersonDTO(String fullName, Integer age, PassportDTO passport) {
    this.fullName = fullName;
    this.age = age;
    this.passport = passport;
  }

  public PersonDTO fullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

  /**
   * Get fullName
   * @return fullName
  */
  @NotNull @Size(min = 4, max = 40) 
  @Schema(name = "fullName", example = "Иванов Иван Иванович", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("fullName")
  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public PersonDTO age(Integer age) {
    this.age = age;
    return this;
  }

  /**
   * Get age
   * minimum: 16
   * @return age
  */
  @NotNull @Min(16) 
  @Schema(name = "age", example = "16", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("age")
  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public PersonDTO passport(PassportDTO passport) {
    this.passport = passport;
    return this;
  }

  /**
   * Get passport
   * @return passport
  */
  @NotNull @Valid 
  @Schema(name = "passport", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("passport")
  public PassportDTO getPassport() {
    return passport;
  }

  public void setPassport(PassportDTO passport) {
    this.passport = passport;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PersonDTO personDTO = (PersonDTO) o;
    return Objects.equals(this.fullName, personDTO.fullName) &&
        Objects.equals(this.age, personDTO.age) &&
        Objects.equals(this.passport, personDTO.passport);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullName, age, passport);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PersonDTO {\n");
    sb.append("    fullName: ").append(toIndentedString(fullName)).append("\n");
    sb.append("    age: ").append(toIndentedString(age)).append("\n");
    sb.append("    passport: ").append(toIndentedString(passport)).append("\n");
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

