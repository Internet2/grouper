package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.app.reports.GshReportRuntime;

/**
 * Base class for compiled Java GSH report templates
 * (<code>templateType=report</code>, <code>templateMode=compiled</code>).
 *
 * The dispatcher — a compiled-template sub-branch inside
 * ReportConfigType.GSH.retrieveReportDataByConfig — reads the configured
 * GSH template id, resolves the compiled class through
 * GshTemplateClassLoaderRegistry, instantiates a subclass of this class,
 * and calls runReport with the GshReportRuntime populated. The dispatcher
 * also assigns GshReportRuntime to its ThreadLocal before invoking, so
 * helper code using GshReportRuntime.retrieveGshReportRuntime() continues
 * to work.
 *
 * Authors populate report data via the runtime — typically
 * gshReportRuntime.getGrouperReportData().setFile(...) and related
 * accumulator methods. Context about the owning group/stem and subject is
 * available on the runtime.
 *
 * Throw any exception to signal hard failure; the dispatcher records it
 * on the report instance.
 *
 * GRP-7011
 */
public abstract class GrouperTemplateReport {

  /**
   * Override to produce the report's data.
   *
   * @param gshReportRuntime the runtime context — carries the owning
   *   group / stem, the report data accumulator, and the subject
   */
  public abstract void runReport(GshReportRuntime gshReportRuntime);

}
