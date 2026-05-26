package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.app.loader.OtherJobTemplateInput;

/**
 * Base class for compiled Java GSH daemon templates
 * (<code>templateType=daemon</code>, <code>templateMode=compiled</code>).
 *
 * The dispatcher — an OtherJobScript-style bridge registered with Quartz —
 * reads the configured GSH template id, resolves the compiled class through
 * GshTemplateClassLoaderRegistry, instantiates a subclass of this class,
 * builds an OtherJobTemplateInput populated from the framework-provided
 * OtherJobInput (plus template-specific config fields), and calls
 * runDaemon(otherJobTemplateInput).
 *
 * Authors override runDaemon and do not have to deal with Quartz, the
 * OtherJobBase return value, or the OtherJobScript ThreadLocal machinery —
 * the bridge handles all of that before runDaemon is called. Status
 * reporting goes through the input's hib3GrouperLoaderLog. Throw any
 * exception to signal hard failure; the bridge records it on the log row.
 *
 * See GRP-7011 (this commit) for the type framework, and the design doc
 * at grouper/temp/trash/gshCompileImprovement.html for the full picture.
 */
public abstract class GrouperTemplateDaemon {

  /**
   * Override to implement the daemon's work. Called once per scheduler fire.
   *
   * @param otherJobTemplateInput context for this fire — Grouper session,
   *   job name, loader log row, GSH template config id, resolved
   *   GshTemplateConfig
   */
  public abstract void runDaemon(OtherJobTemplateInput otherJobTemplateInput);

}
