package org.gbif.taxon.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.net.URI;

/**
 * Media (images, videos, sounds, etc.) associated with a taxon.
 * Based on the GBIF Simple Multimedia extension: https://rs.gbif.org/extension/gbif/1.0/multimedia.xml
 */
@Data
@Schema(description = "Media item (image, video, sound, etc.) associated with a taxon, based on the GBIF Simple Multimedia extension")
public class Media {

  @Schema(description = "A public URL that is a direct link to the media file itself, not an HTML page (dc:identifier). " +
    "For images this should be a URL to the image file, not a webpage displaying it.",
    example = "https://example.com/images/abies-alba.jpg")
  private String identifier;

  @Schema(description = "The Dublin Core type of the media object. " +
    "Recommended values: StillImage, MovingImage, Sound, InteractiveResource, Text.",
    example = "StillImage")
  private String type;

  @Schema(description = "The title or caption for the media item (dc:title)", example = "Abies alba cone and needles")
  private String title;

  @Schema(description = "The date and time the media item was created (dc:created). " +
    "ISO 8601 format recommended.",
    example = "2020-06-15")
  private String created;

  @Schema(description = "The person or organisation that created the media item (dc:creator)", example = "John Smith")
  private String creator;

  @Schema(description = "The license or rights statement for the media object (dc:license). " +
    "Preferably a Creative Commons URI or SPDX identifier.",
    example = "http://creativecommons.org/licenses/by/4.0/")
  private String license;

  @Schema(description = "A URL for a webpage that shows the media item and associated metadata (dc:references)",
    example = "https://example.com/media/details/12345")
  private URI references;

  @Schema(description = "A bibliographic citation for the original source of the media item (dc:source)",
    example = "iNaturalist observation #67890")
  private String source;

  @Schema(description = "Additional remarks or notes about this media item",
    example = "Specimen photographed in natural habitat")
  private String remarks;
}
