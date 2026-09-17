package org.gbif.taxon.resource;

import org.gbif.taxon.dao.TaxonDao;
import org.gbif.taxon.mapper.ExceptionMapper;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * strict=true on the related resource must be restricted to exactly one target dataset to keep it predictable.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RelatedStrictTest {

  private static final String KEY = "2d59e5db-57ad-41ff-97d6-11f5fb264527";
  private static final String COL = "7ddf754f-d193-4cc9-b351-99906754a03b";
  private static final String WORMS = "2d59e5db-57ad-41ff-97d6-11f5fb264528";
  private static final String URL = "/taxon/" + KEY + "/CXA/related";

  @Mock
  private TaxonDao taxonDao;

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    when(taxonDao.listRelatedStrict(any(), anyString(), any())).thenReturn(List.of());
    when(taxonDao.listRelated(any(), anyString(), any(), any(), any())).thenReturn(List.of());
    mvc = MockMvcBuilders
      .standaloneSetup(new TaxonResource(taxonDao))
      .setControllerAdvice(new ExceptionMapper())
      .build();
  }

  @Test
  void strictWithSingleDatasetKey() throws Exception {
    mvc.perform(get(URL).param("strict", "true").param("datasetKey", COL))
      .andExpect(status().isOk());
    verify(taxonDao).listRelatedStrict(UUID.fromString(KEY), "CXA", UUID.fromString(COL));
  }

  @Test
  void strictWithoutDatasetKey() throws Exception {
    mvc.perform(get(URL).param("strict", "true"))
      .andExpect(status().isBadRequest());
    verify(taxonDao, never()).listRelatedStrict(any(), anyString(), any());
  }

  @Test
  void strictWithMultipleDatasetKeys() throws Exception {
    mvc.perform(get(URL).param("strict", "true").param("datasetKey", COL, WORMS))
      .andExpect(status().isBadRequest());
    verify(taxonDao, never()).listRelatedStrict(any(), anyString(), any());
  }

  @Test
  void strictWithOtherFilters() throws Exception {
    mvc.perform(get(URL).param("strict", "true").param("datasetKey", COL).param("publisherKey", WORMS))
      .andExpect(status().isBadRequest());
    verify(taxonDao, never()).listRelatedStrict(any(), anyString(), any());
  }

  @Test
  void nonStrictUnchanged() throws Exception {
    mvc.perform(get(URL).param("datasetKey", COL, WORMS))
      .andExpect(status().isOk());
    verify(taxonDao).listRelated(eq(UUID.fromString(KEY)), eq("CXA"), any(), any(), any());
    verify(taxonDao, never()).listRelatedStrict(any(), anyString(), any());
  }
}
