package org.gbif.taxon.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs HTTP requests whose total processing time exceeds a configurable threshold.
 * Times the entire chain (controller + serialization), so the duration matches what users see.
 */
public class SlowRequestFilter extends OncePerRequestFilter {
  private static final Logger LOG = LoggerFactory.getLogger(SlowRequestFilter.class);

  private final long thresholdMs;

  public SlowRequestFilter(long thresholdMs) {
    this.thresholdMs = thresholdMs;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    long start = System.nanoTime();
    try {
      chain.doFilter(request, response);
    } finally {
      long durationMs = (System.nanoTime() - start) / 1_000_000L;
      if (durationMs >= thresholdMs) {
        String query = request.getQueryString();
        LOG.warn("SLOW REQUEST {}ms {} {}{} -> {}",
            durationMs,
            request.getMethod(),
            request.getRequestURI(),
            query == null ? "" : "?" + query,
            response.getStatus());
      }
    }
  }
}
