package org.gbif.taxon.registry;

import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Identifier;
import org.gbif.api.vocabulary.EndpointType;
import org.gbif.api.vocabulary.IdentifierType;
import org.gbif.taxon.config.ChecklistConfig;
import org.gbif.taxon.config.ColConfig;
import org.gbif.taxon.config.RegistryConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrySyncTest {

  private static final int CLB_KEY = 315557;
  private static final URI COLDP_URL =
    URI.create("https://api.checklistbank.org/dataset/315557/export.zip?format=ColDP&extended=true");

  @Mock
  private RegistryDatasetClient client;

  @Mock
  private ObjectProvider<RegistryDatasetClient> provider;

  private UUID col;
  private RegistrySync sync;

  @BeforeEach
  void setUp() {
    var colCfg = new ColConfig();
    col = colCfg.getExtendedRelease();
    when(provider.getIfAvailable()).thenReturn(client);
    sync = new RegistrySync(provider, colCfg, new ChecklistConfig(), new RegistryConfig());
  }

  private static Endpoint endpoint(int key, EndpointType type, String url) {
    var e = new Endpoint();
    e.setKey(key);
    e.setType(type);
    e.setUrl(URI.create(url));
    return e;
  }

  private static Identifier identifier(int key, IdentifierType type, String value) {
    var i = new Identifier(type, value);
    i.setKey(key);
    return i;
  }

  @Test
  void disabledRegistryIsANoOp() {
    when(provider.getIfAvailable()).thenReturn(null);

    assertThat(sync.syncCol(CLB_KEY)).isFalse();
    verify(client, never()).listEndpoints(any());
    verify(client, never()).listIdentifiers(any());
  }

  @Test
  void alreadyCorrectWritesNothing() {
    when(client.listEndpoints(col)).thenReturn(List.of(endpoint(1, EndpointType.COLDP, COLDP_URL.toString())));
    when(client.listIdentifiers(col))
      .thenReturn(List.of(identifier(2, IdentifierType.CLB_DATASET_KEY, String.valueOf(CLB_KEY))));

    assertThat(sync.syncCol(CLB_KEY)).isFalse();

    verify(client, never()).addEndpoint(any(), any());
    verify(client, never()).deleteEndpoint(any(), org.mockito.ArgumentMatchers.anyInt());
    verify(client, never()).addIdentifier(any(), any());
    verify(client, never()).deleteIdentifier(any(), org.mockito.ArgumentMatchers.anyInt());
  }

  @Test
  void outdatedEndpointIsReplacedAfterTheNewOneIsAdded() {
    var old = endpoint(11, EndpointType.DWC_ARCHIVE, "https://download.checklistbank.org/col/xr_latest_dwca.zip");
    when(client.listEndpoints(col)).thenReturn(List.of(old));
    when(client.listIdentifiers(col))
      .thenReturn(List.of(identifier(2, IdentifierType.CLB_DATASET_KEY, String.valueOf(CLB_KEY))));

    assertThat(sync.syncCol(CLB_KEY)).isTrue();

    var added = org.mockito.ArgumentCaptor.forClass(Endpoint.class);
    InOrder order = inOrder(client);
    order.verify(client).addEndpoint(eq(col), added.capture());
    order.verify(client).deleteEndpoint(col, 11);
    assertThat(added.getValue().getType()).isEqualTo(EndpointType.COLDP);
    assertThat(added.getValue().getUrl()).isEqualTo(COLDP_URL);
  }

  @Test
  void outdatedClbKeyIsReplacedAndOtherIdentifierTypesSurvive() {
    when(client.listEndpoints(col)).thenReturn(List.of(endpoint(1, EndpointType.COLDP, COLDP_URL.toString())));
    when(client.listIdentifiers(col)).thenReturn(List.of(
      identifier(21, IdentifierType.CLB_DATASET_KEY, "314396"),
      identifier(22, IdentifierType.DOI, "10.48580/dgy8b")
    ));

    assertThat(sync.syncCol(CLB_KEY)).isTrue();

    var added = org.mockito.ArgumentCaptor.forClass(Identifier.class);
    InOrder order = inOrder(client);
    order.verify(client).addIdentifier(eq(col), added.capture());
    order.verify(client).deleteIdentifier(col, 21);
    assertThat(added.getValue().getType()).isEqualTo(IdentifierType.CLB_DATASET_KEY);
    assertThat(added.getValue().getIdentifier()).isEqualTo("315557");
    // the DOI identifier written by the crawler must never be removed
    verify(client, never()).deleteIdentifier(col, 22);
  }
}
