package org.gbif.taxon.registry;

import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Identifier;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

/**
 * A minimal feign client for the endpoint and identifier sub resources of the GBIF registry dataset API.
 * We deliberately do not depend on the full registry-ws-client, which expects a newer gbif-api than the
 * one this project is pinned to. Built via {@link org.gbif.ws.client.ClientBuilder} in
 * {@link org.gbif.taxon.config.RegistryConfig}, which supplies GBIF app key authentication.
 */
@RequestMapping("dataset")
public interface RegistryDatasetClient {

  @RequestMapping(method = RequestMethod.GET, value = "{key}/endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  List<Endpoint> listEndpoints(@PathVariable("key") UUID key);

  @RequestMapping(method = RequestMethod.POST, value = "{key}/endpoint", consumes = MediaType.APPLICATION_JSON_VALUE)
  int addEndpoint(@PathVariable("key") UUID key, @RequestBody Endpoint endpoint);

  @RequestMapping(method = RequestMethod.DELETE, value = "{key}/endpoint/{endpointKey}")
  void deleteEndpoint(@PathVariable("key") UUID key, @PathVariable("endpointKey") int endpointKey);

  @RequestMapping(method = RequestMethod.GET, value = "{key}/identifier", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  List<Identifier> listIdentifiers(@PathVariable("key") UUID key);

  @RequestMapping(method = RequestMethod.POST, value = "{key}/identifier", consumes = MediaType.APPLICATION_JSON_VALUE)
  int addIdentifier(@PathVariable("key") UUID key, @RequestBody Identifier identifier);

  @RequestMapping(method = RequestMethod.DELETE, value = "{key}/identifier/{identifierKey}")
  void deleteIdentifier(@PathVariable("key") UUID key, @PathVariable("identifierKey") int identifierKey);
}
