package org.gbif.taxon.registry;

import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Identifier;
import org.gbif.api.vocabulary.EndpointType;
import org.gbif.api.vocabulary.IdentifierType;
import org.gbif.taxon.config.ChecklistConfig;
import org.gbif.taxon.config.ColConfig;
import org.gbif.taxon.config.RegistryConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Keeps the GBIF registry entry for the Catalogue of Life in sync with the ChecklistBank release
 * the matching service actually uses.
 * <p>
 * It registers a ColDP endpoint pinned to that exact release, so the crawler picks up the right
 * archive and writes matching metadata without needing any COL specific handling of its own, and
 * it keeps the CLB_DATASET_KEY identifier pointing at the same release.
 * <p>
 * All operations are idempotent and add before they delete, so the dataset is never left without
 * an endpoint. Identifiers of any other type, in particular the DOIs written by the crawler, are
 * never touched.
 */
@Service
public class RegistrySync {
  private static final Logger LOG = LoggerFactory.getLogger(RegistrySync.class);
  private static final EndpointType ENDPOINT_TYPE = EndpointType.COLDP;
  private static final String ENDPOINT_DESCRIPTION = "ColDP archive of the COL release used by GBIF for interpretation";
  private static final String COLDP = "ColDP";

  private final ObjectProvider<RegistryDatasetClient> client;
  private final ColConfig colCfg;
  private final ChecklistConfig checklistCfg;
  private final RegistryConfig registryCfg;

  public RegistrySync(ObjectProvider<RegistryDatasetClient> client, ColConfig colCfg, ChecklistConfig checklistCfg,
                      RegistryConfig registryCfg) {
    this.client = client;
    this.colCfg = colCfg;
    this.checklistCfg = checklistCfg;
    this.registryCfg = registryCfg;
  }

  /**
   * Points the COL dataset in the GBIF registry at the given ChecklistBank release.
   * Does nothing if registry access is not configured.
   *
   * @param clbKey the ChecklistBank dataset key currently used by the matching service
   * @return true if anything was changed in the registry
   */
  public boolean syncCol(int clbKey) {
    RegistryDatasetClient c = client.getIfAvailable();
    if (c == null) {
      if (registryCfg.isMisconfigured()) {
        LOG.warn("Registry syncing is enabled but no app key is configured, not syncing COL key {}", clbKey);
      } else {
        LOG.debug("Registry access is disabled, not syncing COL key {}", clbKey);
      }
      return false;
    }
    UUID datasetKey = colCfg.getExtendedRelease();
    URI url = checklistCfg.downloadUrl(clbKey, COLDP);
    boolean changed = syncEndpoint(c, datasetKey, url);
    changed |= syncIdentifier(c, datasetKey, clbKey);
    if (changed) {
      LOG.info("Updated GBIF registry dataset {} to CLB dataset {}", datasetKey, clbKey);
    } else {
      LOG.debug("GBIF registry dataset {} already points at CLB dataset {}", datasetKey, clbKey);
    }
    return changed;
  }

  private boolean syncEndpoint(RegistryDatasetClient c, UUID datasetKey, URI url) {
    List<Endpoint> existing = c.listEndpoints(datasetKey);
    if (existing.stream().anyMatch(e -> ENDPOINT_TYPE == e.getType() && url.equals(e.getUrl()))) {
      return false;
    }
    Endpoint endpoint = new Endpoint();
    endpoint.setType(ENDPOINT_TYPE);
    endpoint.setUrl(url);
    endpoint.setDescription(ENDPOINT_DESCRIPTION);
    c.addEndpoint(datasetKey, endpoint);
    LOG.info("Added {} endpoint {} to GBIF dataset {}", ENDPOINT_TYPE, url, datasetKey);
    // only remove the outdated ones once the new endpoint exists
    for (Endpoint e : existing) {
      c.deleteEndpoint(datasetKey, e.getKey());
      LOG.info("Removed outdated {} endpoint {} from GBIF dataset {}", e.getType(), e.getUrl(), datasetKey);
    }
    return true;
  }

  private boolean syncIdentifier(RegistryDatasetClient c, UUID datasetKey, int clbKey) {
    String value = String.valueOf(clbKey);
    List<Identifier> existing = c.listIdentifiers(datasetKey).stream()
      .filter(i -> IdentifierType.CLB_DATASET_KEY == i.getType())
      .toList();
    if (existing.stream().anyMatch(i -> value.equals(i.getIdentifier()))) {
      return false;
    }
    c.addIdentifier(datasetKey, new Identifier(IdentifierType.CLB_DATASET_KEY, value));
    LOG.info("Added {} identifier {} to GBIF dataset {}", IdentifierType.CLB_DATASET_KEY, value, datasetKey);
    // only remove the outdated ones once the new identifier exists. Other types, e.g. DOIs, are left alone
    for (Identifier i : existing) {
      c.deleteIdentifier(datasetKey, i.getKey());
      LOG.info("Removed outdated {} identifier {} from GBIF dataset {}", i.getType(), i.getIdentifier(), datasetKey);
    }
    return true;
  }
}
