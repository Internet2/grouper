/**
 * 
 */
package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class GrouperProvisioningLoader {
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLoader.class);

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

  public String getLoaderEntityTableName() {
    return null;
  }
  
  public List<String> getLoaderEntityColumnNames() {
    return null;
  }
  
  public List<String> getLoaderEntityKeyColumnNames() {
    return null;
  }

  public List<Object[]> retrieveLoaderEntityTableDataFromDataBean() {
    return null;
  }
  
}
