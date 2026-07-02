package org.gbif.taxon.dao;

import life.catalogue.api.vocab.IdentifierScope;
import life.catalogue.common.util.YamlUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Fat-jar-safe access to the CLB identifier scope registry.
 *
 * <p>CLB's own {@link life.catalogue.api.vocab.IdentifierScopes} loads the bundled registry via
 * {@code ClassLoader.getSystemResourceAsStream}, which returns {@code null} inside a Spring Boot
 * repackaged (fat) jar: dependency jars are nested under {@code BOOT-INF/lib} and are only visible
 * to Spring Boot's class loader, not the system class loader. That makes the {@code IdentifierScopes}
 * static initializer throw at runtime ({@code NoClassDefFoundError: Could not initialize class}).
 * We therefore load the same YAML resource ourselves through this class' own class loader, which
 * does see the nested jars.
 */
public final class IdentifierScopeRegistry {
  static final String RESOURCE = "/vocab/identifier-scopes/identifier-scopes.yaml";
  private static final Map<String, IdentifierScope> SCOPES = load();

  private IdentifierScopeRegistry() {}

  private static Map<String, IdentifierScope> load() {
    try (InputStream in = IdentifierScopeRegistry.class.getResourceAsStream(RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("Identifier scope registry resource not found: " + RESOURCE);
      }
      IdentifierScope[] scopes = YamlUtils.read(IdentifierScope[].class, in);
      Map<String, IdentifierScope> map = new HashMap<>();
      for (IdentifierScope s : scopes) {
        if (s.getScope() != null) {
          map.put(s.getScope().toLowerCase().trim(), s);
        }
      }
      return Map.copyOf(map);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load identifier scope registry from " + RESOURCE, e);
    }
  }

  /** @return the scope entry for the given scope string (case-insensitive), or null if unknown. */
  public static IdentifierScope byScope(String scope) {
    return scope == null ? null : SCOPES.get(scope.toLowerCase().trim());
  }
}
