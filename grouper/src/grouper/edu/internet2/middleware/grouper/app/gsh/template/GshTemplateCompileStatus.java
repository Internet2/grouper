package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Per-JVM compile-status cache for compiled-Java GSH templates, backing the GSH
 * template inventory list (whether each compiled template compiles against the
 * running Grouper). Compiling is the expensive part, so the status is computed
 * once per (config id, source hash) and reused until the source changes — the
 * same source-hash invalidation the runtime registry uses.
 *
 * This is read-only validation: it does not define classes or affect the
 * runtime per-template classloader registry.
 *
 * GRP-7034
 */
public class GshTemplateCompileStatus {

  /**
   * outcome of a compile-status check for one compiled template's source
   */
  public static class GshTemplateCompileStatusResult {

    private final boolean success;
    private final long lastCompiledMillis;
    private final String diagnostics;

    /**
     * @param success true if the source compiled cleanly
     * @param lastCompiledMillis when this status was computed
     * @param diagnostics compiler/parse diagnostics when not successful; null on success
     */
    public GshTemplateCompileStatusResult(boolean success, long lastCompiledMillis, String diagnostics) {
      this.success = success;
      this.lastCompiledMillis = lastCompiledMillis;
      this.diagnostics = diagnostics;
    }

    /**
     * @return true if the source compiled cleanly
     */
    public boolean isSuccess() {
      return this.success;
    }

    /**
     * @return epoch millis when this status was computed
     */
    public long getLastCompiledMillis() {
      return this.lastCompiledMillis;
    }

    /**
     * @return compiler/parse diagnostics when not successful; null on success
     */
    public String getDiagnostics() {
      return this.diagnostics;
    }
  }

  /**
   * cache entry — the source hash that produced the result, and the result
   */
  private static class CacheEntry {
    private String sourceHash;
    private GshTemplateCompileStatusResult result;
  }

  /**
   * config id → (source hash, result)
   */
  private static final ConcurrentMap<String, CacheEntry> CONFIG_ID_TO_STATUS =
      new ConcurrentHashMap<String, CacheEntry>();

  /**
   * Compile-status for a compiled template's source, cached per
   * (configId, sourceHash). Recompiles only when the source changes.
   *
   * @param configId template config id (cache key); must be non-empty
   * @param javaSource the Java source the caller read from inline config or a
   *   container file; treated as empty when null
   * @return the cached or freshly-computed status
   */
  public static GshTemplateCompileStatusResult statusForSource(String configId, String javaSource) {
    if (StringUtils.isBlank(configId)) {
      throw new IllegalArgumentException("configId must be non-empty");
    }
    String source = javaSource == null ? "" : javaSource;
    String sourceHash = GrouperUtil.encryptShaHex(source);

    CacheEntry cached = CONFIG_ID_TO_STATUS.get(configId);
    if (cached != null && StringUtils.equals(cached.sourceHash, sourceHash)) {
      return cached.result;
    }

    String diagnostics = GshTemplateConfiguration.compileDiagnosticsOrNull(source);
    GshTemplateCompileStatusResult result =
        new GshTemplateCompileStatusResult(diagnostics == null, System.currentTimeMillis(), diagnostics);

    CacheEntry newEntry = new CacheEntry();
    newEntry.sourceHash = sourceHash;
    newEntry.result = result;
    CONFIG_ID_TO_STATUS.put(configId, newEntry);

    return result;
  }

  /**
   * Clear the cache (hooked into cache-clear; tests use it between cases).
   */
  public static void clearCache() {
    CONFIG_ID_TO_STATUS.clear();
  }

  /**
   * static-utility class; no instantiation
   */
  private GshTemplateCompileStatus() {
  }

}
