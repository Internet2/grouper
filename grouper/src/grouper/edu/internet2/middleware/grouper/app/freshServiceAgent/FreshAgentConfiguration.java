package edu.internet2.middleware.grouper.app.freshServiceAgent;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class FreshAgentConfiguration extends GrouperProvisioningConfiguration {

  private String freshserviceExternalSystemConfigId;

  /**
   * Freshservice requires a role when an agent is created, this is the role id to assign new agents
   */
  private Long defaultAgentRoleId;

  /**
   * assignment_scope of the default role, e.g. entire_helpdesk
   */
  private String defaultAgentRoleAssignmentScope;

  /**
   * Freshservice reactivates (and converts requesters to) occasional agents, if true set them to full-time
   */
  private boolean reactivateAsFullTime;

  @Override
  public void configureSpecificSettings() {
    this.freshserviceExternalSystemConfigId = this.retrieveConfigString("freshserviceExternalSystemConfigId", true);
    String defaultAgentRoleIdString = this.retrieveConfigString("defaultAgentRoleId", false);
    this.defaultAgentRoleId = defaultAgentRoleIdString == null ? null : Long.parseLong(defaultAgentRoleIdString.trim());
    this.defaultAgentRoleAssignmentScope = this.retrieveConfigString("defaultAgentRoleAssignmentScope", false);
    if (this.defaultAgentRoleAssignmentScope == null) {
      this.defaultAgentRoleAssignmentScope = "entire_helpdesk";
    }
    Boolean reactivateAsFullTimeBoolean = this.retrieveConfigBoolean("reactivateAsFullTime", false);
    this.reactivateAsFullTime = reactivateAsFullTimeBoolean == null ? true : reactivateAsFullTimeBoolean;
  }

  /**
   * Get the FreshService external system config ID
   * @return the Freshservice external system config ID
   */
  public String getFreshserviceExternalSystemConfigId() {
    return freshserviceExternalSystemConfigId;
  }

  /**
   * Set the FreshService external system config ID
   * @param freshserviceExternalSystemConfigId the external system config ID to set
   */
  public void setFreshserviceExternalSystemConfigId(String freshserviceExternalSystemConfigId) {
    this.freshserviceExternalSystemConfigId = freshserviceExternalSystemConfigId;
  }

  /**
   * Freshservice requires a role when an agent is created, this is the role id to assign new agents
   * @return the role id or null if not configured
   */
  public Long getDefaultAgentRoleId() {
    return defaultAgentRoleId;
  }

  /**
   * Freshservice requires a role when an agent is created, this is the role id to assign new agents
   * @param defaultAgentRoleId
   */
  public void setDefaultAgentRoleId(Long defaultAgentRoleId) {
    this.defaultAgentRoleId = defaultAgentRoleId;
  }

  /**
   * assignment_scope of the default role, e.g. entire_helpdesk
   * @return the scope
   */
  public String getDefaultAgentRoleAssignmentScope() {
    return defaultAgentRoleAssignmentScope;
  }

  /**
   * assignment_scope of the default role, e.g. entire_helpdesk
   * @param defaultAgentRoleAssignmentScope
   */
  public void setDefaultAgentRoleAssignmentScope(String defaultAgentRoleAssignmentScope) {
    this.defaultAgentRoleAssignmentScope = defaultAgentRoleAssignmentScope;
  }

  /**
   * Freshservice reactivates (and converts requesters to) occasional agents, if true set them to full-time
   * @return true if full-time
   */
  public boolean isReactivateAsFullTime() {
    return reactivateAsFullTime;
  }

  /**
   * Freshservice reactivates (and converts requesters to) occasional agents, if true set them to full-time
   * @param reactivateAsFullTime
   */
  public void setReactivateAsFullTime(boolean reactivateAsFullTime) {
    this.reactivateAsFullTime = reactivateAsFullTime;
  }

}
