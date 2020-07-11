/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.provisioning;

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
  
}
