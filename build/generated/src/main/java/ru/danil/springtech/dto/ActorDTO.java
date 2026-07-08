package ru.danil.springtech.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.danil.springtech.dto.MovieDTO;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ActorDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-08T18:26:47.140048+03:00[Europe/Moscow]")
public class ActorDTO {

  private String name;

  private Integer age;

  @Valid
  private List<@Valid MovieDTO> movies;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorDTO actorDTO = (ActorDTO) o;
    return Objects.equals(this.name, actorDTO.name) &&
        Objects.equals(this.age, actorDTO.age) &&
        Objects.equals(this.movies, actorDTO.movies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age, movies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorDTO {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    age: ").append(toIndentedString(age)).append("\n");
    sb.append("    movies: ").append(toIndentedString(movies)).append("\n");
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

