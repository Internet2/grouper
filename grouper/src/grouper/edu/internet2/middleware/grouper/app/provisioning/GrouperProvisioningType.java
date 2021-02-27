/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of table sync
 */
public enum GrouperProvisioningType {
  
  /**
   * full sync everything
   */
  fullProvisionFull {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }
    
    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return false;
    }

    @Override
    protected void provision(GrouperProvisioner grouperProvisioner) {
      grouperProvisioner.retrieveGrouperProvisioningLogic().provisionFull();
    }
    
  },
  incrementalProvisionChangeLog {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
    
    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return true;
    }

    @Override
    protected void provision(GrouperProvisioner grouperProvisioner) {
      grouperProvisioner.retrieveGrouperProvisioningLogic().provisionIncremental();
      
    }

  };

  /**
   * see if full metadata sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullMetadataSync();

  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullSync();

  /**
   * see if incremental sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isIncrementalSync();

 
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningType.class, string, exceptionOnNull);
  }

  protected abstract void provision(GrouperProvisioner grouperProvisioner);
}
