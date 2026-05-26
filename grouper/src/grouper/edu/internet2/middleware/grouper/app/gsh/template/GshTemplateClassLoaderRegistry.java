package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Per-JVM cache of compiled GSH Java template classes, keyed by template
 * name (config id). Each cache entry holds the source hash that produced
 * the class, the dedicated ByteArrayClassLoader that defines its bytecode,
 * and the loaded Class object itself.
 *
 * On resolve():
 *   - If the cache has an entry for the template name AND the cached source
 *     hash matches the new source's hash, return the cached Class. Statics
 *     on that Class survive across executions.
 *   - Otherwise compile the source via GshTemplateJavaCompiler, define the
 *     bytecode in a fresh ByteArrayClassLoader, atomically swap the cache
 *     entry, and return the new Class. The old loader becomes unreferenced
 *     and is eligible for GC once any in-flight executions holding
 *     references drain.
 *
 * The registry does not read the source from anywhere — the caller (the
 * dispatcher in Phase 4b) reads it from inline config (javaTemplateSource)
 * or from a container file (javaTemplateSourceContainerPath) and hands the
 * source string in. The registry only cares about (templateName, source).
 *
 * Concurrency:
 *   - Cache hits are lock-free (ConcurrentHashMap reads).
 *   - Cache misses serialize per template name via per-name lock objects, so
 *     two threads invoking the same template at the same time compile once,
 *     not twice; concurrent resolves of different templates run in parallel.
 *
 * GRP-7010
 */
public class GshTemplateClassLoaderRegistry {

  /**
   * One template version's cache entry — the source hash that produced it,
   * the classloader holding its bytecode, and the loaded class.
   */
  public static class GshTemplateCachedClass {

    private final String sourceHash;
    private final ByteArrayClassLoader byteArrayClassLoader;
    private final Class<? extends GshTemplateV2> templateClass;

    GshTemplateCachedClass(
        String sourceHash,
        ByteArrayClassLoader byteArrayClassLoader,
        Class<? extends GshTemplateV2> templateClass) {
      this.sourceHash = sourceHash;
      this.byteArrayClassLoader = byteArrayClassLoader;
      this.templateClass = templateClass;
    }

    /**
     * @return SHA-256 of the source string that compiled to this entry
     */
    public String getSourceHash() {
      return this.sourceHash;
    }

    /**
     * @return the loader holding this version's bytecode
     */
    public ByteArrayClassLoader getByteArrayClassLoader() {
      return this.byteArrayClassLoader;
    }

    /**
     * @return the loaded GshTemplateV2 subclass
     */
    public Class<? extends GshTemplateV2> getTemplateClass() {
      return this.templateClass;
    }
  }

  /**
   * Outcome of a resolve() call — either a cached/freshly-compiled class on
   * success, or a parse error / compile diagnostics on failure. The caller
   * inspects isSuccess() and then either uses the templateClass or
   * surfaces the failure details to the user (UI inline errors,
   * inventory-screen compile-status column, etc.).
   */
  public static class GshTemplateResolveResult {

    private final GshTemplateCachedClass cachedClass;
    private final GshTemplateCompileResult compileResult;
    private final String parseError;

    GshTemplateResolveResult(
        GshTemplateCachedClass cachedClass,
        GshTemplateCompileResult compileResult,
        String parseError) {
      this.cachedClass = cachedClass;
      this.compileResult = compileResult;
      this.parseError = parseError;
    }

    /**
     * @return true if a template class is available
     */
    public boolean isSuccess() {
      return this.cachedClass != null;
    }

    /**
     * @return the resolved template class on success; null on failure
     */
    public Class<? extends GshTemplateV2> getTemplateClass() {
      if (this.cachedClass == null) {
        return null;
      }
      return this.cachedClass.getTemplateClass();
    }

    /**
     * @return the full cache entry on success; null on failure
     */
    public GshTemplateCachedClass getCachedClass() {
      return this.cachedClass;
    }

    /**
     * @return compile diagnostics, populated on a compile-error failure or
     *   on a successful compile that produced warnings; null otherwise
     */
    public GshTemplateCompileResult getCompileResult() {
      return this.compileResult;
    }

    /**
     * @return parse-error message (no package, no public class, etc.);
     *   null if parsing succeeded
     */
    public String getParseError() {
      return this.parseError;
    }
  }

  /**
   * The cache — template name → (sourceHash, loader, Class).
   */
  private static final ConcurrentMap<String, GshTemplateCachedClass> TEMPLATE_NAME_TO_CACHED =
      new ConcurrentHashMap<String, GshTemplateCachedClass>();

  /**
   * Per-template-name locks. Used to serialize concurrent compiles of the
   * same template without serializing across different templates.
   */
  private static final ConcurrentMap<String, Object> TEMPLATE_NAME_TO_LOCK =
      new ConcurrentHashMap<String, Object>();

  /**
   * Resolve a template by name. If the cached source hash matches the new
   * source, returns the cached Class. Otherwise parses the FQN from source,
   * compiles, defines in a fresh ByteArrayClassLoader, swaps the cache
   * entry, returns the new Class. On parse or compile failure, returns a
   * failure result; never throws for user-input problems.
   *
   * @param templateName logical template name (config id); must be non-empty
   * @param javaSource Java source body (caller has already read it from
   *   inline config or a container file); must be non-null
   * @return result with the resolved class on success, or with parse-error /
   *   compile diagnostics on failure
   */
  public static GshTemplateResolveResult resolve(String templateName, String javaSource) {
    if (templateName == null || templateName.length() == 0) {
      throw new IllegalArgumentException("templateName must be non-empty");
    }
    if (javaSource == null) {
      throw new IllegalArgumentException("javaSource must be non-null");
    }

    String sourceHash = GrouperUtil.encryptShaHex(javaSource);

    // Fast path: lock-free cache hit
    GshTemplateCachedClass cached = TEMPLATE_NAME_TO_CACHED.get(templateName);
    if (cached != null && cached.getSourceHash() != null
        && cached.getSourceHash().equals(sourceHash)) {
      return new GshTemplateResolveResult(cached, null, null);
    }

    // Slow path: per-template lock, double-check, compile if needed
    Object lock = lockFor(templateName);
    synchronized (lock) {
      cached = TEMPLATE_NAME_TO_CACHED.get(templateName);
      if (cached != null && cached.getSourceHash() != null
          && cached.getSourceHash().equals(sourceHash)) {
        return new GshTemplateResolveResult(cached, null, null);
      }

      GshTemplateSourceParser.GshTemplateSourceParseResult parseResult =
          GshTemplateSourceParser.parse(javaSource);
      if (!parseResult.isSuccess()) {
        return new GshTemplateResolveResult(null, null, parseResult.getErrorMessage());
      }
      String fullyQualifiedClassName = parseResult.getFullyQualifiedClassName();

      GshTemplateCompileResult compileResult =
          GshTemplateJavaCompiler.compile(fullyQualifiedClassName, javaSource);
      if (!compileResult.isSuccess()) {
        return new GshTemplateResolveResult(null, compileResult, null);
      }

      ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(
          GshTemplateV2.class.getClassLoader(),
          compileResult.getClassNameToBytecode());

      Class<?> rawLoadedClass;
      try {
        rawLoadedClass = byteArrayClassLoader.loadClass(fullyQualifiedClassName);
      } catch (ClassNotFoundException e) {
        // Bytecode for the top-level class is present in the map (we just compiled it) so
        // ClassNotFoundException here would indicate a bug in the FQN parsing or compile path.
        throw new RuntimeException(
            "Compiled class '" + fullyQualifiedClassName
                + "' not found in fresh classloader; this is a bug", e);
      }

      if (!GshTemplateV2.class.isAssignableFrom(rawLoadedClass)) {
        return new GshTemplateResolveResult(null, compileResult,
            "Compiled class '" + fullyQualifiedClassName
                + "' does not extend GshTemplateV2");
      }

      Class<? extends GshTemplateV2> templateClass = rawLoadedClass.asSubclass(GshTemplateV2.class);

      GshTemplateCachedClass newCached =
          new GshTemplateCachedClass(sourceHash, byteArrayClassLoader, templateClass);
      TEMPLATE_NAME_TO_CACHED.put(templateName, newCached);

      return new GshTemplateResolveResult(newCached, compileResult, null);
    }
  }

  /**
   * Look up (or create) the lock object for a given template name. Lock
   * objects accumulate across the JVM lifetime, one per template name ever
   * resolved — negligible memory cost.
   *
   * @param templateName template config id
   * @return a stable lock object for this template name
   */
  private static Object lockFor(String templateName) {
    Object lock = TEMPLATE_NAME_TO_LOCK.get(templateName);
    if (lock != null) {
      return lock;
    }
    Object newLock = new Object();
    Object existing = TEMPLATE_NAME_TO_LOCK.putIfAbsent(templateName, newLock);
    if (existing != null) {
      return existing;
    }
    return newLock;
  }

  /**
   * Clear all cached template classes. Hooked into
   * GrouperCacheUtils.clearAllCaches() and called by tests between cases.
   * After clear, every subsequently-resolved template will be recompiled
   * from source on first access. Per-template static state is reset.
   *
   * The per-template lock map is intentionally NOT cleared — locks are
   * stable per template name and clearing them during concurrent use would
   * allow two threads to acquire different lock objects for the same
   * template name and both compile in parallel. Locks accumulate at most
   * one entry per distinct template name ever seen on this JVM, which is
   * negligible.
   */
  public static void clearCache() {
    TEMPLATE_NAME_TO_CACHED.clear();
  }

  /**
   * Test-only: read the current cache entry for a template, or null. Used
   * by tests to verify swap behavior without going through resolve().
   */
  static GshTemplateCachedClass peekForTesting(String templateName) {
    return TEMPLATE_NAME_TO_CACHED.get(templateName);
  }

  /**
   * Static-utility class; no instantiation.
   */
  private GshTemplateClassLoaderRegistry() {
  }

}
