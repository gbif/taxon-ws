package org.gbif.taxon.resource;

import org.gbif.taxon.dao.TaxonDao;
import org.gbif.taxon.mapper.ExceptionMapper;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Clients asking for a representation we don't produce must get a 406, not a 500.
 */
class NotAcceptableTest {

  private final MockMvc mvc = MockMvcBuilders
    .standaloneSetup(new TaxonResource(mock(TaxonDao.class)))
    .setControllerAdvice(new ExceptionMapper())
    .build();

  @Test
  void suggestWithUnsupportedAcceptHeader() throws Exception {
    mvc.perform(get("/taxon/suggest/2d59e5db-57ad-41ff-97d6-11f5fb264527").header("Accept", "text/html"))
      .andExpect(status().isNotAcceptable());
  }
}
