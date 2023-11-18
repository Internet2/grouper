package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * provisioning log
 */
public class GrouperProvisioningLogCommands {
  
  private GrouperProvisioner grouperProvisioner;
  
  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  private int infoCount = 0;
  /**
   * debug log
   * @param string
   */
  public void infoLog(String string) {
    LOG.info(string);
    if (infoCount++ < 1000) {
      this.grouperProvisioner.retrieveGrouperProvisioningObjectLog().getObjectLog().append(new Timestamp(System.currentTimeMillis()))
        .append(": ").append(this.grouperProvisioner.getInstanceId()).append(", ").append(this.grouperProvisioner.getConfigId())
        .append(", ").append(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType()).append(": INFO: ").append(string).append("\n\n");
    }
  }
  private int errorCount = 0;

  /**
   * debug log
   * @param string
   */
  public void errorLog(String string) {
    LOG.error(string);
    if (errorCount++ < 1000) {
      this.grouperProvisioner.retrieveGrouperProvisioningObjectLog().getObjectLog().append(new Timestamp(System.currentTimeMillis())).append(": ")
      .append(this.grouperProvisioner.getInstanceId()).append(", ").append(this.grouperProvisioner.getConfigId())
      .append(", ").append(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType()).append(": ERROR: ").append(string).append("\n\n");
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLogCommands.class);
  
}
