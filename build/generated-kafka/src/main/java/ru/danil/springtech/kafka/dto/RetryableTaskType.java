package ru.danil.springtech.kafka.dto;

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
 * Gets or Sets RetryableTaskType
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-23T11:46:01.451727700+03:00[Europe/Moscow]")
public enum RetryableTaskType {
  
  CREATED_MEDICINE_POLICY("CREATED_MEDICINE_POLICY");

  private String value;

  RetryableTaskType(String value) {
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
  public static RetryableTaskType fromValue(String value) {
    for (RetryableTaskType b : RetryableTaskType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

