package org.gbif.taxon.dao;

import org.gbif.taxon.registry.RegistrySync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps the COL XR key in sync with the matching service, which is authoritative for the
 * ChecklistBank release GBIF uses to interpret occurrences.
 * Without this the key would only be read once at startup and a matching service deployment
 * would require a manual cache flush.
 * The GBIF registry is synced on every run, not just when the key changes, so that drift
 * introduced elsewhere is corrected too. Syncing is idempotent and writes nothing when the
 * registry is already correct.
 */
@Component
public class ColKeyRefresher {
  private static final Logger LOG = LoggerFactory.getLogger(ColKeyRefresher.class);

  private final DatasetKeyMap keyMap;
  private final ChecklistDao checklistDao;
  private final TaxonDao taxonDao;
  private final RegistrySync registrySync;

  public ColKeyRefresher(DatasetKeyMap keyMap, ChecklistDao checklistDao, TaxonDao taxonDao, RegistrySync registrySync) {
    this.keyMap = keyMap;
    this.checklistDao = checklistDao;
    this.taxonDao = taxonDao;
    this.registrySync = registrySync;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    refresh();
  }

  @Scheduled(fixedDelayString = "${col.refresh-interval:PT1H}", initialDelayString = "${col.refresh-interval:PT1H}")
  public void refresh() {
    try {
      if (keyMap.refreshColKey()) {
        checklistDao.flushCache();
        taxonDao.flushCache();
      }
      registrySync.syncCol(keyMap.getColKey());
    } catch (Exception e) {
      // never let a matching service or registry outage take down the scheduler, we keep the previous key
      LOG.warn("Failed to refresh the COL XR key, keeping {}", keyMap.getColKey(), e);
    }
  }
}
