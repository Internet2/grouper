package edu.internet2.middleware.grouper.app.okta;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class GrouperOktaConfiguration extends GrouperProvisioningConfiguration {
  
  private String oktaExternalSystemConfigId;
  

  public String getOktaExternalSystemConfigId() {
    return oktaExternalSystemConfigId;
  }
  
  public void setOktaExternalSystemConfigId(String oktaExternalSystemConfigId) {
    this.oktaExternalSystemConfigId = oktaExternalSystemConfigId;
  }
  

  @Override
  public void configureSpecificSettings() {
    this.oktaExternalSystemConfigId = this.retrieveConfigString("oktaExternalSystemConfigId", true);
  }
  
  @Override
  public int getDaoSleepBeforeSelectAfterInsertMillis() {
    return GrouperUtil.intValue(this.retrieveConfigInt("sleepBeforeSelectAfterInsertMillis", false), 15000);
  }
  
  @Override
  public void configureAfterMetadata() {
    super.configureAfterMetadata();
  }

}
