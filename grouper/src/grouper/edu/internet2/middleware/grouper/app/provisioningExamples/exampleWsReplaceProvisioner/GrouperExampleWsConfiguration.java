package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperExampleWsConfiguration extends GrouperProvisioningConfiguration {
  
  private String exampleWsExternalSystemConfigId;
  
  private String exampleWsSource;
  
  public String getExampleWsSource() {
    return exampleWsSource;
  }
  
  public void setExampleWsSource(String exampleWsSource) {
    this.exampleWsSource = exampleWsSource;
  }
  
  public String getExampleWsExternalSystemConfigId() {
    return exampleWsExternalSystemConfigId;
  }

  public void setExampleWsExternalSystemConfigId(String exampleWsExternalSystemConfigId) {
    this.exampleWsExternalSystemConfigId = exampleWsExternalSystemConfigId;
  }

  @Override
  public void configureSpecificSettings() {
    this.exampleWsExternalSystemConfigId = this.retrieveConfigString("exampleWsExternalSystemConfigId", true);
    this.exampleWsSource = this.retrieveConfigString("exampleWsSource", true);
  }

}
