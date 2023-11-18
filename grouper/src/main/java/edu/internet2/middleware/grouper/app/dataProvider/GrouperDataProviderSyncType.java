package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of sync
 */
public enum GrouperDataProviderSyncType {
  
  /**
   * full sync everything
   */
  fullSyncFull {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }

    @Override
    public boolean isIncrementalSync() {
      return false;
    }

    @Override
    protected void sync(GrouperDataProviderSync grouperDataProviderSync) {
      grouperDataProviderSync.retrieveGrouperDataProviderLogic().syncFull();
    }
    
  },
  incrementalSyncChangeLog {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }

    @Override
    public boolean isIncrementalSync() {
      return true;
    }

    @Override
    protected void sync(GrouperDataProviderSync grouperDataProviderSync) {
      grouperDataProviderSync.retrieveGrouperDataProviderLogic().syncIncremental();
      
    }

  };

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
  public static GrouperDataProviderSyncType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperDataProviderSyncType.class, string, exceptionOnNull);
  }

  protected abstract void sync(GrouperDataProviderSync grouperDataProviderSync);
}
