/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * type of table sync
 */
public enum LdapSyncSubtype {
  
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
    

    /**
     * 
     */
    @Override
    public void retrieveData(final Map<String, Object> debugMap, final LdapSync ldapSync) {
      
    }
    

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, LdapSync gcTableSync) {
      
      return null;
    }


  },
  
  /**
   * get all incremental rows, which have the primary keys of rows that need updating
   */
  incrementalPrimaryKey {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
    

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, LdapSync ldapSync) {
      return null;
      
    }


    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, LdapSync ldapSync) {
               
    }

    
  };
  
  /**
   * log object
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(LdapSyncSubtype.class);
  
  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullSync();
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static LdapSyncSubtype valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperClientUtils.enumValueOfIgnoreCase(LdapSyncSubtype.class, 
        string, exceptionOnNull);
  }

  /**
   * do the initial select query for the sync
   * @param debugMap, gcTableSync
   */
  public abstract void retrieveData(Map<String, Object> debugMap, LdapSync gcTableSync);
  
  /**
   * do the initial compare step
   * @param debugMap, gcTableSync
   * @return records changed or null if not applicable
   */
  public abstract Integer syncData(Map<String, Object> debugMap, LdapSync gcTableSync);
}
