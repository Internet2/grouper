package edu.internet2.middleware.grouper.app.interfolio;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime provisioning configuration for the Interfolio provisioner.  Reads the provisioner-specific
 * settings (the Interfolio external system to connect to, and whether to also grant FS access).
 */
public class InterfolioProvisioningConfiguration extends GrouperProvisioningConfiguration {

  /** config id of the Interfolio external system to provision to */
  private String interfolioExternalSystemConfigId;

  /**
   * whether to also subscribe/unsubscribe users to FS (faculty search) in addition to RPT.  RPT is
   * always granted on create and removed on delete; FS is gated by this flag.  Defaults to true.
   */
  private boolean enableFs;

  @Override
  public void configureSpecificSettings() {
    this.interfolioExternalSystemConfigId = this.retrieveConfigString("interfolioExternalSystemConfigId", true);
    this.enableFs = GrouperUtil.booleanValue(this.retrieveConfigString("enableFs", false), true);
  }

  public String getInterfolioExternalSystemConfigId() {
    return interfolioExternalSystemConfigId;
  }

  public void setInterfolioExternalSystemConfigId(String interfolioExternalSystemConfigId) {
    this.interfolioExternalSystemConfigId = interfolioExternalSystemConfigId;
  }

  public boolean isEnableFs() {
    return enableFs;
  }

  public void setEnableFs(boolean enableFs) {
    this.enableFs = enableFs;
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    super.setThreadPoolSize(1);
  }

}
