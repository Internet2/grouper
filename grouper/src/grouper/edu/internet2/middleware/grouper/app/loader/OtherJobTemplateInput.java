package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;

/**
 * Specialization of OtherJobInput passed to compiled GSH daemon templates
 * (subclasses of GrouperTemplateDaemon, <code>templateType=daemon</code>).
 *
 * Adds template-specific context — the GSH template config id and the
 * resolved GshTemplateConfig — to the framework-provided fields
 * (grouperSession, jobName, hib3GrouperLoaderLog) inherited from
 * OtherJobInput. Other OtherJobBase subclasses (non-template daemons) keep
 * receiving plain OtherJobInput from the framework; only the
 * compiled-template bridge constructs this subclass.
 *
 * The bridge populates this input by:
 * <pre>
 *   OtherJobTemplateInput templateInput = new OtherJobTemplateInput();
 *   parentOtherJobInput.copyFieldsTo(templateInput);
 *   templateInput.setGshTemplateConfigId(...);
 *   templateInput.setGshTemplateConfig(...);
 * </pre>
 *
 * copyFieldsTo is overridden below to also copy this subclass's fields
 * when the target is itself an OtherJobTemplateInput.
 *
 * GRP-7011
 */
public class OtherJobTemplateInput extends OtherJobInput {

  // ---------------------------------------------------------------------
  // When adding a new field here, also add a setter call for it in
  // copyFieldsTo below (inside the instanceof block).
  // ---------------------------------------------------------------------

  /**
   * config id of the GSH template that produced this daemon
   */
  private String gshTemplateConfigId;

  /**
   * the resolved template config, for templates that want to introspect
   * their own configuration (input definitions, owner, etc.)
   */
  private GshTemplateConfig gshTemplateConfig;

  /**
   * config id of the GSH template that produced this daemon
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

  /**
   * Override to also copy this subclass's fields when the target is an
   * OtherJobTemplateInput. Falls back to the parent behavior (copy only
   * the parent's fields) when the target isn't a subclass.
   */
  @Override
  public void copyFieldsTo(OtherJobInput target) {
    super.copyFieldsTo(target);
    if (target instanceof OtherJobTemplateInput) {
      OtherJobTemplateInput templateTarget = (OtherJobTemplateInput) target;
      templateTarget.setGshTemplateConfigId(this.gshTemplateConfigId);
      templateTarget.setGshTemplateConfig(this.gshTemplateConfig);
    }
  }

}
