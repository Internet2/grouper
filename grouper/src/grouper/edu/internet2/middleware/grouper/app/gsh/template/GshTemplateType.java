package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Type of a GSH template — what dispatcher fires it and what framework
 * base class the compiled-Java implementation extends.
 *
 * The first three values (gsh, abac, provisioner) are the existing legacy
 * types. They support templateMode=interpreted (Groovy engine) and
 * templateMode=compiled (compiled Java). For these types the compiled-Java
 * subclass extends GshTemplateV2 (or, for provisioner, GshTemplateProvisionerBase).
 *
 * The remaining values (daemon, daemonChangeLog, report, customUi, hook,
 * library) are new and only meaningful with templateMode=compiled — there is
 * no legacy Groovy path for them. The compiled-Java subclass extends a
 * dedicated framework base per type; see the design doc for the mapping.
 *
 * GRP-7011 added daemon, daemonChangeLog, report, customUi, hook, library.
 */
public enum GshTemplateType {

  /** standard GSH template — body extends GshTemplateV2 */
  gsh,

  /** ABAC-pattern template — body extends GshTemplateV2 */
  abac,

  /** GSH provisioner template — compiled-Java body extends GshTemplateProvisionerBase */
  provisioner,

  /**
   * daemon template — compiled-Java body extends GrouperTemplateDaemon and
   * implements runDaemon(OtherJobTemplateInput). Dispatched from
   * OtherJobScript via scriptType=compiledJava.
   */
  daemon,

  /**
   * change-log daemon template — compiled-Java body extends
   * GrouperTemplateDaemonChangeLog and implements
   * processRecords(EsbPublisherChangeLogScript). Dispatched from
   * EsbPublisherChangeLogScript via a compiled-template branch.
   */
  daemonChangeLog,

  /**
   * report template — compiled-Java body extends GrouperTemplateReport and
   * implements runReport(GshReportRuntime). Dispatched from
   * ReportConfigType.GSH via a compiled-template sub-branch.
   */
  report,

  /**
   * custom UI button-action template — compiled-Java body extends
   * GrouperTemplateCustomUi (in the grouper-ui module) and overrides one or
   * more action methods (runOnJoin, runOnLeave, etc.). Dispatched from the
   * custom UI controllers via dedicated join/leave bridge methods on
   * CustomUiContainer.
   */
  customUi,

  /**
   * hook template — compiled-Java body extends one of the existing hook
   * abstract classes (GroupHooks, MembershipHooks, StemHooks, etc.) and
   * overrides whichever event methods apply. Dispatched from the hooks
   * framework via a new hooks.&lt;domain&gt;.gshTemplateConfigIds config
   * property; no new base class introduced for this type.
   */
  hook,

  /**
   * library template — compiled-Java body that exposes reusable methods
   * for other templates to call. No required base class; no dispatcher of
   * its own. Other templates invoke it via the registry (by interface
   * cast when an institution-shared-jar interface is in play, or via
   * GrouperUtil.callMethod when reflection is preferred).
   */
  library;

  /**
   * do a case-insensitive matching
   *
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateType.class, string, exceptionOnNotFound);
  }

}
