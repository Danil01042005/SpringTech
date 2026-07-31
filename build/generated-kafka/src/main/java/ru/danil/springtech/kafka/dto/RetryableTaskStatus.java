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
 * Gets or Sets RetryableTaskStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-30T13:31:20.241753100+03:00[Europe/Moscow]")
public enum RetryableTaskStatus {
  
  SEND_TO_KAFKA("SEND_TO_KAFKA"),
  
  PENDING("PENDING"),
  
  SUCCESS("SUCCESS"),
  
  DELETE_ACTOR_COMPENSATED("DELETE_ACTOR_COMPENSATED");

  private String value;

  RetryableTaskStatus(String value) {
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
  public static RetryableTaskStatus fromValue(String value) {
    for (RetryableTaskStatus b : RetryableTaskStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

