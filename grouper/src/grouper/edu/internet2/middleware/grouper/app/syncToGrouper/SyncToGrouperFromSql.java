package edu.internet2.middleware.grouper.app.syncToGrouper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

/**
 * 
 * @author mchyzer
 */
public class SyncToGrouperFromSql {

  public SyncToGrouperFromSql() {
    super();
  }

  public SyncToGrouperFromSql(SyncToGrouper syncToGrouper) {
    super();
    this.syncToGrouper = syncToGrouper;
  }

  private SyncToGrouper syncToGrouper = null;
  
  public SyncToGrouper getSyncToGrouper() {
    return syncToGrouper;
  }
  
  public void setSyncToGrouper(SyncToGrouper syncToGrouper) {
    this.syncToGrouper = syncToGrouper;
  }

  /**
   * database external system config id
   */
  private String databaseConfigId;
  
  
  
  /**
   * database external system config id
   * @return
   */
  public String getDatabaseConfigId() {
    return databaseConfigId;
  }

  /**
   * database external system config id
   * @param databaseConfigId
   */
  public void setDatabaseConfigId(String databaseConfigId) {
    this.databaseConfigId = databaseConfigId;
  }

  /**
   * sql to get stems
   */
  private String stemSql;

  /**
   * sql to get stems
   * @return
   */
  public String getStemSql() {
    return stemSql;
  }

  /**
   * sql to get stems
   * @param stemSql
   */
  public void setStemSql(String stemSql) {
    this.stemSql = stemSql;
  }

  /**
   * 
   */
  public void loadStemDataFromSql() {
    
    GrouperUtil.assertion(StringUtils.isNotBlank(this.databaseConfigId), "Database config ID is required!");
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSync()) {
      
      GcDbAccess stemDbAccess = new GcDbAccess().connectionName(this.databaseConfigId);
      List<Object> bindVars = new ArrayList<Object>();
      if (StringUtils.isBlank(this.stemSql) && this.syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {

        this.stemSql = buildGrouperStemQuery(bindVars);
        
        stemDbAccess.bindVars(bindVars);
      }
      
      GrouperUtil.assertion(StringUtils.isNotBlank(this.stemSql), "If syncing stems, then stemSql is required!");

      try {
        GcTableSyncTableMetadata gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(this.databaseConfigId, this.stemSql, bindVars);
  
        GcTableSyncColumnMetadata nameColumn = gcTableSyncTableMetadata.lookupColumn("name", true);
        GcTableSyncColumnMetadata idColumn = 
            gcTableSyncTableMetadata.lookupColumn("id", this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdOnInsert());
        GcTableSyncColumnMetadata descriptionColumn = 
            gcTableSyncTableMetadata.lookupColumn("description", this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDescription());
        GcTableSyncColumnMetadata displayNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("display_name", this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDisplayName());
        GcTableSyncColumnMetadata alternateNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("alternate_name", this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldAlternateName());
        GcTableSyncColumnMetadata idIndexColumn =
            gcTableSyncTableMetadata.lookupColumn("id_index", this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdIndexOnInsert());
        
        List<Object[]> stemArrayList = stemDbAccess.sql(this.stemSql).selectList(Object[].class);
            
        List<SyncStemToGrouperBean> syncStemToGrouperBeans = new ArrayList<SyncStemToGrouperBean>();
        for (Object[] stemArray : GrouperUtil.nonNull(stemArrayList)) {
          String name = (String)stemArray[nameColumn.getColumnIndexZeroIndexed()];
          SyncStemToGrouperBean syncStemToGrouperBean = new SyncStemToGrouperBean();
          syncStemToGrouperBean.assignName(name);
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdOnInsert()) {
            String id = (String)stemArray[idColumn.getColumnIndexZeroIndexed()];
            if (!StringUtils.isBlank(id)) {
              syncStemToGrouperBean.assignId(id);
            }
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDescription()) {
            String description = (String)stemArray[descriptionColumn.getColumnIndexZeroIndexed()];
            syncStemToGrouperBean.assignDescription(description);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDisplayName()) {
            String displayName = (String)stemArray[displayNameColumn.getColumnIndexZeroIndexed()];
            syncStemToGrouperBean.assignDisplayName(displayName);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldAlternateName()) {
            String alternateName = (String)stemArray[alternateNameColumn.getColumnIndexZeroIndexed()];
            syncStemToGrouperBean.assignAlternateName(alternateName);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdIndexOnInsert()) {
            Long idIndex = GrouperUtil.longObjectValue(stemArray[idIndexColumn.getColumnIndexZeroIndexed()], true);
            syncStemToGrouperBean.assignIdIndex(idIndex);
          }
          
          syncStemToGrouperBeans.add(syncStemToGrouperBean);
        }
        this.syncToGrouper.setSyncStemToGrouperBeans(syncStemToGrouperBeans);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "sql: '" + this.stemSql + "', ");
        throw re;
      }
    }
    
  }

  /**
   * 
   */
  private String buildGrouperStemQuery(List<Object> bindVars) {
    StringBuilder theStemSql = new StringBuilder("select name");
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdOnInsert()) {
      theStemSql.append(", id");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldIdIndexOnInsert()) {
      theStemSql.append(", id_index");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDisplayName()) {
      theStemSql.append(", display_name");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldDescription()) {
      theStemSql.append(", description");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isStemSyncFieldAlternateName()) {
      theStemSql.append(", alternate_name");
    }
    theStemSql.append(" from ");
    if (!StringUtils.isBlank(this.databaseSyncFromAnotherGrouperSchema)) {
      theStemSql.append(StringUtils.trim(this.databaseSyncFromAnotherGrouperSchema));
      if (!this.databaseSyncFromAnotherGrouperSchema.contains(".")) {
        theStemSql.append(".");
      }
    }
    theStemSql.append("grouper_stems gs");
    GrouperUtil.assertion(GrouperUtil.length(this.databaseSyncFromAnotherGrouperTopLevelStems) > 0, 
        "If syncing grouper and folders then the top level folders are required or : for all");
    
    Set<String> topLevelStemSet = new TreeSet<String>(this.databaseSyncFromAnotherGrouperTopLevelStems);
    if (!topLevelStemSet.contains(":")) {
      
      topLevelStemSet = GrouperUtil.stemCalculateTopLevelStems(topLevelStemSet);

      boolean addedOne = false;
      
      GrouperUtil.assertion(GrouperUtil.length(topLevelStemSet) < 400, "Cannot have more than 400 top level stems to sync");
      
      for (String topLevelStemName : topLevelStemSet) {
        if (!addedOne) {
          theStemSql.append(" where ");
        } else {
          theStemSql.append(" or ");
        }
        addedOne = true;
        // the exact name or the children
        theStemSql.append("name = ? or name like ?");
        bindVars.add(topLevelStemName);
        bindVars.add(topLevelStemName + ":%");
      }
      
    }
    return theStemSql.toString();
  }

  /**
   * top level stems to sync from another grouper by sql
   */
  private List<String> databaseSyncFromAnotherGrouperTopLevelStems = new ArrayList<String>();

  /**
   * top level stems to sync from another grouper schema to use
   */
  private String databaseSyncFromAnotherGrouperSchema = null;

  /**
   * sql to get groups
   */
  private String groupSql;

  /**
   * sql to get composites
   */
  private String compositeSql;

  /**
   * sql to get memberships
   */
  private String membershipSql;

  /**
   * sql to get privilege groups
   */
  private String privilegeGroupSql;

  /**
   * sql to get privilege stems
   */
  private String privilegeStemSql;

  /**
   * sql to get privilege stems
   * @return
   */
  public String getPrivilegeStemSql() {
    return privilegeStemSql;
  }

  /**
   * sql to get privilege stems
   * @param privilegeStemSql
   */
  public void setPrivilegeStemSql(String privilegeStemSql) {
    this.privilegeStemSql = privilegeStemSql;
  }

  /**
   * sql to get privilege groups
   * @return
   */
  public String getPrivilegeGroupSql() {
    return privilegeGroupSql;
  }

  /**
   * sql to get privilege groups
   * @param privilegeGroupSql
   */
  public void setPrivilegeGroupSql(String privilegeGroupSql) {
    this.privilegeGroupSql = privilegeGroupSql;
  }

  /**
   * sql to get memberships
   * @return
   */
  public String getMembershipSql() {
    return membershipSql;
  }

  /**
   * sql to get memberships
   * @param membershipSql
   */
  public void setMembershipSql(String membershipSql) {
    this.membershipSql = membershipSql;
  }

  /**
   * sql to get composites
   * @return
   */
  public String getCompositeSql() {
    return compositeSql;
  }

  /**
   * sql to get composites
   * @param compositeSql
   */
  public void setCompositeSql(String compositeSql) {
    this.compositeSql = compositeSql;
  }

  /**
   * sql to get groups
   * @return
   */
  public String getGroupSql() {
    return groupSql;
  }

  /**
   * sql to get groups
   * @param groupSql
   */
  public void setGroupSql(String groupSql) {
    this.groupSql = groupSql;
  }

  /**
   * top level stems to sync from another grouper schema to use
   * @return sync
   */
  public String getDatabaseSyncFromAnotherGrouperSchema() {
    return databaseSyncFromAnotherGrouperSchema;
  }

  /**
   * top level stems to sync from another grouper schema to use
   * @param databaseSyncFromAnotherGrouperSchema
   */
  public void setDatabaseSyncFromAnotherGrouperSchema(
      String databaseSyncFromAnotherGrouperSchema) {
    this.databaseSyncFromAnotherGrouperSchema = databaseSyncFromAnotherGrouperSchema;
  }

  /**
   * top level stems to sync from another grouper by sql
   * @param list
   */
  public void setDatabaseSyncFromAnotherGrouperTopLevelStems(List<String> list) {
    
    this.databaseSyncFromAnotherGrouperTopLevelStems = list;
    
  }

  /**
   * top level stems to sync from another grouper by sql
   * @return
   */
  public List<String> getDatabaseSyncFromAnotherGrouperTopLevelStems() {
    return databaseSyncFromAnotherGrouperTopLevelStems;
  }

  /**
   * 
   */
  private String buildGrouperGroupQuery(List<Object> bindVars) {
    StringBuilder theGroupSql = new StringBuilder("select name");
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldAlternateName()) {
      theGroupSql.append(", alternate_name");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDescription()) {
      theGroupSql.append(", description");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisabledTimestamp()) {
      theGroupSql.append(", disabled_timestamp");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisplayName()) {
      theGroupSql.append(", display_name");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldEnabledTimestamp()) {
      theGroupSql.append(", enabled_timestamp");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdOnInsert()) {
      theGroupSql.append(", id");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdIndexOnInsert()) {
      theGroupSql.append(", id_index");
    }
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldTypeOfGroup()) {
      theGroupSql.append(", type_of_group");
    }
    theGroupSql.append(" from ");
    if (!StringUtils.isBlank(this.databaseSyncFromAnotherGrouperSchema)) {
      theGroupSql.append(StringUtils.trim(this.databaseSyncFromAnotherGrouperSchema));
      if (!this.databaseSyncFromAnotherGrouperSchema.contains(".")) {
        theGroupSql.append(".");
      }
    }
    theGroupSql.append("grouper_groups gg");
    GrouperUtil.assertion(GrouperUtil.length(this.databaseSyncFromAnotherGrouperTopLevelStems) > 0, 
        "If syncing grouper and groups then the top level folders are required or : for all");
    
    Set<String> topLevelStemSet = new TreeSet<String>(this.databaseSyncFromAnotherGrouperTopLevelStems);
    if (!topLevelStemSet.contains(":")) {
      
      topLevelStemSet = GrouperUtil.stemCalculateTopLevelStems(topLevelStemSet);
  
      boolean addedOne = false;
      
      GrouperUtil.assertion(GrouperUtil.length(topLevelStemSet) < 400, "Cannot have more than 400 top level stems to sync");
      
      for (String topLevelStemName : topLevelStemSet) {
        if (!addedOne) {
          theGroupSql.append(" where ");
        } else {
          theGroupSql.append(" or ");
        }
        addedOne = true;
        // the exact name or the children
        theGroupSql.append("gg.name like ?");
        bindVars.add(topLevelStemName + ":%");
      }
    }
    return theGroupSql.toString();
  }

  /**
   * 
   */
  public void loadGroupDataFromSql() {
    
    GrouperUtil.assertion(StringUtils.isNotBlank(this.databaseConfigId), "Database config ID is required!");
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSync()) {
      
      GcDbAccess groupDbAccess = new GcDbAccess().connectionName(this.databaseConfigId);
      List<Object> bindVars = new ArrayList<Object>();
      if (StringUtils.isBlank(this.groupSql) && this.syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
  
        this.groupSql = buildGrouperGroupQuery(bindVars);
        
        groupDbAccess.bindVars(bindVars);
      }
      
      GrouperUtil.assertion(StringUtils.isNotBlank(this.groupSql), "If syncing group, then groupSql is required!");
  
      try {
        GcTableSyncTableMetadata gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(this.databaseConfigId, this.groupSql, bindVars);
  
        GcTableSyncColumnMetadata nameColumn = gcTableSyncTableMetadata.lookupColumn("name", true);
        GcTableSyncColumnMetadata alternateNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("alternate_name", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldAlternateName());
        GcTableSyncColumnMetadata descriptionColumn = 
            gcTableSyncTableMetadata.lookupColumn("description", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDescription());
        GcTableSyncColumnMetadata disabledTimestampColumn = 
            gcTableSyncTableMetadata.lookupColumn("disabled_timestamp", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisabledTimestamp());
        GcTableSyncColumnMetadata displayNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("display_name", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisplayName());
        GcTableSyncColumnMetadata enabledTimestampColumn = 
            gcTableSyncTableMetadata.lookupColumn("enabled_timestamp", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldEnabledTimestamp());
        GcTableSyncColumnMetadata idColumn = 
            gcTableSyncTableMetadata.lookupColumn("id", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdOnInsert());
        GcTableSyncColumnMetadata idIndexColumn =
            gcTableSyncTableMetadata.lookupColumn("id_index", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdIndexOnInsert());
        GcTableSyncColumnMetadata typeOfGroupColumn =
            gcTableSyncTableMetadata.lookupColumn("type_of_group", this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldTypeOfGroup());

        List<Object[]> groupArrayList = groupDbAccess.sql(this.groupSql).selectList(Object[].class);

        List<SyncGroupToGrouperBean> syncGroupToGrouperBeans = new ArrayList<SyncGroupToGrouperBean>();
        for (Object[] groupArray : GrouperUtil.nonNull(groupArrayList)) {
          String name = (String)groupArray[nameColumn.getColumnIndexZeroIndexed()];
          SyncGroupToGrouperBean syncGroupToGrouperBean = new SyncGroupToGrouperBean();
          syncGroupToGrouperBean.assignName(name);
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldAlternateName()) {
            String alternateName = (String)groupArray[alternateNameColumn.getColumnIndexZeroIndexed()];
            syncGroupToGrouperBean.assignAlternateName(alternateName);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDescription()) {
            String description = (String)groupArray[descriptionColumn.getColumnIndexZeroIndexed()];
            syncGroupToGrouperBean.assignDescription(description);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisabledTimestamp()) {
            Long disabledTimestamp = GrouperUtil.longObjectValue(groupArray[disabledTimestampColumn.getColumnIndexZeroIndexed()], true);
            syncGroupToGrouperBean.assignDisabledTimestamp(disabledTimestamp);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldDisplayName()) {
            String displayName = (String)groupArray[displayNameColumn.getColumnIndexZeroIndexed()];
            syncGroupToGrouperBean.assignDisplayName(displayName);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldEnabledTimestamp()) {
            Long enabledTimestamp = GrouperUtil.longObjectValue(groupArray[enabledTimestampColumn.getColumnIndexZeroIndexed()], true);
            syncGroupToGrouperBean.assignEnabledTimestamp(enabledTimestamp);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdOnInsert()) {
            String id = (String)groupArray[idColumn.getColumnIndexZeroIndexed()];
            if (!StringUtils.isBlank(id)) {
              syncGroupToGrouperBean.assignId(id);
            }
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldIdIndexOnInsert()) {
            Long idIndex = GrouperUtil.longObjectValue(groupArray[idIndexColumn.getColumnIndexZeroIndexed()], true);
            syncGroupToGrouperBean.assignIdIndex(idIndex);
          }
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isGroupSyncFieldTypeOfGroup()) {
            String typeOfGroup = (String)groupArray[typeOfGroupColumn.getColumnIndexZeroIndexed()];
            syncGroupToGrouperBean.assignTypeOfGroup(typeOfGroup);
          }
          
          syncGroupToGrouperBeans.add(syncGroupToGrouperBean);
        }
        this.syncToGrouper.setSyncGroupToGrouperBeans(syncGroupToGrouperBeans);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "sql: '" + this.groupSql + "', ");
        throw re;
      }
    }
    
  }

  /**
   * 
   */
  public void loadCompositeDataFromSql() {
    
    GrouperUtil.assertion(StringUtils.isNotBlank(this.databaseConfigId), "Database config ID is required!");
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeSync()) {
      
      GcDbAccess compositeDbAccess = new GcDbAccess().connectionName(this.databaseConfigId);
      List<Object> bindVars = new ArrayList<Object>();
      if (StringUtils.isBlank(this.compositeSql) && this.syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
  
        this.compositeSql = buildGrouperCompositeQuery(bindVars);
        
        compositeDbAccess.bindVars(bindVars);
      }
      
      GrouperUtil.assertion(StringUtils.isNotBlank(this.compositeSql), "If syncing composites, then compositeSql is required!");
  
      try {
        GcTableSyncTableMetadata gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(this.databaseConfigId, this.compositeSql, bindVars);
        
        GcTableSyncColumnMetadata idColumn = 
            gcTableSyncTableMetadata.lookupColumn("id", this.syncToGrouper.getSyncToGrouperBehavior().isCompositeSyncFieldIdOnInsert());
        GcTableSyncColumnMetadata ownerNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("owner_name", true);
        GcTableSyncColumnMetadata leftFactorNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("left_factor_name", true);
        GcTableSyncColumnMetadata rightFactorNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("right_factor_name", true);
        GcTableSyncColumnMetadata typeColumn = 
            gcTableSyncTableMetadata.lookupColumn("type", true);
        
        List<Object[]> comopsiteArrayList = compositeDbAccess.sql(this.compositeSql).selectList(Object[].class);
            
        List<SyncCompositeToGrouperBean> syncCompositeToGrouperBeans = new ArrayList<SyncCompositeToGrouperBean>();
        for (Object[] compositeArray : GrouperUtil.nonNull(comopsiteArrayList)) {
          String ownerName = (String)compositeArray[ownerNameColumn.getColumnIndexZeroIndexed()];
          String leftFactorName = (String)compositeArray[leftFactorNameColumn.getColumnIndexZeroIndexed()];
          String rightFactorName = (String)compositeArray[rightFactorNameColumn.getColumnIndexZeroIndexed()];
          String type = (String)compositeArray[typeColumn.getColumnIndexZeroIndexed()];
          SyncCompositeToGrouperBean syncCompositeToGrouperBean = new SyncCompositeToGrouperBean();
          syncCompositeToGrouperBean.assignOwnerName(ownerName);
          syncCompositeToGrouperBean.assignLeftFactorName(leftFactorName);
          syncCompositeToGrouperBean.assignRightFactorName(rightFactorName);
          syncCompositeToGrouperBean.assignType(type);
          
          if (this.syncToGrouper.getSyncToGrouperBehavior().isCompositeSyncFieldIdOnInsert()) {
            String id = (String)compositeArray[idColumn.getColumnIndexZeroIndexed()];
            if (!StringUtils.isBlank(id)) {
              syncCompositeToGrouperBean.assignId(id);
            }
          }
          
          syncCompositeToGrouperBeans.add(syncCompositeToGrouperBean);
        }
        this.syncToGrouper.setSyncCompositeToGrouperBeans(syncCompositeToGrouperBeans);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "sql: '" + this.compositeSql + "', ");
        throw re;
      }
    }
    
  }

  /**
   * 
   */
  private String buildGrouperCompositeQuery(List<Object> bindVars) {

    String schema = "";
    if (!StringUtils.isBlank(this.databaseSyncFromAnotherGrouperSchema)) {
      schema = StringUtils.trim(this.databaseSyncFromAnotherGrouperSchema);
      if (!this.databaseSyncFromAnotherGrouperSchema.contains(".")) {
        schema += ".";
      }
    }

    StringBuilder theCompositeSql = new StringBuilder(
        "SELECT group_owner.name AS owner_name, group_left_factor.name AS left_factor_name, group_right_factor.name AS right_factor_name, gc.type "
        + "FROM " + schema + "grouper_composites gc, " + schema + "grouper_groups group_owner, " + schema + "grouper_groups group_left_factor, "
            + schema + "grouper_groups group_right_factor "
        + "WHERE gc.owner = group_owner.id AND gc.left_factor = group_left_factor.id AND gc.right_factor = group_right_factor.id");

    GrouperUtil.assertion(GrouperUtil.length(this.databaseSyncFromAnotherGrouperTopLevelStems) > 0, 
        "If syncing grouper and folders then the top level folders are required or : for all");
    
    Set<String> topLevelStemSet = new TreeSet<String>(this.databaseSyncFromAnotherGrouperTopLevelStems);
    if (!topLevelStemSet.contains(":")) {
      
      topLevelStemSet = GrouperUtil.stemCalculateTopLevelStems(topLevelStemSet);
  
      boolean addedOne = false;
      
      GrouperUtil.assertion(GrouperUtil.length(topLevelStemSet) < 400, "Cannot have more than 400 top level stems to sync");
      theCompositeSql.append(" and ( ");
      for (String topLevelStemName : topLevelStemSet) {
        if (addedOne) {
          theCompositeSql.append(" or ");
        }
        addedOne = true;
        // the exact name or the children
        theCompositeSql.append("group_owner.name like ?");
        bindVars.add(topLevelStemName + ":%");
      }
      theCompositeSql.append(" ) ");
    }
    return theCompositeSql.toString();
  }

  /**
   * 
   */
  private String buildGrouperMembershipQuery(List<Object> bindVars) {
  
    String schema = "";
    if (!StringUtils.isBlank(this.databaseSyncFromAnotherGrouperSchema)) {
      schema = StringUtils.trim(this.databaseSyncFromAnotherGrouperSchema);
      if (!this.databaseSyncFromAnotherGrouperSchema.contains(".")) {
        schema += ".";
      }
    }
    
    StringBuilder theMembershipSql = new StringBuilder(
        "SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier");
    
    if (this.getSyncToGrouper().getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled()) {
      theMembershipSql.append(", gmav.immediate_mship_disabled_time, gmav.immediate_mship_enabled_time");
    }

    theMembershipSql.append(" FROM " + schema + "grouper_memberships_all_v gmav, " + schema + "grouper_members gm, " + schema + "grouper_groups gg, " + schema + "grouper_fields gf "
        + "WHERE gmav.mship_type = 'immediate'");
    
    if (!this.getSyncToGrouper().getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled()) {
      theMembershipSql.append(" AND gmav.immediate_mship_enabled = 'T'");
    }
    theMembershipSql.append(" AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.name = 'members'");
          
    GrouperUtil.assertion(GrouperUtil.length(this.databaseSyncFromAnotherGrouperTopLevelStems) > 0, 
        "If syncing grouper and folders then the top level folders are required or : for all");
    
    Set<String> topLevelStemSet = new TreeSet<String>(this.databaseSyncFromAnotherGrouperTopLevelStems);
    if (!topLevelStemSet.contains(":")) {
      
      topLevelStemSet = GrouperUtil.stemCalculateTopLevelStems(topLevelStemSet);
  
      boolean addedOne = false;
      GrouperUtil.assertion(GrouperUtil.length(topLevelStemSet) < 400, "Cannot have more than 400 top level stems to sync");
      theMembershipSql.append(" and ( ");
      for (String topLevelStemName : topLevelStemSet) {
        if (addedOne) {
          theMembershipSql.append(" or ");
        }
        addedOne = true;
        // the exact name or the children
        theMembershipSql.append("gg.name like ?");
        bindVars.add(topLevelStemName + ":%");
      }
      theMembershipSql.append(" ) ");
    }
    return theMembershipSql.toString();
  }

  /**
   * 
   */
  public void loadMembershipDataFromSql() {
    
    GrouperUtil.assertion(StringUtils.isNotBlank(this.databaseConfigId), "Database config ID is required!");
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSync()) {
      
      GcDbAccess membershipDbAccess = new GcDbAccess().connectionName(this.databaseConfigId);
      List<Object> bindVars = new ArrayList<Object>();
      if (StringUtils.isBlank(this.membershipSql) && this.syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
  
        this.membershipSql = buildGrouperMembershipQuery(bindVars);
        
        membershipDbAccess.bindVars(bindVars);
      }
      
      GrouperUtil.assertion(StringUtils.isNotBlank(this.membershipSql), "If syncing memberships, then membershipSql is required!");
  
      try {
        GcTableSyncTableMetadata gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(this.databaseConfigId, this.membershipSql, bindVars);
        
        GcTableSyncColumnMetadata immediateMembershipIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("immediate_membership_id", this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSyncFieldIdOnInsert());
        GcTableSyncColumnMetadata groupNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("group_name", true);
        GcTableSyncColumnMetadata subjectSourceIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_source_id", true);
        GcTableSyncColumnMetadata subjectIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_id", true);
        GcTableSyncColumnMetadata subjectIdentifierColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_identifier", true);
        GcTableSyncColumnMetadata immediateMshipDisabledTimeColumn = 
            gcTableSyncTableMetadata.lookupColumn("immediate_mship_disabled_time", this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled());
        GcTableSyncColumnMetadata immediateMshipEnabledTimeColumn = 
            gcTableSyncTableMetadata.lookupColumn("immediate_mship_enabled_time", this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled());
        
        List<Object[]> membershipArrayList = membershipDbAccess.sql(this.membershipSql).selectList(Object[].class);
            
        List<SyncMembershipToGrouperBean> syncMembershipToGrouperBeans = new ArrayList<SyncMembershipToGrouperBean>();
        for (Object[] membershipArray : GrouperUtil.nonNull(membershipArrayList)) {
          String immediateMembershipId = this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSyncFieldIdOnInsert() ? (String)membershipArray[immediateMembershipIdColumn.getColumnIndexZeroIndexed()] : null;
          String groupName = (String)membershipArray[groupNameColumn.getColumnIndexZeroIndexed()];
          String subjectSourceId = (String)membershipArray[subjectSourceIdColumn.getColumnIndexZeroIndexed()];
          String subjectId = (String)membershipArray[subjectIdColumn.getColumnIndexZeroIndexed()];
          String subjectIdentifier = (String)membershipArray[subjectIdentifierColumn.getColumnIndexZeroIndexed()];
          Long immediateMshipDisabledTime = null;
          Long immediateMshipEnabledTime = null;
          if (this.syncToGrouper.getSyncToGrouperBehavior().isMembershipSyncFieldsEnabledDisabled()) {
            immediateMshipDisabledTime = GrouperUtil.longObjectValue(membershipArray[immediateMshipDisabledTimeColumn.getColumnIndexZeroIndexed()], true);
            immediateMshipEnabledTime = GrouperUtil.longObjectValue(membershipArray[immediateMshipEnabledTimeColumn.getColumnIndexZeroIndexed()], true);
          }
          
          SyncMembershipToGrouperBean syncMembershipToGrouperBean = new SyncMembershipToGrouperBean();
          syncMembershipToGrouperBean.assignImmediateMembershipId(immediateMembershipId);
          syncMembershipToGrouperBean.assignGroupName(groupName);
          syncMembershipToGrouperBean.assignSubjectSourceId(subjectSourceId);
          syncMembershipToGrouperBean.assignSubjectId(subjectId);
          syncMembershipToGrouperBean.assignSubjectIdentifier(subjectIdentifier);
          syncMembershipToGrouperBean.assignImmediateMshipDisabledTime(immediateMshipDisabledTime);
          syncMembershipToGrouperBean.assignImmediateMshipEnabledTime(immediateMshipEnabledTime);
          
          syncMembershipToGrouperBeans.add(syncMembershipToGrouperBean);
        }
        this.syncToGrouper.setSyncMembershipToGrouperBeans(syncMembershipToGrouperBeans);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "sql: '" + this.membershipSql + "', ");
        throw re;
      }
    }
    
  }

  /**
   * 
   */
  private String buildGrouperPrivilegeGroupQuery(List<Object> bindVars) {
  
    String schema = "";
    if (!StringUtils.isBlank(this.databaseSyncFromAnotherGrouperSchema)) {
      schema = StringUtils.trim(this.databaseSyncFromAnotherGrouperSchema);
      if (!this.databaseSyncFromAnotherGrouperSchema.contains(".")) {
        schema += ".";
      }
    }
    
    StringBuilder thePrivilegeSql = new StringBuilder(
        "SELECT gmav.immediate_membership_id AS immediate_membership_id, gg.name AS group_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name as field_name");
    
    thePrivilegeSql.append(" FROM " + schema + "grouper_memberships_all_v gmav, " + schema + "grouper_members gm, " + schema + "grouper_groups gg, " + schema + "grouper_fields gf "
        + "WHERE gmav.mship_type = 'immediate'");
    
    thePrivilegeSql.append(" AND gmav.immediate_mship_enabled = 'T'");

    thePrivilegeSql.append(" AND gmav.owner_group_id = gg.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'access'");
          
    GrouperUtil.assertion(GrouperUtil.length(this.databaseSyncFromAnotherGrouperTopLevelStems) > 0, 
        "If syncing grouper and folders then the top level folders are required or : for all");
    
    Set<String> topLevelStemSet = new TreeSet<String>(this.databaseSyncFromAnotherGrouperTopLevelStems);
    if (!topLevelStemSet.contains(":")) {
      
      topLevelStemSet = GrouperUtil.stemCalculateTopLevelStems(topLevelStemSet);
  
      boolean addedOne = false;
      GrouperUtil.assertion(GrouperUtil.length(topLevelStemSet) < 400, "Cannot have more than 400 top level stems to sync");
      thePrivilegeSql.append(" and ( ");
      for (String topLevelStemName : topLevelStemSet) {
        if (addedOne) {
          thePrivilegeSql.append(" or ");
        }
        addedOne = true;
        // the exact name or the children
        thePrivilegeSql.append("gg.name like ?");
        bindVars.add(topLevelStemName + ":%");
      }
      thePrivilegeSql.append(" ) ");
    }
    return thePrivilegeSql.toString();
  }

  /**
   * 
   */
  public void loadPrivilegeGroupDataFromSql() {
    
    GrouperUtil.assertion(StringUtils.isNotBlank(this.databaseConfigId), "Database config ID is required!");
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupSync()) {
      
      GcDbAccess privilegeGroupDbAccess = new GcDbAccess().connectionName(this.databaseConfigId);
      List<Object> bindVars = new ArrayList<Object>();
      if (StringUtils.isBlank(this.privilegeGroupSql) && this.syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
  
        this.privilegeGroupSql = buildGrouperPrivilegeGroupQuery(bindVars);
        
        privilegeGroupDbAccess.bindVars(bindVars);
      }
      
      GrouperUtil.assertion(StringUtils.isNotBlank(this.privilegeGroupSql), "If syncing group privileges, then privilegeGroupSql is required!");
  
      try {
        GcTableSyncTableMetadata gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(this.databaseConfigId, this.privilegeGroupSql, bindVars);
        
        GcTableSyncColumnMetadata immediateMembershipIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("immediate_membership_id", this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupSyncFieldIdOnInsert());
        GcTableSyncColumnMetadata groupNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("group_name", true);
        GcTableSyncColumnMetadata subjectSourceIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_source_id", true);
        GcTableSyncColumnMetadata subjectIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_id", true);
        GcTableSyncColumnMetadata subjectIdentifierColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_identifier", true);
        GcTableSyncColumnMetadata fieldNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("field_name", true);
        
        List<Object[]> privilegeGroupArrayList = privilegeGroupDbAccess.sql(this.privilegeGroupSql).selectList(Object[].class);
            
        List<SyncPrivilegeGroupToGrouperBean> syncPrivilegeGroupToGrouperBeans = new ArrayList<SyncPrivilegeGroupToGrouperBean>();
        for (Object[] privilegeGroupArray : GrouperUtil.nonNull(privilegeGroupArrayList)) {
          String immediateMembershipId = this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeGroupSyncFieldIdOnInsert() ? (String)privilegeGroupArray[immediateMembershipIdColumn.getColumnIndexZeroIndexed()] : null;
          String groupName = (String)privilegeGroupArray[groupNameColumn.getColumnIndexZeroIndexed()];
          String subjectSourceId = (String)privilegeGroupArray[subjectSourceIdColumn.getColumnIndexZeroIndexed()];
          String subjectId = (String)privilegeGroupArray[subjectIdColumn.getColumnIndexZeroIndexed()];
          String subjectIdentifier = (String)privilegeGroupArray[subjectIdentifierColumn.getColumnIndexZeroIndexed()];
          String fieldName = (String)privilegeGroupArray[fieldNameColumn.getColumnIndexZeroIndexed()];
          
          SyncPrivilegeGroupToGrouperBean syncPrivilegeGroupToGrouperBean = new SyncPrivilegeGroupToGrouperBean();
          syncPrivilegeGroupToGrouperBean.assignImmediateMembershipId(immediateMembershipId);
          syncPrivilegeGroupToGrouperBean.assignGroupName(groupName);
          syncPrivilegeGroupToGrouperBean.assignSubjectSourceId(subjectSourceId);
          syncPrivilegeGroupToGrouperBean.assignSubjectId(subjectId);
          syncPrivilegeGroupToGrouperBean.assignSubjectIdentifier(subjectIdentifier);
          syncPrivilegeGroupToGrouperBean.assignFieldName(fieldName);
          
          syncPrivilegeGroupToGrouperBeans.add(syncPrivilegeGroupToGrouperBean);
        }
        this.syncToGrouper.setSyncPrivilegeGroupToGrouperBeans(syncPrivilegeGroupToGrouperBeans);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "sql: '" + this.privilegeGroupSql + "', ");
        throw re;
      }
    }
    
  }

  /**
   * 
   */
  private String buildGrouperPrivilegeStemQuery(List<Object> bindVars) {
  
    String schema = "";
    if (!StringUtils.isBlank(this.databaseSyncFromAnotherGrouperSchema)) {
      schema = StringUtils.trim(this.databaseSyncFromAnotherGrouperSchema);
      if (!this.databaseSyncFromAnotherGrouperSchema.contains(".")) {
        schema += ".";
      }
    }
    
    StringBuilder thePrivilegeSql = new StringBuilder(
        "SELECT gmav.immediate_membership_id AS immediate_membership_id, gs.name AS stem_name, gm.subject_source AS subject_source_id, gm.subject_id, gm.subject_identifier0 AS subject_identifier, gf.name as field_name");
    
    thePrivilegeSql.append(" FROM " + schema + "grouper_memberships_all_v gmav, " + schema + "grouper_members gm, " + schema + "grouper_stems gs, " + schema + "grouper_fields gf "
        + "WHERE gmav.mship_type = 'immediate'");
    
    thePrivilegeSql.append(" AND gmav.immediate_mship_enabled = 'T'");
  
    thePrivilegeSql.append(" AND gmav.owner_stem_id = gs.id AND gmav.member_id = gm.id AND gmav.field_id = gf.id AND gf.type = 'naming'");
          
    GrouperUtil.assertion(GrouperUtil.length(this.databaseSyncFromAnotherGrouperTopLevelStems) > 0, 
        "If syncing grouper and folders then the top level folders are required or : for all");
    
    Set<String> topLevelStemSet = new TreeSet<String>(this.databaseSyncFromAnotherGrouperTopLevelStems);
    if (!topLevelStemSet.contains(":")) {
      
      topLevelStemSet = GrouperUtil.stemCalculateTopLevelStems(topLevelStemSet);
  
      boolean addedOne = false;
      GrouperUtil.assertion(GrouperUtil.length(topLevelStemSet) < 400, "Cannot have more than 400 top level stems to sync");
      thePrivilegeSql.append(" and ( ");
      for (String topLevelStemName : topLevelStemSet) {
        if (addedOne) {
          thePrivilegeSql.append(" or ");
        }
        addedOne = true;
        // the exact name or the children
        thePrivilegeSql.append("gs.name = ? or ");
        bindVars.add(topLevelStemName);
        thePrivilegeSql.append("gs.name like ?");
        bindVars.add(topLevelStemName + ":%");
      }
      thePrivilegeSql.append(" ) ");
    }
    return thePrivilegeSql.toString();
  }

  /**
   * 
   */
  public void loadPrivilegeStemDataFromSql() {
    
    GrouperUtil.assertion(StringUtils.isNotBlank(this.databaseConfigId), "Database config ID is required!");
    
    if (this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemSync()) {
      
      GcDbAccess privilegeStemDbAccess = new GcDbAccess().connectionName(this.databaseConfigId);
      List<Object> bindVars = new ArrayList<Object>();
      if (StringUtils.isBlank(this.privilegeStemSql) && this.syncToGrouper.getSyncToGrouperBehavior().isSqlLoadFromAnotherGrouper()) {
  
        this.privilegeStemSql = buildGrouperPrivilegeStemQuery(bindVars);
        
        privilegeStemDbAccess.bindVars(bindVars);
      }
      
      GrouperUtil.assertion(StringUtils.isNotBlank(this.privilegeStemSql), "If syncing stem privileges, then privilegeStemSql is required!");
  
      try {
        GcTableSyncTableMetadata gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(this.databaseConfigId, this.privilegeStemSql, bindVars);
        
        GcTableSyncColumnMetadata immediateMembershipIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("immediate_membership_id", this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemSyncFieldIdOnInsert());
        GcTableSyncColumnMetadata stemNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("stem_name", true);
        GcTableSyncColumnMetadata subjectSourceIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_source_id", true);
        GcTableSyncColumnMetadata subjectIdColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_id", true);
        GcTableSyncColumnMetadata subjectIdentifierColumn = 
            gcTableSyncTableMetadata.lookupColumn("subject_identifier", true);
        GcTableSyncColumnMetadata fieldNameColumn = 
            gcTableSyncTableMetadata.lookupColumn("field_name", true);
        
        List<Object[]> privilegeStemArrayList = privilegeStemDbAccess.sql(this.privilegeStemSql).selectList(Object[].class);
            
        List<SyncPrivilegeStemToGrouperBean> syncPrivilegeStemToGrouperBeans = new ArrayList<SyncPrivilegeStemToGrouperBean>();
        for (Object[] privilegeStemArray : GrouperUtil.nonNull(privilegeStemArrayList)) {
          String immediateMembershipId = this.syncToGrouper.getSyncToGrouperBehavior().isPrivilegeStemSyncFieldIdOnInsert() ? (String)privilegeStemArray[immediateMembershipIdColumn.getColumnIndexZeroIndexed()] : null;
          String stemName = (String)privilegeStemArray[stemNameColumn.getColumnIndexZeroIndexed()];
          String subjectSourceId = (String)privilegeStemArray[subjectSourceIdColumn.getColumnIndexZeroIndexed()];
          String subjectId = (String)privilegeStemArray[subjectIdColumn.getColumnIndexZeroIndexed()];
          String subjectIdentifier = (String)privilegeStemArray[subjectIdentifierColumn.getColumnIndexZeroIndexed()];
          String fieldName = (String)privilegeStemArray[fieldNameColumn.getColumnIndexZeroIndexed()];
          
          SyncPrivilegeStemToGrouperBean syncPrivilegeStemToGrouperBean = new SyncPrivilegeStemToGrouperBean();
          syncPrivilegeStemToGrouperBean.assignImmediateMembershipId(immediateMembershipId);
          syncPrivilegeStemToGrouperBean.assignStemName(stemName);
          syncPrivilegeStemToGrouperBean.assignSubjectSourceId(subjectSourceId);
          syncPrivilegeStemToGrouperBean.assignSubjectId(subjectId);
          syncPrivilegeStemToGrouperBean.assignSubjectIdentifier(subjectIdentifier);
          syncPrivilegeStemToGrouperBean.assignFieldName(fieldName);
          
          syncPrivilegeStemToGrouperBeans.add(syncPrivilegeStemToGrouperBean);
        }
        this.syncToGrouper.setSyncPrivilegeStemToGrouperBeans(syncPrivilegeStemToGrouperBeans);
      } catch (RuntimeException re) {
        GrouperUtil.injectInException(re, "sql: '" + this.privilegeGroupSql + "', ");
        throw re;
      }
    }
    
  }
  
}
