package org.gbif.species.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Media (images, videos, sounds, etc.) associated with a taxon.
 */
@Data
@Schema(description = "Media (images, videos, sounds, etc.) associated with a taxon")
public class Media {

  @Schema(description = "The URL identifier for the media resource",
    example = "https://example.com/images/abies-alba.jpg")
  private String identifier;

  @Schema(description = "The type of media (StillImage, MovingImage, Sound, etc.)", example = "StillImage")
  private String type;

  @Schema(description = "The title or caption of the media", example = "Abies alba cone and needles")
  private String title;

  @Schema(description = "The date the media was created", example = "2020-06-15")
  private String created;

  @Schema(description = "The creator or photographer of the media", example = "John Smith")
  private String creator;

  @Schema(description = "The license under which the media is provided", example = "CC BY 4.0")
  private String license;

  @Schema(description = "Link to references or source information",
    example = "https://example.com/media/details/12345")
  private String references;

  @Schema(description = "Source of the media", example = "iNaturalist")
  private String source;

  @Schema(description = "Additional remarks or notes about the media",
    example = "Specimen photographed in natural habitat")
  private String remarks;
}
