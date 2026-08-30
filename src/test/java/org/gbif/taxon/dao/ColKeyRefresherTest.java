package org.gbif.taxon.dao;

import org.gbif.taxon.registry.RegistrySync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ColKeyRefresherTest {

  @Mock
  private DatasetKeyMap keyMap;

  @Mock
  private ChecklistDao checklistDao;

  @Mock
  private TaxonDao taxonDao;

  @Mock
  private RegistrySync registrySync;

  private ColKeyRefresher refresher;

  @BeforeEach
  void setUp() {
    when(keyMap.getColKey()).thenReturn(315557);
    refresher = new ColKeyRefresher(keyMap, checklistDao, taxonDao, registrySync);
  }

  @Test
  void unchangedKeyStillSyncsTheRegistryButKeepsCaches() {
    when(keyMap.refreshColKey()).thenReturn(false);

    refresher.refresh();

    verify(checklistDao, never()).flushCache();
    verify(taxonDao, never()).flushCache();
    verify(registrySync).syncCol(315557);
  }

  @Test
  void changedKeyFlushesCachesAndSyncs() {
    when(keyMap.refreshColKey()).thenReturn(true);

    refresher.refresh();

    verify(checklistDao).flushCache();
    verify(taxonDao).flushCache();
    verify(registrySync).syncCol(315557);
  }

  @Test
  void matchingServiceOutageIsSwallowed() {
    when(keyMap.refreshColKey()).thenThrow(new IllegalStateException("matcher is down"));

    refresher.refresh();

    verify(registrySync, never()).syncCol(org.mockito.ArgumentMatchers.anyInt());
  }

  @Test
  void registryOutageIsSwallowed() {
    when(keyMap.refreshColKey()).thenReturn(true);
    when(registrySync.syncCol(315557)).thenThrow(new IllegalStateException("registry is down"));

    refresher.refresh();

    verify(checklistDao).flushCache();
  }
}
