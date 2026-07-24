/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasksJob.UpgradeTaskStatus;

/**
 * One row on the Configure -&gt; Upgrade tasks admin screen: a single Grouper upgrade task (a version of
 * the {@link edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasks} enum) together with the
 * status computed for the live database.  This is a read-only display bean - all of its values are
 * derived from the upgrade task metadata and the metadata group's completed-version attribute; nothing
 * here mutates state.
 */
public class GuiUpgradeTask {

  /** the upgrade task version number, e.g. 43 (the numeric part of the UpgradeTasks enum name "V43") */
  private int version;

  /** the externalized human-readable description of what the task does (from UpgradeTasksInterface.description()) */
  private String description;

  /** whether this is a DDL (schema-changing) task as opposed to a data/maintenance task */
  private boolean ddl;

  /** the Grouper release the task was introduced in, e.g. "7.3.0" (from versionIntroduced()) */
  private String releasedInVersion;

  /** the computed status for the live database: complete, not complete, or not applicable */
  private UpgradeTaskStatus status;

  /** optional free-text detail, e.g. the result of an on-demand "check status" for a DDL task */
  private String detail;

  /** whether this row is an "unexpected" task: a version recorded complete in the database's attribute
   * assignments for which the running Grouper jar has no matching {@link edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasks}
   * enum constant.  There is no code to run for it, so the UI only offers a cleanup (mark not complete) action. */
  private boolean unexpected;

  /**
   * the upgrade task version number, e.g. 43
   * @return version
   */
  public int getVersion() {
    return this.version;
  }

  /**
   * @param version1 the upgrade task version number
   */
  public void setVersion(int version1) {
    this.version = version1;
  }

  /**
   * the externalized human-readable description of what the task does
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @param description1 the externalized description
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * whether this is a DDL (schema-changing) task
   * @return true if DDL
   */
  public boolean isDdl() {
    return this.ddl;
  }

  /**
   * @param ddl1 whether this is a DDL task
   */
  public void setDdl(boolean ddl1) {
    this.ddl = ddl1;
  }

  /**
   * the Grouper release the task was introduced in, e.g. "7.3.0"
   * @return released in version
   */
  public String getReleasedInVersion() {
    return this.releasedInVersion;
  }

  /**
   * @param releasedInVersion1 the Grouper release the task was introduced in
   */
  public void setReleasedInVersion(String releasedInVersion1) {
    this.releasedInVersion = releasedInVersion1;
  }

  /**
   * the computed status for the live database
   * @return status
   */
  public UpgradeTaskStatus getStatus() {
    return this.status;
  }

  /**
   * @param status1 the computed status
   */
  public void setStatus(UpgradeTaskStatus status1) {
    this.status = status1;
  }

  /**
   * optional free-text detail, e.g. the result of an on-demand "check status"
   * @return detail
   */
  public String getDetail() {
    return this.detail;
  }

  /**
   * @param detail1 the detail text
   */
  public void setDetail(String detail1) {
    this.detail = detail1;
  }

  /**
   * whether this row is an "unexpected" task: recorded complete in the database's attribute assignments
   * but with no matching {@link edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasks} enum
   * constant in the running Grouper jar
   * @return true if unexpected
   */
  public boolean isUnexpected() {
    return this.unexpected;
  }

  /**
   * @param unexpected1 whether this row is an unexpected task
   */
  public void setUnexpected(boolean unexpected1) {
    this.unexpected = unexpected1;
  }

  /**
   * The externalized text key for this task's status label, so the JSP can render a localized label,
   * e.g. status COMPLETE -&gt; "configurationUpgradeTasksStatusCOMPLETE".
   * @return the text key for the status label
   */
  public String getStatusLabelKey() {
    return "configurationUpgradeTasksStatus" + (this.status == null ? "" : this.status.name());
  }

  /**
   * A color for the status badge so the table reads at a glance: complete green, not complete orange,
   * not applicable grey.
   * @return a CSS color value
   */
  public String getStatusColor() {
    if (this.status == null) {
      return "#000000";
    }
    switch (this.status) {
      case COMPLETE:
        return "#006400"; // dark green
      case NOT_COMPLETE:
        return "#cc6600"; // dark orange
      case NOT_APPLICABLE:
        return "#666666"; // grey
      case UNEXPECTED:
        return "#cc0000"; // dark red
      default:
        return "#000000";
    }
  }
}
