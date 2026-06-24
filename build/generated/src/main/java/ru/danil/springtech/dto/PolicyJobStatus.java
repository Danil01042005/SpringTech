package ru.danil.springtech.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets PolicyJobStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-06-24T17:56:52.707955700+03:00[Europe/Moscow]")
public enum PolicyJobStatus {
  
  NUMBER_200(200),
  
  NUMBER_403(403),
  
  NUMBER_503(503);

  private Integer value;

  PolicyJobStatus(Integer value) {
    this.value = value;
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static PolicyJobStatus fromValue(Integer value) {
    for (PolicyJobStatus b : PolicyJobStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

