package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry.GshTemplateResolveResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Shared helper that turns a compiled-Java GSH template config into a ready
 * instance of the type's framework base class. Every compiled-template
 * dispatcher (GshTemplateExec for gsh/abac, OtherJobScript for daemons, and
 * the change-log / report / customUi / hook / library wirings to come) funnels
 * through here so the resolve &rarr; cast &rarr; instantiate sequence and its
 * error reporting live in one place.
 *
 * <p>The sequence:</p>
 * <ol>
 *   <li>read the template source from its configured location
 *       (GshTemplateConfig.readSource() — inline config or container file);</li>
 *   <li>resolve the compiled class through GshTemplateClassLoaderRegistry
 *       (compile-on-miss, source-hash cache);</li>
 *   <li>cast the Class to the expected framework base via asSubclass(),
 *       surfacing a clear message if the author extended the wrong base;</li>
 *   <li>instantiate via the public no-arg constructor.</li>
 * </ol>
 *
 * <p>Parse and compile failures are turned into a RuntimeException carrying the
 * registry diagnostics so the caller's normal error path (loader log, report
 * status, UI inline errors) reports them.</p>
 *
 * GRP-7026
 */
public class GshTemplateCompiledDispatch {

  /**
   * Resolve, cast, and instantiate a compiled-Java template.
   *
   * @param <T> the framework base class type for this templateType
   * @param configId GSH template config id (registry cache key); must be non-empty
   * @param gshTemplateConfig the populated config (source location, mode)
   * @param baseClass the framework base the compiled class must extend/implement
   * @return a new instance cast to baseClass
   */
  public static <T> T instantiate(String configId, GshTemplateConfig gshTemplateConfig, Class<T> baseClass) {

    if (gshTemplateConfig == null) {
      throw new IllegalArgumentException("gshTemplateConfig must be non-null for '" + configId + "'");
    }
    if (baseClass == null) {
      throw new IllegalArgumentException("baseClass must be non-null for '" + configId + "'");
    }

    String javaSource = gshTemplateConfig.readSource();
    if (javaSource == null) {
      throw new RuntimeException("GSH template '" + configId + "' has no source to compile");
    }

    GshTemplateResolveResult resolveResult = GshTemplateClassLoaderRegistry.resolve(configId, javaSource);
    if (!resolveResult.isSuccess()) {
      throw new RuntimeException(describeFailure(configId, resolveResult));
    }

    Class<?> templateClass = resolveResult.getTemplateClass();

    Class<? extends T> subclass = null;
    try {
      subclass = templateClass.asSubclass(baseClass);
    } catch (ClassCastException cce) {
      throw new RuntimeException("GSH template '" + configId + "' class "
          + templateClass.getName() + " must extend/implement "
          + baseClass.getName() + " for this template type", cce);
    }

    return GrouperUtil.newInstance(subclass);
  }

  /**
   * Build a readable failure message from a non-success resolve result —
   * either a parse error (no package / no public class) or compile diagnostics.
   *
   * @param configId GSH template config id
   * @param resolveResult the failed resolve result
   * @return a human-readable failure message
   */
  private static String describeFailure(String configId, GshTemplateResolveResult resolveResult) {
    StringBuilder message = new StringBuilder("GSH template '" + configId + "' did not compile: ");
    if (resolveResult.getParseError() != null) {
      message.append(resolveResult.getParseError());
      return message.toString();
    }
    GshTemplateCompileResult compileResult = resolveResult.getCompileResult();
    if (compileResult != null) {
      boolean first = true;
      for (GshTemplateCompileDiagnostic diagnostic : compileResult.errorDiagnostics()) {
        if (!first) {
          message.append("; ");
        }
        message.append(diagnostic.toString());
        first = false;
      }
    }
    return message.toString();
  }

  /**
   * static-utility class; no instantiation
   */
  private GshTemplateCompiledDispatch() {
  }

}
