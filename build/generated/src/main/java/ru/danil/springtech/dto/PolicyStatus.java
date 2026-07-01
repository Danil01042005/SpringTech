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
 * Gets or Sets PolicyStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-01T15:48:20.452899200+03:00[Europe/Moscow]")
public enum PolicyStatus {
  
  PENDING("PENDING"),
  
  COMPLETED("COMPLETED"),
  
  FAILED("FAILED"),
  
  COMPENSATING("COMPENSATING");

  private String value;

  PolicyStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static PolicyStatus fromValue(String value) {
    for (PolicyStatus b : PolicyStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

