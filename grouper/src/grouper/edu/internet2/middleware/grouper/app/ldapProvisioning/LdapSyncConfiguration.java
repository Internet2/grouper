/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 * an instance of this class focuses on the configuration for table sync
 * create an instance, set the key, and call configure
 */
public class LdapSyncConfiguration {

  /**
   * subtype which also implies which type (full | incremental)
   */
  private LdapSyncSubtype ldapSyncSubtype = null;
  
  
  /**
   * subtype which also implies which type (full | incremental)
   * @return the gcTableSyncSubtype
   */
  public LdapSyncSubtype getGcTableSyncSubtype() {
    return this.ldapSyncSubtype;
  }

  
  /**
   * subtype which also implies which type (full | incremental)
   * @param gcTableSyncSubtype1 the gcTableSyncSubtype to set
   */
  public void setGcTableSyncSubtype(LdapSyncSubtype gcTableSyncSubtype1) {
    this.ldapSyncSubtype = gcTableSyncSubtype1;
  }

  

  /**
   * key in config that points to this instance of table sync
   */
  private String configKey;

  /**
   * 
   */
  public LdapSyncConfiguration() {
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Integer retrieveConfigInt(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.intObjectValue(configValueString, true);
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Boolean retrieveConfigBoolean(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.booleanObjectValue(configValueString);
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public String retrieveConfigString(String configName, boolean required) {
    
    String value = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.configKey + "." + configName);
    if (!StringUtils.isBlank(value)) {
      return value;
    }
    value = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTableDefault." + configName);
    if (!StringUtils.isBlank(value)) {
      return value;
    }
    if (required) {
      throw new RuntimeException("Cant find config for syncTable: " + this.configKey + ": " + configName);
    }
    return null;

  }
  
  /**
   * gc table sync
   */
  private LdapSync ldapSync;
  
  /**
   * gc table sync
   * @return table sync
   */
  public LdapSync getGcTableSync() {
    return this.ldapSync;
  }

  /**
   * gc table sync
   * @param gcTableSync1
   */
  public void setGcTableSync(LdapSync gcTableSync1) {
    this.ldapSync = gcTableSync1;
  }

  /**
   * links to ldap config in grouper-loader.properties
   */
  private String ldapPoolName;

  /**
   * links to ldap config in grouper-loader.properties
   * @return ldap pool name
   */
  public String getLdapPoolName() {
    return this.ldapPoolName;
  }


  /**
   * links to ldap config in grouper-loader.properties
   * @param ldapPoolName1
   */
  public void setLdapPoolName(String ldapPoolName1) {
    this.ldapPoolName = ldapPoolName1;
  }

  /**
   * true or false if needs to get users from ldap defaults to true
   */
  private boolean needsTargetSystemUsers;

  
  
  /**
   * true or false if needs to get users from ldap defaults to true
   * @return true of false
   */
  public boolean isNeedsTargetSystemUsers() {
    return this.needsTargetSystemUsers;
  }


  /**
   * true or false if needs to get users from ldap defaults to true
   * @param needsTargetSystemUsers1
   */
  public void setNeedsTargetSystemUsers(boolean needsTargetSystemUsers1) {
    this.needsTargetSystemUsers = needsTargetSystemUsers1;
  }


  /**
   * @param debugMap
   * @param configKey
   * @param ldapSyncSubtype
   */
  public void configureTableSync(Map<String, Object> debugMap, LdapSync theGcTableSync, String theConfigKey, LdapSyncSubtype theGcTableSyncSubtype) {

    if (debugMap == null) {
      debugMap = new LinkedHashMap<String, Object>();
    }
    
    try {
      this.ldapSync = theGcTableSync;
      
      this.setConfigKey(theConfigKey);
      this.setGcTableSyncSubtype(theGcTableSyncSubtype);

      this.ldapPoolName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("ldapProvisioner." + theConfigKey + ".ldapPoolName");
      debugMap.put("ldapPoolName", this.ldapPoolName);
      
      this.needsTargetSystemUsers = GrouperClientConfig.retrieveConfig().propertyValueBoolean("ldapProvisioner." + theConfigKey + ".needsTargetSystemUsers", true);
      if (!this.needsTargetSystemUsers) {
        debugMap.put("needsTargetSystemUsers", this.needsTargetSystemUsers);
      }
      
    } catch (RuntimeException re) {
      if (this.ldapSync != null && this.ldapSync.getGcGrouperSyncLog() != null) {
        try {
          this.ldapSync.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.CONFIG_ERROR);
          this.ldapSync.getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
        } catch (RuntimeException re2) {
          GrouperClientUtils.injectInException(re, "***** START ANOTHER EXCEPTON *******" + GrouperClientUtils.getFullStackTrace(re2) + "***** END ANOTHER EXCEPTON *******");
        }
      }
      throw re;
    }
    
  }

  /**
   * switch from incremental to full if the number of groups (and records over threshold) is over this threshold
   * i.e. needs to be over 100 groups and over 300000 records
   */
  private int switchFromIncrementalToFullIfOverGroupCount;

  /**
   * switch from incremental to full if the number of groups (and records over threshold) is over this threshold
   * i.e. needs to be over 100 groups and over 300000 records
   * @return threshold
   */
  public int getSwitchFromIncrementalToFullIfOverGroupCount() {
    return this.switchFromIncrementalToFullIfOverGroupCount;
  }

  /**
   * switch from incremental to full if the number of groups (and records over threshold) is over this threshold
   * i.e. needs to be over 100 groups and over 300000 records
   * @param switchFromIncrementalToFullIfOverGroupCount1
   */
  public void setSwitchFromIncrementalToFullIfOverGroupCount(
      int switchFromIncrementalToFullIfOverGroupCount1) {
    this.switchFromIncrementalToFullIfOverGroupCount = switchFromIncrementalToFullIfOverGroupCount1;
  }

  /**
   * switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
   */
  private int switchFromIncrementalToGroupIfOverRecordsInGroup;
  
  /**
   * switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
   * @return threshold
   */
  public int getSwitchFromIncrementalToGroupIfOverRecordsInGroup() {
    return this.switchFromIncrementalToGroupIfOverRecordsInGroup;
  }

  /**
   * switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
   * @param switchFromIncrementalToGroupIfOverRecordsInGroup1
   */
  public void setSwitchFromIncrementalToGroupIfOverRecordsInGroup(
      int switchFromIncrementalToGroupIfOverRecordsInGroup1) {
    this.switchFromIncrementalToGroupIfOverRecordsInGroup = switchFromIncrementalToGroupIfOverRecordsInGroup1;
  }

  /**
   * switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
   * fullSyncChangeFlag, fullSyncFull, fullSyncGroups
   */
  private LdapSyncSubtype switchFromIncrementalToFullSubtype;

  /**
   * switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
   * fullSyncChangeFlag, fullSyncFull, fullSyncGroups
   * @return type
   */
  public LdapSyncSubtype getSwitchFromIncrementalToFullSubtype() {
    return this.switchFromIncrementalToFullSubtype;
  }

  /**
   * switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
   * fullSyncChangeFlag, fullSyncFull, fullSyncGroups
   * @param switchFromIncrementalToFullSubtype1
   */
  public void setSwitchFromIncrementalToFullSubtype(
      LdapSyncSubtype switchFromIncrementalToFullSubtype1) {
    this.switchFromIncrementalToFullSubtype = switchFromIncrementalToFullSubtype1;
  }

  /**
   * switch from incremental to full if the number of incrementals is over this threshold
   */
  private int switchFromIncrementalToFullIfOverRecords;

  /**
   * switch from incremental to full if the number of incrementals is over this threshold
   * @return threshold
   */
  public int getSwitchFromIncrementalToFullIfOverRecords() {
    return this.switchFromIncrementalToFullIfOverRecords;
  }

  /**
   * switch from incremental to full if the number of incrementals is over this threshold
   * @param switchFromIncrementalToFullIfOverRecords1
   */
  public void setSwitchFromIncrementalToFullIfOverRecords(
      int switchFromIncrementalToFullIfOverRecords1) {
    this.switchFromIncrementalToFullIfOverRecords = switchFromIncrementalToFullIfOverRecords1;
  }

  /**
   * key in config that points to this instance of table sync
   * @return the key
   */
  public String getConfigKey() {
    return this.configKey;
  }

  /**
   * key in config that points to this instance of table sync
   * @param key1 the key to set
   */
  public void setConfigKey(String key1) {
    this.configKey = key1;
  }


}
