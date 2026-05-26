package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Execution mode for a GSH template. The mode determines which engine
 * handles the template's source body at runtime.
 *
 * <ul>
 *   <li><code>interpreted</code> — the legacy path. The source body (Groovy
 *       or Java-syntax-compatible-with-Groovy) is parsed and executed by
 *       the Groovy engine on each invocation. Available for templateType
 *       values gsh, abac, and provisioner. Default for backward compat —
 *       existing config rows without an explicit templateMode behave
 *       exactly as they always have.</li>
 *   <li><code>compiled</code> — the new path. The source body is real Java,
 *       compiled to bytecode once (and re-compiled when the source hash
 *       changes), defined in a per-template ByteArrayClassLoader, and
 *       invoked as method calls on the loaded class. Available for every
 *       templateType. Required for the new types (daemon, daemonChangeLog,
 *       report, customUi, hook, library) since they have no legacy
 *       interpreted path.</li>
 * </ul>
 *
 * GRP-7011
 */
public enum GshTemplateMode {

  /**
   * legacy Groovy-engine-interpreted source
   */
  interpreted,

  /**
   * compiled-on-save Java source, loaded via the per-template classloader
   * registry
   */
  compiled;

  /**
   * Resolve an enum value by case-insensitive name, mirroring the
   * convention used by GshTemplateType.
   *
   * @param string name of the enum value (case insensitive)
   * @param exceptionOnNotFound true to throw if the name doesn't match
   * @return the matched enum value, or null if not found and
   *   exceptionOnNotFound is false
   */
  public static GshTemplateMode valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateMode.class, string, exceptionOnNotFound);
  }

}
