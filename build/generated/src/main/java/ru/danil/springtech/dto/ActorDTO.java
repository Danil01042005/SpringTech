package ru.danil.springtech.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import ru.danil.springtech.dto.MovieDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ActorDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-31T14:44:45.571404800+03:00[Europe/Moscow]")
public class ActorDTO {

  private UUID id;

  private String name;

  private Integer age;

  @Valid
  private List<@Valid MovieDTO> movies;

  private PolicyStatus policyStatus;

  private PolicyDTO policy;

  public ActorDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ActorDTO(String name, Integer age) {
    this.name = name;
    this.age = age;
  }

  public ActorDTO id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ActorDTO name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  @NotNull @Size(min = 4) 
  @Schema(name = "name", example = "Ivan Ivan Ivanovich", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ActorDTO age(Integer age) {
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

  public ActorDTO movies(List<@Valid MovieDTO> movies) {
    this.movies = movies;
    return this;
  }

  public ActorDTO addMoviesItem(MovieDTO moviesItem) {
    if (this.movies == null) {
      this.movies = new ArrayList<>();
    }
    this.movies.add(moviesItem);
    return this;
  }

  /**
   * Get movies
   * @return movies
  */
  @Valid 
  @Schema(name = "movies", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("movies")
  public List<@Valid MovieDTO> getMovies() {
    return movies;
  }

  public void setMovies(List<@Valid MovieDTO> movies) {
    this.movies = movies;
  }

  public ActorDTO policyStatus(PolicyStatus policyStatus) {
    this.policyStatus = policyStatus;
    return this;
  }

  /**
   * Get policyStatus
   * @return policyStatus
  */
  @Valid 
  @Schema(name = "policyStatus", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("policyStatus")
  public PolicyStatus getPolicyStatus() {
    return policyStatus;
  }

  public void setPolicyStatus(PolicyStatus policyStatus) {
    this.policyStatus = policyStatus;
  }

  public ActorDTO policy(PolicyDTO policy) {
    this.policy = policy;
    return this;
  }

  /**
   * Get policy
   * @return policy
  */
  @Valid 
  @Schema(name = "policy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("policy")
  public PolicyDTO getPolicy() {
    return policy;
  }

  public void setPolicy(PolicyDTO policy) {
    this.policy = policy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorDTO actorDTO = (ActorDTO) o;
    return Objects.equals(this.id, actorDTO.id) &&
        Objects.equals(this.name, actorDTO.name) &&
        Objects.equals(this.age, actorDTO.age) &&
        Objects.equals(this.movies, actorDTO.movies) &&
        Objects.equals(this.policyStatus, actorDTO.policyStatus) &&
        Objects.equals(this.policy, actorDTO.policy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, age, movies, policyStatus, policy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorDTO {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    age: ").append(toIndentedString(age)).append("\n");
    sb.append("    movies: ").append(toIndentedString(movies)).append("\n");
    sb.append("    policyStatus: ").append(toIndentedString(policyStatus)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
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

