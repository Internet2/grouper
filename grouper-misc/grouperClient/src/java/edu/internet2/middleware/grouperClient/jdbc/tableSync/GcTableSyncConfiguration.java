/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 * an instance of this class focuses on the configuration for table sync
 * create an instance, set the key, and call configure
 */
public class GcTableSyncConfiguration {

  /**
   * database from key
   */
  private String databaseFrom;
  
  /**
   * database from key
   * @return the databaseFrom
   */
  public String getDatabaseFrom() {
    return this.databaseFrom;
  }

  /**
   * database to key
   * @return the databaseTo
   */
  public String getDatabaseTo() {
    return this.databaseTo;
  }

  /**
   * database to key (readonly) if large queries should be performed against a different database
   */
  private String databaseToReadonly;

  /**
   * database to key (readonly) if large queries should be performed against a different database
   * @return the databaseToReadonly
   */
  public String getDatabaseToReadonly() {
    return this.databaseToReadonly;
  }
  
  /**
   * database to or readonly
   * @return the databaseToReadonly if exists or databaseTo
   */
  public String getDatabaseToOrReadonly() {
    return GrouperClientUtils.defaultIfBlank(this.databaseToReadonly, this.databaseTo);
  }
  
  /**
   * database to key (readonly) if large queries should be performed against a different database
   * @param databaseToReadonly1 the databaseToReadonly to set
   */
  public void setDatabaseToReadonly(String databaseToReadonly1) {
    this.databaseToReadonly = databaseToReadonly1;
  }

  /**
   * database to key
   */
  private String databaseTo;

  /**
   * subtype which also implies which type (full | incremental)
   */
  private GcTableSyncSubtype gcTableSyncSubtype = null;
  
  
  /**
   * subtype which also implies which type (full | incremental)
   * @return the gcTableSyncSubtype
   */
  public GcTableSyncSubtype getGcTableSyncSubtype() {
    return this.gcTableSyncSubtype;
  }

  
  /**
   * subtype which also implies which type (full | incremental)
   * @param gcTableSyncSubtype1 the gcTableSyncSubtype to set
   */
  public void setGcTableSyncSubtype(GcTableSyncSubtype gcTableSyncSubtype1) {
    this.gcTableSyncSubtype = gcTableSyncSubtype1;
  }

  

  /**
   * group column
   */
  private String groupColumnString;
 
  /**
   * number of bind vars in select
   */
  private int maxBindVarsInSelect;

  
  /**
   * number of bind vars in select
   * @return number of bind vars in select
   */
  public int getMaxBindVarsInSelect() {
    return this.maxBindVarsInSelect;
  }

  /**
   * 
   * @param maxBindVarsInSelect1
   */
  public void setMaxBindVarsInSelect(int maxBindVarsInSelect1) {
    this.maxBindVarsInSelect = maxBindVarsInSelect1;
  }

  /**
   * batch size when batching data
   */
  private int batchSize;
  
  
  /**
   * batch size when batching data
   * @return the batchSize
   */
  public int getBatchSize() {
    return this.batchSize;
  }

  
  /**
   * batch size when batching data
   * @param batchSize1 the batchSize to set
   */
  public void setBatchSize(int batchSize1) {
    this.batchSize = batchSize1;
  }

  /**
   * how many to group by
   */
  private int groupingSize;

  /**
   * @return the groupSize
   */
  public int getGroupingSize() {
    return this.groupingSize;
  }

  /**
   * key in config that points to this instance of table sync
   */
  private String configKey;

  /**
   * if doing fullSyncChangeFlag (look for a col that says if the rows are equal, e.g. a timestamp or a checksum)
   */
  private String changeFlagColumnString;
  
  /**
   * if doing fullSyncChangeFlag (look for a col that says if the rows are equal, e.g. a timestamp or a checksum)
   * @return col
   */
  public String getChangeFlagColumnString() {
    return this.changeFlagColumnString;
  }

  /**
   * if doing fullSyncChangeFlag (look for a col that says if the rows are equal, e.g. a timestamp or a checksum)
   * @param fullSyncChangeFlagColumnString1
   */
  public void setChangeFlagColumnString(String fullSyncChangeFlagColumnString1) {
    this.changeFlagColumnString = fullSyncChangeFlagColumnString1;
  }

  /**
   * name of a column in progress table that has a sequence or last updated date
   */
  private String incrementalProgressColumnString;

  /**
   * name of a column that has a sequence or last updated date
   * @return incremental col
   */
  public String getIncrementalProgressColumnString() {
    return this.incrementalProgressColumnString;
  }

  /**
   * name of a column that has a sequence or last updated date
   * @param incrementalAllColumnsColumnString1
   */
  public void setIncrementalProgressColumnString(
      String incrementalAllColumnsColumnString1) {
    this.incrementalProgressColumnString = incrementalAllColumnsColumnString1;
  }

  /**
   * table where real time primary key and last_updated col is
   */
  private String incrementalPrimaryKeyTable;
  /**
   * grouperClient.syncTable.personSource.statusDatabase = awsDev
   */
  private String statusDatabase;
  /**
   * table name from
   */
  private String tableFrom;
  /**
   * table name to
   */
  private String tableTo;

  /**
   * 
   */
  public GcTableSyncConfiguration() {
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
   * primary key columns, * means all columns, or list of comma separated values, default to *
   */
  private String primaryKeyColumnsString;
  
  /**
   * all columns, could be * which means all, or list of comma separated values, default to *
   */
  private String columnsString;

  /**
   * name of a column in "FROM" table that has a sequence or last updated date
   */
  private String incrementalAllColumnsColumnString;
  
  /**
   * name of a column in "FROM" table that has a sequence or last updated date
   * @return column
   */
  public String getIncrementalAllColumnsColumnString() {
    return this.incrementalAllColumnsColumnString;
  }

  /**
   * name of a column in "FROM" table that has a sequence or last updated date
   * @param incrementalAllColumnsColumnString1
   */
  public void setIncrementalAllColumnsColumnString(
      String incrementalAllColumnsColumnString1) {
    this.incrementalAllColumnsColumnString = incrementalAllColumnsColumnString1;
  }

  /**
   * primary key columns, * means all columns, or list of comma separated values, default to *
   * @return the primaryKeyColumnsString
   */
  public String getPrimaryKeyColumnsString() {
    return this.primaryKeyColumnsString;
  }

  
  /**
   * primary key columns, * means all columns, or list of comma separated values, default to *
   * @param primaryKeyColumnsString1 the primaryKeyColumnsString to set
   */
  public void setPrimaryKeyColumnsString(String primaryKeyColumnsString1) {
    this.primaryKeyColumnsString = primaryKeyColumnsString1;
  }

  
  /**
   * all columns, could be * which means all, or list of comma separated values, default to *
   * @return the columnsString
   */
  public String getColumnsString() {
    return this.columnsString;
  }

  
  /**
   * all columns, could be * which means all, or list of comma separated values, default to *
   * @param columnsString1 the columnsString to set
   */
  public void setColumnsString(String columnsString1) {
    this.columnsString = columnsString1;
  }

  /**
   * gc table sync
   */
  private GcTableSync gcTableSync;
  
  /**
   * gc table sync
   * @return table sync
   */
  public GcTableSync getGcTableSync() {
    return this.gcTableSync;
  }

  /**
   * gc table sync
   * @param gcTableSync1
   */
  public void setGcTableSync(GcTableSync gcTableSync1) {
    this.gcTableSync = gcTableSync1;
  }

  /**
   * @param debugMap
   * @param configKey
   * @param gcTableSyncSubtype
   */
  public void configureTableSync(Map<String, Object> debugMap, GcTableSync theGcTableSync, String theConfigKey, GcTableSyncSubtype theGcTableSyncSubtype) {

    try {
      this.gcTableSync = theGcTableSync;
      
      this.setConfigKey(theConfigKey);
      this.setGcTableSyncSubtype(theGcTableSyncSubtype);

      String defaultStatusDatabase = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTableDefault.statusDatabase", "grouper");
      // grouperClient.syncTable.personSource.statusDatabase = awsDev
      this.statusDatabase = GrouperClientUtils.defaultIfNull(this.retrieveConfigString("statusDatabase", false), defaultStatusDatabase);
      if (!StringUtils.equals(defaultStatusDatabase, this.statusDatabase)) {
        debugMap.put("configStatusDatabase", this.statusDatabase);
      }
      
      this.gcTableSync.setGcGrouperSync(GcGrouperSync.retrieveOrCreateByProvisionerName(defaultStatusDatabase, configKey));
      if (!GrouperClientUtils.equals(GcGrouperSync.SQL_SYNC_ENGINE, this.gcTableSync.getGcGrouperSync().getSyncEngine())) {
        this.gcTableSync.getGcGrouperSync().setSyncEngine(GcGrouperSync.SQL_SYNC_ENGINE);
        this.gcTableSync.getGcGrouperSync().store();
      }

      this.gcTableSync.setGcGrouperSyncJob(this.gcTableSync.getGcGrouperSync().retrieveJobOrCreateBySyncType(gcTableSyncSubtype.name()));
      this.gcTableSync.setGcGrouperSyncLog(this.gcTableSync.getGcGrouperSyncJob().retrieveGrouperSyncLogOrCreate());
      this.gcTableSync.getGcGrouperSyncLog().setSyncTimestamp(new Timestamp(System.currentTimeMillis()));
    
      // must have key
      if (StringUtils.isBlank(this.configKey)) {
        throw new RuntimeException("Why is config key blank?");
      }
  
      if (this.gcTableSyncSubtype == null) {
        throw new RuntimeException("Why is table sync subtype blank?");
      }
  
      if (debugMap == null) {
        debugMap = new LinkedHashMap<String, Object>();
      }
      
      //  grouperClient.syncTable.personSource.databaseFrom = pcom
      this.databaseFrom = GrouperClientUtils.defaultIfBlank(this.retrieveConfigString("databaseFrom", false), "grouper");
      debugMap.put("configDatabaseFrom", this.databaseFrom);
  
      //  grouperClient.syncTable.personSource.databaseTo = awsDev
      this.databaseTo = GrouperClientUtils.defaultIfBlank(this.retrieveConfigString("databaseTo", false), "grouper");
      debugMap.put("configDatabaseTo", this.databaseTo);
      
      // grouper client or loader database key where copying data to, large queries go against different database
      this.databaseToReadonly = this.retrieveConfigString("databaseToReadonly", false);
      if (!StringUtils.isBlank(this.databaseToReadonly)) {
        debugMap.put("configDatabaseToReadonly", this.databaseToReadonly);
      }
  
      //  grouperClient.syncTable.personSource.tableFrom = PERSON_SOURCE_TEMP
      this.tableFrom = this.retrieveConfigString("tableFrom", true);
      debugMap.put("configTableFrom", this.tableFrom);
  
      //  grouperClient.syncTable.personSource.tableTo = PERSON_SOURCE_TEMP
      this.tableTo = GrouperClientUtils.defaultIfBlank(this.retrieveConfigString("tableTo", false), this.tableFrom);
      debugMap.put("configTableTo", this.tableTo);
      
      //  grouperClient.syncTable.personSource.columns = *
      this.columnsString = GrouperClientUtils.defaultIfBlank(this.retrieveConfigString("columns", false), "*");
      debugMap.put("configColumns", this.columnsString);
  
      //  grouperClient.syncTable.personSource.primaryKeyColumns = penn_id
      this.primaryKeyColumnsString = this.retrieveConfigString("primaryKeyColumns", true);
      debugMap.put("configPrimaryKeyColumns", this.primaryKeyColumnsString);
  
      // grouperClient.syncTable.personSource.fullSyncChangeFlagColumn = check_sum
      this.changeFlagColumnString = this.retrieveConfigString("changeFlagColumn", this.gcTableSyncSubtype == GcTableSyncSubtype.fullSyncChangeFlag);
      if (!GrouperClientUtils.isBlank(this.changeFlagColumnString)) {
        debugMap.put("changeFlagColumn", this.changeFlagColumnString);
      }
  
      // grouperClient.syncTable.personSource.incrementalAllColumnsColumn = last_updated
      this.incrementalAllColumnsColumnString = this.retrieveConfigString("incrementalAllColumnsColumn", this.gcTableSyncSubtype == GcTableSyncSubtype.incrementalAllColumns);
      if (!GrouperClientUtils.isBlank(this.incrementalProgressColumnString)) {
        debugMap.put("incrementalAllColumnsColumn", this.incrementalProgressColumnString);
      }
      
      
      // grouperClient.syncTable.personSource.incrementalAllColumnsColumn = last_updated
      this.incrementalProgressColumnString = this.retrieveConfigString("incrementalProgressColumn", this.gcTableSyncSubtype == GcTableSyncSubtype.incrementalPrimaryKey);
      if (!GrouperClientUtils.isBlank(this.incrementalProgressColumnString)) {
        debugMap.put("incrementalProgressColumn", this.incrementalProgressColumnString);
      }
      
      //  grouperClient.syncTable.personSource.groupColumn = penn_id
      this.groupColumnString = this.retrieveConfigString("groupingColumn", false);
      if (this.gcTableSyncSubtype.isNeedsGroupColumn() ) {
  //      if (GrouperClientUtils.isBlank(this.groupColumnString) && !this.primaryKeyColumnsString.contains(",") && !this.primaryKeyColumnsString.equals("*")) {
  //        this.groupColumnString = this.primaryKeyColumnsString;
  //      }
        if (GrouperClientUtils.isBlank(this.groupColumnString)) {
          throw new RuntimeException("groupingColumn is required for " + this.configKey);
        }
      }
      if (!GrouperClientUtils.isBlank(this.groupColumnString)) {
        debugMap.put("configGroupingColumn", this.groupColumnString);
      }
      
  
      int defaultBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.batchSize", 800);
      // grouperClient.syncTable.personSource.batchSize = 50
      // size of jdbc batches
      this.batchSize = GrouperClientUtils.defaultIfNull(this.retrieveConfigInt("batchSize", false), defaultBatchSize);
      if (this.batchSize != defaultBatchSize) {
        debugMap.put("configBatchSize", this.batchSize);
      }
      
      
      int defaultMaxBindVarsInSelect = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
      // grouperClient.syncTable.personSource.maxBindVarsInSelect = 900
      // number of bind vars in select
      this.maxBindVarsInSelect = GrouperClientUtils.defaultIfNull(this.retrieveConfigInt("maxBindVarsInSelect", false), defaultMaxBindVarsInSelect);
      if (this.maxBindVarsInSelect < 1) {
        throw new RuntimeException("Cant have less than one maxBindVarsInSelect! " + this.maxBindVarsInSelect );
      }
      if (this.maxBindVarsInSelect != defaultMaxBindVarsInSelect) {
        debugMap.put("maxBindVarsInSelect", this.maxBindVarsInSelect);
      }
      
      // grouperClient.syncTable.personSource.groupingSize = 10000
      // the group column is what is uniquely selected, and then batched through to get data, defaults to 10000
      // size of jdbc batches
      int defaultGroupingSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.groupingSize", 10000);
      this.groupingSize = GrouperClientUtils.defaultIfNull(this.retrieveConfigInt("groupingSize", false), defaultGroupingSize);
      if (this.groupingSize != defaultGroupingSize) {
        debugMap.put("configGroupingSize", this.groupingSize);
      }
  
      // grouperClient.syncTable.personSource.incrementalPrimaryKeyTable = real_time_table
      this.incrementalPrimaryKeyTable = this.retrieveConfigString("incrementalPrimaryKeyTable", false);
      if (!GrouperClientUtils.isBlank(this.incrementalPrimaryKeyTable)) {
        debugMap.put("incrementalPrimaryKeyTable", this.incrementalPrimaryKeyTable);
      }
  
      // grouperClient.syncTableDefault.switchFromIncrementalToFullIfOverRecords = 300000
      // if this is less than 0, then it will not switch from incremental to full
      // default switch from incremental to full if the number of incrementals is over this threshold
      int defaultSwitchFromIncrementalToFullIfOverRecords = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.switchFromIncrementalToFullIfOverRecords", 300000);
      this.switchFromIncrementalToFullIfOverRecords = GrouperClientUtils.defaultIfNull(this.retrieveConfigInt("switchFromIncrementalToFullIfOverRecords", false), defaultSwitchFromIncrementalToFullIfOverRecords);
      if (this.switchFromIncrementalToFullIfOverRecords != defaultSwitchFromIncrementalToFullIfOverRecords) {
        debugMap.put("configSwitchFromIncrementalToFullIfOverRecords", this.switchFromIncrementalToFullIfOverRecords);
      }

      // # switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
      // # fullSyncChangeFlag, fullSyncFull, fullSyncGroups
      GcTableSyncSubtype defaultSwitchFromIncrementalToFullSubtype = GcTableSyncSubtype.valueOfIgnoreCase(
          GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTableDefault.switchFromIncrementalToFullSubtype", "fullSyncFull"), false);
      this.switchFromIncrementalToFullSubtype = GcTableSyncSubtype.valueOfIgnoreCase(
          GrouperClientUtils.defaultIfNull(this.retrieveConfigString("switchFromIncrementalToFullSubtype", false), 
              defaultSwitchFromIncrementalToFullSubtype == null ? null : defaultSwitchFromIncrementalToFullSubtype.name()), false);
      if (defaultSwitchFromIncrementalToFullSubtype != this.switchFromIncrementalToFullSubtype) {
        debugMap.put("configSwitchFromIncrementalToFullSubtype", this.switchFromIncrementalToFullSubtype);
      }
      if (this.switchFromIncrementalToFullSubtype != null && !this.switchFromIncrementalToFullSubtype.isFullSync()) {
        
        throw new RuntimeException("defaultSwitchFromIncrementalToFullSubtype is not a full sync! " + this.switchFromIncrementalToFullSubtype);
        
      }

      // switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
      // if this is less than 0, then it will not switch from incremental to full
      int defaultSwitchFromIncrementalToGroupIfOverRecordsInGroup = GrouperClientConfig.retrieveConfig()
          .propertyValueInt("grouperClient.syncTableDefault.switchFromIncrementalToGroupIfOverRecordsInGroup", 50000);
      this.switchFromIncrementalToGroupIfOverRecordsInGroup = GrouperClientUtils.defaultIfNull(
          this.retrieveConfigInt("switchFromIncrementalToGroupIfOverRecordsInGroup", false), defaultSwitchFromIncrementalToGroupIfOverRecordsInGroup);
      if (this.switchFromIncrementalToGroupIfOverRecordsInGroup != defaultSwitchFromIncrementalToGroupIfOverRecordsInGroup) {
        debugMap.put("configSwitchFromIncrementalToGroupIfOverRecordsInGroup", this.switchFromIncrementalToGroupIfOverRecordsInGroup);
      }

      // switch from incremental to full if the number of groups (and records over threshold) is over this threshold
      // i.e. needs to be over 100 groups and over 300000 records
      int defaultSwitchFromIncrementalToFullIfOverGroupCount = GrouperClientConfig.retrieveConfig()
          .propertyValueInt("grouperClient.syncTableDefault.switchFromIncrementalToFullIfOverGroupCount", 100);
      this.switchFromIncrementalToFullIfOverGroupCount = GrouperClientUtils.defaultIfNull(
          this.retrieveConfigInt("switchFromIncrementalToFullIfOverGroupCount", false), defaultSwitchFromIncrementalToFullIfOverGroupCount);
      if (this.switchFromIncrementalToFullIfOverGroupCount != defaultSwitchFromIncrementalToFullIfOverGroupCount) {
        debugMap.put("configSwitchFromIncrementalToFullIfOverGroupCount", this.switchFromIncrementalToFullIfOverGroupCount);
      }
      
    } catch (RuntimeException re) {
      if (this.gcTableSync != null && this.gcTableSync.getGcGrouperSyncLog() != null) {
        try {
          this.gcTableSync.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.CONFIG_ERROR);
          this.gcTableSync.getGcGrouperSyncLog().store();
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
  private GcTableSyncSubtype switchFromIncrementalToFullSubtype;

  /**
   * switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
   * fullSyncChangeFlag, fullSyncFull, fullSyncGroups
   * @return type
   */
  public GcTableSyncSubtype getSwitchFromIncrementalToFullSubtype() {
    return this.switchFromIncrementalToFullSubtype;
  }

  /**
   * switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
   * fullSyncChangeFlag, fullSyncFull, fullSyncGroups
   * @param switchFromIncrementalToFullSubtype1
   */
  public void setSwitchFromIncrementalToFullSubtype(
      GcTableSyncSubtype switchFromIncrementalToFullSubtype1) {
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
   * table where real time primary key and last_updated col is
   * @return the realTimeTable
   */
  public String getIncrementalPrimaryKeyTable() {
    return this.incrementalPrimaryKeyTable;
  }

  /**
   * @return the statusDatabase
   */
  public String getStatusDatabase() {
    return this.statusDatabase;
  }

  /**
   * table name from
   * @return the tableName
   */
  public String getTableFrom() {
    return this.tableFrom;
  }

  /**
   * table name to
   * @return the tableNameTo
   */
  public String getTableTo() {
    return this.tableTo;
  }

  /**
   * group column
   * @return the groupColumn
   */
  public String getGroupColumnString() {
    return this.groupColumnString;
  }

  /**
   * database from key
   * @param databaseFrom1 the databaseFrom to set
   */
  public void setDatabaseFrom(String databaseFrom1) {
    this.databaseFrom = databaseFrom1;
  }

  /**
   * database to key
   * @param databaseTo1 the databaseTo to set
   */
  public void setDatabaseTo(String databaseTo1) {
    this.databaseTo = databaseTo1;
  }

  /**
   * @param groupColumn1 the groupColumn to set
   */
  public void setGroupColumnString(String groupColumn1) {
    this.groupColumnString = groupColumn1;
  }

  /**
   * @param groupSize1 the groupSize to set
   */
  public void setGroupingSize(int groupSize1) {
    this.groupingSize = groupSize1;
  }

  /**
   * key in config that points to this instance of table sync
   * @param key1 the key to set
   */
  public void setConfigKey(String key1) {
    this.configKey = key1;
  }

  /**
   * table where real time primary key and last_updated col is
   * @param realTimeTable1 the realTimeTable to set
   */
  public void setIncrementalPrimaryKeyTable(String realTimeTable1) {
    this.incrementalPrimaryKeyTable = realTimeTable1;
  }

  /**
   * grouperClient.syncTable.personSource.statusDatabase = awsDev
   * @param statusDatabase1 the statusDatabase to set
   */
  public void setStatusDatabase(String statusDatabase1) {
    this.statusDatabase = statusDatabase1;
  }

  /**
   * table name from
   * @param tableName1 the tableName to set
   */
  public void setTableFrom(String tableName1) {
    this.tableFrom = tableName1;
  }

  /**
   * table name to
   * @param tableNameTo1 the tableNameTo to set
   */
  public void setTableTo(String tableNameTo1) {
    this.tableTo = tableNameTo1;
  }


}
