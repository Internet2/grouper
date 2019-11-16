/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * type of table sync
 */
public enum GcTableSyncSubtype {
  
  /**
   * full sync all columns
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
  },
  
  /**
   * full sync but do groupings on single col primary key or single grouping col (e.g. group name of memberships)
   */
  fullSyncGroupings {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }
    
    /**
     * see if needs grouping column
     * @return true if needs grouping column
     */
    @Override
    public boolean isNeedsGroupingColumn() { 
      return true;
    }

  },
  
  /**
   * full sync get primary keys and a col that if not matching indicates an update
   */
  fullSyncChangeFlag {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }
  },
  
  /**
   * get all incremental rows and all columns in those rows (e.g. last updated col on source, which might not get deletes unless there is a disabled flag)
   */
  incrementalFullAllColumns {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
  },
  
  /**
   * get all incremental rows, which have the primary keys of rows that need updating
   */
  incrementalFullPrimaryKey {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
  };
  
  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullSync();
  
  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public boolean isNeedsGroupingColumn() { 
    return false;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GcTableSyncSubtype valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GcTableSyncSubtype.class, 
        string, exceptionOnNull);
  }
}
