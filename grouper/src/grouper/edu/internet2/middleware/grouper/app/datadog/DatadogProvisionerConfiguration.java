package edu.internet2.middleware.grouper.app.datadog;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class DatadogProvisionerConfiguration extends GrouperProvisioningConfiguration {

  private String datadogExternalSystemConfigId;

  private String datadogIgnoreUserEmails;

  private String datadogIgnoreRoles;

  private boolean datadogAddTeamAdminMetadata;

  public String getDatadogExternalSystemConfigId() {
    return datadogExternalSystemConfigId;
  }

  public void setDatadogExternalSystemConfigId(String datadogExternalSystemConfigId) {
    this.datadogExternalSystemConfigId = datadogExternalSystemConfigId;
  }

  public String getDatadogIgnoreUserEmails() {
    return datadogIgnoreUserEmails;
  }

  public void setDatadogIgnoreUserEmails(String datadogIgnoreUserEmails) {
    this.datadogIgnoreUserEmails = datadogIgnoreUserEmails;
  }

  public String getDatadogIgnoreRoles() {
    return datadogIgnoreRoles;
  }

  public void setDatadogIgnoreRoles(String datadogIgnoreRoles) {
    this.datadogIgnoreRoles = datadogIgnoreRoles;
  }

  public boolean isDatadogAddTeamAdminMetadata() {
    return datadogAddTeamAdminMetadata;
  }

  public void setDatadogAddTeamAdminMetadata(boolean datadogAddTeamAdminMetadata) {
    this.datadogAddTeamAdminMetadata = datadogAddTeamAdminMetadata;
  }

  @Override
  public void configureSpecificSettings() {

    this.datadogExternalSystemConfigId = this.retrieveConfigString("datadogExternalSystemConfigId", true);
    this.datadogIgnoreUserEmails = GrouperUtil.defaultIfBlank(this.retrieveConfigString("datadogIgnoreUserEmails", false), "");
    this.datadogIgnoreRoles = GrouperUtil.defaultIfBlank(this.retrieveConfigString("datadogIgnoreRoles", false), "");
    this.datadogAddTeamAdminMetadata = GrouperUtil.booleanValue(this.retrieveConfigString("datadogAddTeamAdminMetadata", false), false);

  }

}
