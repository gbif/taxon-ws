package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Error response")
public class ErrorMessage {

  @Schema(description = "HTTP status code", example = "404")
  private int code;

  @Schema(description = "Error message", example = "Not found")
  private String message;

  @Schema(description = "Additional error details")
  private String details;

  public ErrorMessage(int code, String message, String details) {
    this.code = code;
    this.message = message;
    this.details = details;
  }
}
