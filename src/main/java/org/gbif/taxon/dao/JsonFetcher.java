package org.gbif.taxon.dao;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class JsonFetcher {

  private final RestClient restClient;

  public JsonFetcher(RestClient.Builder builder) {
    this.restClient = builder.build();
  }

  public JsonNode fetchJson(String url) {
    return restClient.get()
      .uri(url)
      .retrieve()
      .body(JsonNode.class);
  }

  public <T> T fetchJson(String url, Class<T> type) {
    return restClient.get()
      .uri(url)
      .retrieve()
      .body(type);
  }
}
