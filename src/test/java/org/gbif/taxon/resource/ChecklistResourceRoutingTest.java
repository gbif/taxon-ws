package org.gbif.taxon.resource;

import org.gbif.taxon.api.Checklist;
import org.gbif.taxon.api.ChecklistMetrics;
import org.gbif.taxon.api.NameUsage;
import org.gbif.taxon.dao.ChecklistDao;
import org.gbif.taxon.dao.DatasetKeyMap;
import org.gbif.taxon.dao.TaxonDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The checklist paths sit underneath the taxon prefix, where taxon/{datasetKey}/{taxonKey} could in
 * principle swallow them. These tests pin down that the literal checklist segment wins.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChecklistResourceRoutingTest {

  private static final String KEY = "2d59e5db-57ad-41ff-97d6-11f5fb264527";

  @Mock
  private TaxonDao taxonDao;

  @Mock
  private ChecklistDao checklistDao;

  @Mock
  private DatasetKeyMap keyMap;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    when(checklistDao.list()).thenReturn(List.of());
    when(checklistDao.get(any())).thenReturn(new Checklist());
    when(taxonDao.metrics(any())).thenReturn(new ChecklistMetrics());
    when(taxonDao.get(any(), any())).thenReturn(new NameUsage());
    mvc = MockMvcBuilders
      .standaloneSetup(new ChecklistResource(taxonDao, checklistDao, keyMap),
                       new DatasetResource(taxonDao),
                       new TaxonResource(taxonDao))
      .build();
  }

  @Test
  void listChecklists() throws Exception {
    mvc.perform(get("/taxon/checklist")).andExpect(status().isOk());
    verify(checklistDao).list();
  }

  @Test
  void singleChecklistBeatsTheTaxonUsagePattern() throws Exception {
    mvc.perform(get("/taxon/checklist/" + KEY)).andExpect(status().isOk());

    verify(checklistDao).get(UUID.fromString(KEY));
    // taxon/{datasetKey}/{taxonKey} must not have matched with datasetKey=checklist
    verify(taxonDao, never()).get(any(), any());
  }

  @Test
  void checklistMetrics() throws Exception {
    mvc.perform(get("/taxon/checklist/" + KEY + "/metrics")).andExpect(status().isOk());
    verify(taxonDao).metrics(UUID.fromString(KEY));
  }

  @Test
  void deprecatedDatasetMetricsStillWorks() throws Exception {
    mvc.perform(get("/dataset/" + KEY + "/metrics")).andExpect(status().isOk());
    verify(taxonDao).metrics(UUID.fromString(KEY));
  }

  @Test
  void taxonUsagePathIsUnaffected() throws Exception {
    mvc.perform(get("/taxon/" + KEY + "/CXA")).andExpect(status().isOk());
    verify(taxonDao).get(eq(UUID.fromString(KEY)), eq("CXA"));
  }

  @Test
  void removedDatasetCollectionPathIsGone() throws Exception {
    mvc.perform(get("/dataset")).andExpect(status().isNotFound());
    mvc.perform(get("/dataset/" + KEY)).andExpect(status().isNotFound());
  }
}
