package edu.internet2.middleware.grouper.app.duo;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDuoConfiguration extends GrouperProvisioningConfiguration {

  private String duoExternalSystemConfigId;

  /**
   * GRP-7384: when true, the Duo native sync-back also captures derived per-user auth method
   * attributes (push / sms / voice phone counts, token counts by type, webauthn / u2f counts),
   * appended to the default or configured nativeAttributesEntities list
   */
  private boolean nativeAttributesEntitiesIncludeAuthMethods;

  @Override
  public void configureSpecificSettings() {
    
    this.duoExternalSystemConfigId = this.retrieveConfigString("duoExternalSystemConfigId", true);
    this.nativeAttributesEntitiesIncludeAuthMethods = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("nativeAttributesEntitiesIncludeAuthMethods", false), false);
  }

  /**
   * GRP-7384: if derived auth method attributes should be appended to the user sync-back capture
   * @return true to include auth method attributes
   */
  public boolean isNativeAttributesEntitiesIncludeAuthMethods() {
    return this.nativeAttributesEntitiesIncludeAuthMethods;
  }

  /**
   * @param nativeAttributesEntitiesIncludeAuthMethods1
   */
  public void setNativeAttributesEntitiesIncludeAuthMethods(boolean nativeAttributesEntitiesIncludeAuthMethods1) {
    this.nativeAttributesEntitiesIncludeAuthMethods = nativeAttributesEntitiesIncludeAuthMethods1;
  }

  
  public String getDuoExternalSystemConfigId() {
    return duoExternalSystemConfigId;
  }

  
  public void setDuoExternalSystemConfigId(String duoExternalSystemConfigId) {
    this.duoExternalSystemConfigId = duoExternalSystemConfigId;
  }

 
}
