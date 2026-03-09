/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.taxon.api.search;

import org.gbif.api.model.common.search.BaseSearchRequest;

/**
 * A name usage specific suggest request.
 */
public class NameUsageSuggestRequest extends BaseSearchRequest<NameUsageSearchParameter> {

  @Override
  public void setHighlight(boolean highlight) {
    throw new UnsupportedOperationException("Highlight parameter is not supported in taxon operations.");
  }

  @Override
  public void setSpellCheck(boolean spellCheck) {
    throw new UnsupportedOperationException("Spell check parameter is supported in taxon operations.");
  }

  @Override
  public void setSpellCheckCount(int spellCheckCount) {
    // the request provider sets this to -1 by default, so we need to allow for that
    if (spellCheckCount != -1) {
      throw new UnsupportedOperationException("Spell check count parameter is supported in taxon operations.");
    }
  }

  @Override
  public void setMatchCase(Boolean matchCase) {
    throw new UnsupportedOperationException("Match case parameter is supported in taxon operations.");
  }

  @Override
  public void setShuffle(String shuffle) {
    throw new UnsupportedOperationException("Shuffle parameter is supported in taxon operations.");
  }
}
