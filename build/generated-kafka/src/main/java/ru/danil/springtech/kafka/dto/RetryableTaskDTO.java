package ru.danil.springtech.kafka.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * RetryableTaskDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:37:01.505470200+03:00[Europe/Moscow]")
public class RetryableTaskDTO {

  private UUID id;

  private RetryableTaskType type;

  private String payload;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime retryTime;

  private Integer attempts;

  private UUID leaseToken;

  public RetryableTaskDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public RetryableTaskDTO(RetryableTaskType type, String payload) {
    this.type = type;
    this.payload = payload;
  }

  public RetryableTaskDTO id(UUID id) {
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

  public RetryableTaskDTO type(RetryableTaskType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  @NotNull @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public RetryableTaskType getType() {
    return type;
  }

  public void setType(RetryableTaskType type) {
    this.type = type;
  }

  public RetryableTaskDTO payload(String payload) {
    this.payload = payload;
    return this;
  }

  /**
   * JSON-строка с PolicyDTO
   * @return payload
  */
  @NotNull 
  @Schema(name = "payload", description = "JSON-строка с PolicyDTO", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("payload")
  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public RetryableTaskDTO retryTime(OffsetDateTime retryTime) {
    this.retryTime = retryTime;
    return this;
  }

  /**
   * Get retryTime
   * @return retryTime
  */
  @Valid 
  @Schema(name = "retryTime", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("retryTime")
  public OffsetDateTime getRetryTime() {
    return retryTime;
  }

  public void setRetryTime(OffsetDateTime retryTime) {
    this.retryTime = retryTime;
  }

  public RetryableTaskDTO attempts(Integer attempts) {
    this.attempts = attempts;
    return this;
  }

  /**
   * Get attempts
   * @return attempts
  */
  
  @Schema(name = "attempts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("attempts")
  public Integer getAttempts() {
    return attempts;
  }

  public void setAttempts(Integer attempts) {
    this.attempts = attempts;
  }

  public RetryableTaskDTO leaseToken(UUID leaseToken) {
    this.leaseToken = leaseToken;
    return this;
  }

  /**
   * Get leaseToken
   * @return leaseToken
  */
  @Valid 
  @Schema(name = "leaseToken", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("leaseToken")
  public UUID getLeaseToken() {
    return leaseToken;
  }

  public void setLeaseToken(UUID leaseToken) {
    this.leaseToken = leaseToken;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RetryableTaskDTO retryableTaskDTO = (RetryableTaskDTO) o;
    return Objects.equals(this.id, retryableTaskDTO.id) &&
        Objects.equals(this.type, retryableTaskDTO.type) &&
        Objects.equals(this.payload, retryableTaskDTO.payload) &&
        Objects.equals(this.retryTime, retryableTaskDTO.retryTime) &&
        Objects.equals(this.attempts, retryableTaskDTO.attempts) &&
        Objects.equals(this.leaseToken, retryableTaskDTO.leaseToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, payload, retryTime, attempts, leaseToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RetryableTaskDTO {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("    retryTime: ").append(toIndentedString(retryTime)).append("\n");
    sb.append("    attempts: ").append(toIndentedString(attempts)).append("\n");
    sb.append("    leaseToken: ").append(toIndentedString(leaseToken)).append("\n");
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

