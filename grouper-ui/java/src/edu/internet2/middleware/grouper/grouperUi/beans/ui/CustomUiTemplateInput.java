package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.subject.Subject;

/**
 * Input passed to compiled GSH custom-UI templates (subclasses of
 * GrouperTemplateCustomUi, <code>templateType=customUi</code>,
 * <code>templateMode=compiled</code>).
 *
 * Bundles the four things the legacy CustomUiContainer.gshRunScript method
 * passed positionally (the container, group, subject, logged-in subject)
 * plus the template-specific config fields (id, resolved GshTemplateConfig).
 *
 * The dispatcher constructs this from the per-action call site (a
 * gshRunJoinScript or gshRunLeaveScript bridge method on CustomUiContainer)
 * and hands it to the template's runOnJoin or runOnLeave method.
 *
 * Lives in the grouper-ui module because CustomUiContainer (one of its
 * fields) is itself in grouper-ui — keeping the bean here avoids a
 * grouper → grouper-ui dependency.
 *
 * GRP-7011
 */
public class CustomUiTemplateInput {

  // ---------------------------------------------------------------------
  // When adding a new field here, follow the OtherJobInput.copyFieldsTo
  // pattern if a subclass appears. For now there's no subclass; this is a
  // plain POJO.
  // ---------------------------------------------------------------------

  private CustomUiContainer customUiContainer;

  private Group group;

  private Subject subject;

  private Subject subjectLoggedIn;

  private String gshTemplateConfigId;

  private GshTemplateConfig gshTemplateConfig;

  /**
   * the custom UI container — carries the engine, variable bindings,
   * debug map, override map
   * @return the container
   */
  public CustomUiContainer getCustomUiContainer() {
    return this.customUiContainer;
  }

  /**
   * @param customUiContainer
   */
  public void setCustomUiContainer(CustomUiContainer customUiContainer) {
    this.customUiContainer = customUiContainer;
  }

  /**
   * the group being acted on
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * @param group
   */
  public void setGroup(Group group) {
    this.group = group;
  }

  /**
   * the subject being acted on (the member being added or removed)
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }

  /**
   * @param subject
   */
  public void setSubject(Subject subject) {
    this.subject = subject;
  }

  /**
   * the subject currently logged in (may equal subject when self-service,
   * or may be an admin acting on someone else)
   * @return the logged-in subject
   */
  public Subject getSubjectLoggedIn() {
    return this.subjectLoggedIn;
  }

  /**
   * @param subjectLoggedIn
   */
  public void setSubjectLoggedIn(Subject subjectLoggedIn) {
    this.subjectLoggedIn = subjectLoggedIn;
  }

  /**
   * config id of the GSH template that produced this invocation
   * @return config id
   */
  public String getGshTemplateConfigId() {
    return this.gshTemplateConfigId;
  }

  /**
   * @param gshTemplateConfigId
   */
  public void setGshTemplateConfigId(String gshTemplateConfigId) {
    this.gshTemplateConfigId = gshTemplateConfigId;
  }

  /**
   * the resolved template config, for templates that want to introspect
   * their own configuration
   * @return the config
   */
  public GshTemplateConfig getGshTemplateConfig() {
    return this.gshTemplateConfig;
  }

  /**
   * @param gshTemplateConfig
   */
  public void setGshTemplateConfig(GshTemplateConfig gshTemplateConfig) {
    this.gshTemplateConfig = gshTemplateConfig;
  }

}
