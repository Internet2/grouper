package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncConfiguration;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncRowData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableBean;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * does the logic to use the data from the DAOs and call the correct methods to synnc things up or dry run or send messages for async
 * @author mchyzer
 *
 */
public class GrouperProvisioningLogic {
  
  /**
   * 
   */
  public void provision() {

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.configure);

    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncStart(startTimestamp);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().provision(this.grouperProvisioner);
    
  }

  /**
   * 
   */
  public void provisionFull() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());

    {
      debugMap.put("state", "propagateProvisioningAttributes");
      long start = System.currentTimeMillis();
      grouperProvisioner.propagateProvisioningAttributes();
      long propagateProvisioningAttributes = System.currentTimeMillis()-start;
      debugMap.put("propagateProvisioningAttributes_millis", propagateProvisioningAttributes);
      
      if (grouperProvisioner.getConfigId().startsWith("junitProvisioningAttributePropagationTest")) {
        // just testing attribute propagation
        return;
      }
    }
        
    try {
      debugMap.put("state", "retrieveAllDataFromGrouperAndTarget");
      long start = System.currentTimeMillis();
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveAllData();
      long retrieveDataPass1 = System.currentTimeMillis()-start;
      debugMap.put("retrieveDataPass1_millis", retrieveDataPass1);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveAllDataFromGrouperAndTarget);
    }
    
    debugMap.put("state", "loadDataToGrouper");
    long start = System.currentTimeMillis();
    grouperProvisioner.retrieveGrouperProvisioningLogic().loadDataToGrouper();
    long retrieveDataPass1 = System.currentTimeMillis()-start;
    debugMap.put("loadDataToGrouper_millis", retrieveDataPass1);

    try {
      debugMap.put("state", "targetAttributeManipulation");
      // do not assign defaults to target
      // filter groups and manipulate attributes and types
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups());
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities());
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships());
              
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetAttributeManipulation);
    }

    try {
      debugMap.put("state", "matchingIdTargetObjects");
      // assign matching id to target objects
      this.grouperProvisioner.retrieveGrouperTranslator().matchingIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdTargetObjects);
    }
    
    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
    }

    try {
      debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");

      {
        List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
        List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
      }
      
      {
        List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
        List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(
            grouperProvisioningEntities, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsEntitiesToTarget);
    }

    try {
      debugMap.put("state", "manipulateGrouperTargetAttributes");
      List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);
  
      List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntities, null);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntities, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntities);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetGroupsEntitiesAttributes);
    }
    
    try {
      debugMap.put("state", "matchingIdGrouperGroupsEntities");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
    }

    try {
      debugMap.put("state", "retrieveIndividualEntitiesIfNeeded");
      // when select all entities is false e.g AWS then we need to fetch entities one by one.
      this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualEntitiesIfNeeded();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIndividualEntitiesIfNeeded);
    }
    
    {
      debugMap.put("state", "indexMatchingIdGroups");
      
      // index the groups and entity matching ids
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
      
      debugMap.put("state", "indexMatchingIdEntities");
      
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
    }

    {
      debugMap.put("state", "assignRecalc");
      // everything in a full sync is a recalc
      for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
        provisioningGroupWrapper.setRecalc(true);
      }
      for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
        provisioningEntityWrapper.setRecalc(true);
      }
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers())) {
        provisioningMembershipWrapper.setRecalc(true);
      }

    }
    debugMap.put("state", "insertGroups");
    createMissingGroupsFull();

    debugMap.put("state", "insertEntities");
    createMissingEntitiesFull();

    try {
      debugMap.put("state", "retrieveTargetGroupLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
    }

    // validate
    debugMap.put("state", "validateGroupsAndEntities");
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false, false);
    
    try {
  
      debugMap.put("state", "translateGrouperMembershipsToTarget");
      {
        List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(false);
        List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
            grouperProvisioningMemberships, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
    }

    try {
      debugMap.put("state", "manipulateGrouperMembershipTargetAttributes");
      List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForMemberships(grouperTargetMemberships);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(grouperTargetMemberships, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(grouperTargetMemberships);
  
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMembershipsAttributes);
    }

    try {
      debugMap.put("state", "matchingIdGrouperMemberships");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false));
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
    }

    // index the memberships
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();

    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectMembershipsFull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers(),
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers(),
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
    
    // validate memberships
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateMemberships(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false), false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, true);
    
    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.countInsertsUpdatesDeletes();

    this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafesAtStart();
    this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafes();
    
    RuntimeException runtimeException = null;
    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } catch (RuntimeException e) {
      runtimeException = e;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);

    }
  
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
      gcGrouperSync.setLastFullSyncStart(startTimestamp);
      gcGrouperSync.setLastFullSyncRun(nowTimestamp);

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setErrorMessage(null);
      gcGrouperSyncJob.setErrorTimestamp(null);
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
        gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
      }
      gcGrouperSyncJob.setPercentComplete(100);
      // 257 this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao()
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }

    // TODO flesh this out, resolve subjects, linked cached data, etc, try individually again
//    this.getGrouperProvisioner().retrieveTargetDao().resolveErrors();
//    this.getGrouperProvisioner().retrieveTargetDao().sendErrorFixesToTarget();

//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedGroupsToTarget();
//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedMembershipsToTarget();

    // make sure the sync objects are correct
//    new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

//    // step 1
//    debugMap.put("state", "retrieveData");
//    this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
//    
//    this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));
//
//    if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
//      debugMap.put("interrupted", true);
//      debugMap.put("state", "done");
//      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
//      return;
//    }
    if (GrouperClientUtils.isBlank(this.getGrouperProvisioner().getGcGrouperSyncLog().getStatus())) {
      this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.SUCCESS);
    }

  }
  
  public void loadDataToGrouper() {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable()) {
      return;
    }
    
 // we need to get data from the table
    GcTableSync loadUsersToTableGcTableSync = new GcTableSync();
    
    //retrieve the existing data from database
    GcTableSyncTableBean loadUsersToTableGcTableSyncTableBeanSql = new GcTableSyncTableBean(loadUsersToTableGcTableSync);
    
    GrouperProvisioningLoader grouperProvisioningLoader = this.grouperProvisioner.retrieveGrouperProvisioningLoader();
    String tableName = grouperProvisioningLoader.getLoaderEntityTableName();
    GrouperUtil.assertion(StringUtils.isNotBlank(tableName), "grouperLoaderEntityTableName is blank.");
    
    
    loadUsersToTableGcTableSyncTableBeanSql.configureMetadata("grouper", tableName);
    loadUsersToTableGcTableSync.setDataBeanTo(loadUsersToTableGcTableSyncTableBeanSql);

    Set<String> databaseColumnNames = new LinkedHashSet(grouperProvisioningLoader.getLoaderEntityColumnNames());

    Set<String> loadUsersToTableUniqueKeyColumnNames = new LinkedHashSet(grouperProvisioningLoader.getLoaderEntityKeyColumnNames());

    GcTableSyncTableMetadata gcTableSyncTableMetadata = loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata();

    gcTableSyncTableMetadata.assignColumns(GrouperUtil.join(databaseColumnNames.iterator(), ','));
    gcTableSyncTableMetadata.assignPrimaryKeyColumns(GrouperUtil.join(loadUsersToTableUniqueKeyColumnNames.iterator(), ','));
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("grouper");
    
    String configId = this.getGrouperProvisioner().getConfigId();
    
    String sql = "select " + gcTableSyncTableMetadata.columnListAll() + " from " + gcTableSyncTableMetadata.getTableName() + " where config_id = ?";
    
    List<Object[]> results = gcDbAccess.sql(sql).addBindVar(configId).selectList(Object[].class);

    GcTableSyncTableData loadUsersToTableGcTableSyncTableDataSql = new GcTableSyncTableData();
    loadUsersToTableGcTableSyncTableDataSql.init(loadUsersToTableGcTableSyncTableBeanSql, gcTableSyncTableMetadata.lookupColumns(loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata().columnListAll()), results);
    loadUsersToTableGcTableSyncTableDataSql.indexData();
 
    loadUsersToTableGcTableSyncTableBeanSql.setDataInitialQuery(loadUsersToTableGcTableSyncTableDataSql);
    loadUsersToTableGcTableSyncTableBeanSql.setGcTableSync(loadUsersToTableGcTableSync);

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    debugMap.put("loadUsersDbUniqueKeys", loadUsersToTableGcTableSyncTableDataSql.allPrimaryKeys().size());
    
    GcTableSyncTableBean gcTableSyncTableBeanFrom = new GcTableSyncTableBean();
    loadUsersToTableGcTableSync.setDataBeanFrom(gcTableSyncTableBeanFrom);
    gcTableSyncTableBeanFrom.setTableMetadata(loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata());
    gcTableSyncTableBeanFrom.setGcTableSync(loadUsersToTableGcTableSync);

    GcTableSyncTableData loadUsersToTableGcTableSyncTableDataLdap = new GcTableSyncTableData();
    loadUsersToTableGcTableSync.getDataBeanFrom().setDataInitialQuery(loadUsersToTableGcTableSyncTableDataLdap);

    loadUsersToTableGcTableSyncTableDataLdap.setColumnMetadata(loadUsersToTableGcTableSyncTableDataSql.getColumnMetadata());

    loadUsersToTableGcTableSyncTableDataLdap.setGcTableSyncTableBean(loadUsersToTableGcTableSyncTableDataSql.getGcTableSyncTableBean());

    List<GcTableSyncRowData> gcTableSyncRowDatas = new ArrayList<GcTableSyncRowData>();
    
    List<Object[]> targetTableData = grouperProvisioningLoader.retrieveLoaderEntityTableDataFromDataBean();
    
    for (Object[] rowData: targetTableData) {
      
      GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
      gcTableSyncRowDatas.add(gcTableSyncRowData);
      
      gcTableSyncRowData.setGcTableSyncTableData(loadUsersToTableGcTableSyncTableDataLdap);
      
      gcTableSyncRowData.setData(rowData);
      
    }
    
    
    loadUsersToTableGcTableSyncTableDataLdap.setRows(gcTableSyncRowDatas);

    // compare and sync
    GcTableSyncConfiguration gcTableSyncConfiguration = new GcTableSyncConfiguration();
    loadUsersToTableGcTableSync.setGcTableSyncConfiguration(gcTableSyncConfiguration);

    loadUsersToTableGcTableSync.setGcTableSyncOutput(new GcTableSyncOutput());

    Map<String, Object> debugMapLocal = new LinkedHashMap<String, Object>();
    GcTableSyncSubtype.fullSyncFull.syncData(debugMapLocal, loadUsersToTableGcTableSync);

    // merge the debug maps
    for (String key : debugMapLocal.keySet()) {
      
      Object newValue = debugMapLocal.get(key);
 
      // convert micros to millis
      if (key.endsWith("Millis")) {
        if (newValue instanceof Number) {
          newValue = ((Number)newValue).longValue()/1000;
        }
      }

      
      String newKey = "loadUsers" + StringUtils.capitalize(key);
      debugMap.put(newKey, newValue);

    }
    
  }

  /**
   * when data was retrieved (i.e. when the group syncs start)
   */
  private long retrieveDataStartMillisSince1970 = -1;
  
  /**
   * when data was retrieved (i.e. when the group syncs start)
   * @return when data retrieved
   */
  public long getRetrieveDataStartMillisSince1970() {
    return retrieveDataStartMillisSince1970;
  }
  
  /**
   * 
   */
  public void retrieveIndividualEntitiesIfNeeded() {
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntitiesAll() || !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
      return;
    }
    
    // Step 1 - Get all the grouper target entities and select them from the target (Call the batch method that gets all at once)
    
    List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
    
    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest(grouperTargetEntities, false);
    
    TargetDaoRetrieveEntitiesResponse targetEntitiesResponse = this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().retrieveEntities(targetDaoRetrieveEntitiesRequest);
    
    // Step 2 - Go through retrieveAllData method and whatever processing is done on the target entities; perform them here as well
    GrouperProvisioningLists targetProvisioningObjects = this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects();
    if (targetProvisioningObjects.getProvisioningEntities() == null) {
      targetProvisioningObjects.setProvisioningEntities(targetEntitiesResponse.getTargetEntities());
    } else {
      targetProvisioningObjects.getProvisioningEntities().addAll(targetEntitiesResponse.getTargetEntities());
    }
    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers();
    
    // add wrappers for all groups
    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningEntityWrappers.add(provisioningEntityWrapper);

      provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
    }
    
    // Step 3 - Go through the full logic and see if any other processing is done on the target entities
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(
        this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), null);

    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
        this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), true, false, false);
   
    
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
        this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities());
    
    
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities());
   
  }

  /**
   * 
   */
  public void provisionIncremental() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLogicIncremental grouperProvisioningLogicIncremental = this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental();

    this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafesAtStart();

    try {
      // ######### STEP 1: propagate provisioning data to group sync table
      debugMap.put("state", "propagateProvisioningAttributes");
      grouperProvisioningLogicIncremental.propagateProvisioningAttributes();
      
      // ######### STEP 2: check messages
      debugMap.put("state", "incrementalCheckMessages");
      grouperProvisioningLogicIncremental.incrementalCheckMessages();
      
      if (this.getGrouperProvisioner().getConfigId().startsWith("junitProvisioningAttributePropagationTest")) {
        // just testing attribute propagation
        return;
      }
      
      // ######### STEP 3: check for esb events
      debugMap.put("state", "incrementalCheckChangeLog");
      grouperProvisioningLogicIncremental.incrementalCheckChangeLog();
    } finally {      
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.logIncomingDataUnprocessed);
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
    
      // ######### STEP 4: see if any actions happened before the last full sync
      debugMap.put("state", "filterByProvisioningFullSync");
      grouperProvisioningLogicIncremental.filterByProvisioningFullSync();
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isFullSync()) {

      debugMap.put("runFullSync", "true");

      runFullSyncFromIncremental(); 
      
    } else {
    
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 5: events without recalc that occurred during full sync (after start before finish), should be recalc'ed
        debugMap.put("state", "recalcActionsDuringFullSync");
        grouperProvisioningLogicIncremental.recalcEventsDuringFullSync();
      }
      
      // ######### STEP 6: look for errors based on algorithm and retry those actions
      debugMap.put("state", "addErrorsToQueue");
      grouperProvisioningLogicIncremental.addErrorsToQueue();
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 7: filter out non recalc actions captures in recalc
        debugMap.put("state", "filterNonRecalcActionsCapturedByRecalc");
        grouperProvisioningLogicIncremental.filterNonRecalcActionsCapturedByRecalc();
      }
  
//      // ######### STEP 8: organize recalc and non recalc requests groups
//      debugMap.put("state", "organizeRecalcAndNonRecalcRequestsGroups");
//      grouperProvisioningLogicIncremental.organizeRecalcAndNonRecalcRequestsGroups();

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 8: retrieve all group sync objects for 
        debugMap.put("state", "retrieveIncrementalSyncGroups");
        {
          this.getGrouperProvisioner().retrieveGrouperSyncDao().retrieveIncrementalSyncGroups();
          Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();
          assignSyncObjectsToWrappersGroups(grouperSyncGroupIdToProvisioningGroupWrapper);
        }
        
        // ######### STEP 9: retrieve provisioning attributes for recalc groups and adjust sync objects
        //debugMap.put("state", "retrieveProvisioningGroupAttributesAndFixGroupSync");
        //grouperProvisioningLogicIncremental.retrieveProvisioningGroupAttributesAndFixGroupSync();
      
        // ######### STEP 10: filter if not provisionable
        debugMap.put("state", "filterByNotProvisionable");
        grouperProvisioningLogicIncremental.filterByGroupNotProvisionable();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 11: filter by group sync
        debugMap.put("state", "filterByGroupSync");
        grouperProvisioningLogicIncremental.filterByGroupSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 12: convert to group sync
        debugMap.put("state", "convertToGroupSync");
        grouperProvisioningLogicIncremental.convertToGroupSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 13: convert to full sync
        debugMap.put("state", "convertToFullSync");
        grouperProvisioningLogicIncremental.convertToFullSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isFullSync()) {

        debugMap.put("runFullSync", "true");

        runFullSyncFromIncremental(); 
        
      } else {
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 14: events without recalc that occurred during group sync (after start before finish), should be recalc'ed
          debugMap.put("state", "recalcActionsDuringGroupSync");
          grouperProvisioningLogicIncremental.recalcEventsDuringGroupSync();
        }
        
        this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.logIncomingDataToProcess);

        // index recalc data
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().indexIncrementalData();
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 15: retrieve all membership sync objects
          // ######### STEP 16: retrieve all members sync objects
          debugMap.put("state", "retrieveIncrementalSyncMemberships");
          {
            this.getGrouperProvisioner().retrieveGrouperSyncDao().retrieveIncrementalSyncMemberships();
            this.getGrouperProvisioner().retrieveGrouperSyncDao().retrieveIncrementalSyncMembers();
  
            Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();
            assignSyncObjectsToWrappersMembers(grouperSyncMemberIdToProvisioningEntityWrapper);
  
            Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();
            Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper 
              = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper();
            assignSyncObjectsToWrappersMemberships(grouperSyncGroupIdToProvisioningGroupWrapper, grouperSyncMemberIdToProvisioningEntityWrapper, grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper);
          }
  
          // ######### STEP 17: retrieve grouper data
          try {
            debugMap.put("state", "retrieveIncrementalDataFromGrouper");
            long start = System.currentTimeMillis();
            this.retrieveDataStartMillisSince1970 = start;
            // keep track of when this started so we can update when group syncs occurred
            debugMap.put("retrieveDataStartMillisSince1970", start);
            grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveGrouperDataIncremental();
            long retrieveGrouperDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveGrouperDataMillis", retrieveGrouperDataMillis);
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIncrementalDataFromGrouper);
          }
          
          // ######### STEP 18: filter unneeded actions
          debugMap.put("state", "filterUnneededActions");
          grouperProvisioningLogicIncremental.filterUnneededActions();
          
          // ######### STEP 19: convert inconsistent events to recalc
          debugMap.put("state", "convertInconsistentEventsToRecalc");
          grouperProvisioningLogicIncremental.convertInconsistentEventsToRecalc();
          
          // ######### STEP 20: copy incremental state to wrappers (so wrapper knows if recalc or whatever)
          debugMap.put("state", "copyIncrementalStateToWrappers");
          grouperProvisioningLogicIncremental.copyIncrementalStateToWrappers();
          
          // ######### STEP 21: resolve subjects for subject link if recalc or for subjects missing data
          try {
            debugMap.put("state", "retrieveSubjectLink");
            this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
          }
        }
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 22: translate grouper groups/entities to target format
          try {
            debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");
  
            {
              List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
              List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
              this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
            }
            
            {
              List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
              
              List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(
                  grouperProvisioningEntities, false, false);
              this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
            }    
  
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsEntitiesToTarget);
          }
  
          // ######### STEP 23: based on configs manipulate the defaults, types, etc for grouper target groups/entities translated attributes and fields
          try {
            debugMap.put("state", "manipulateGrouperTargetAttributes");
            List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);
        
            List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntities, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntities, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntities);
            
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetGroupsEntitiesAttributes);
          }
          
          // ######### STEP 24: calculate the matching id of grouper translated groups/entities
          try {
            debugMap.put("state", "matchingIdGrouperGroupsEntities");
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities());
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
          }
  
          // ######### STEP 25: take all the matching ids of grouper groups/entities and index those for quick lookups
          {
            debugMap.put("state", "indexMatchingIdGroups");
            
            // index the groups and entity matching ids
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
            
            debugMap.put("state", "indexMatchingIdEntities");
            
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
            
          }
          
          // ######### STEP 26: take all the matching ids of grouper groups/entities and index those for quick lookups
          // validate
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, false);
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false, false);
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateMemberships(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false), false);
          
          // ######### STEP 27: recalc retrieve data from target
          try {
            debugMap.put("state", "retrieveIncrementalTargetData");
            long start = System.currentTimeMillis();
            grouperProvisioningLogicIncremental.retrieveIncrementalTargetData();
            long retrieveTargetDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveTargetDataMillis", retrieveTargetDataMillis);
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetDataIncremental);
          }

          // ######### STEP 28: target object attribute manipulation
          try {
            debugMap.put("state", "targetAttributeManipulation");
            // do not assign defaults to target
            // filter groups and manipulate attributes and types
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups(), true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities(), true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships(), true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups());
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities());
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships());
                    
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetAttributeManipulation);
          }
  
          // ######### STEP 29: matching id target objects
          try {
            debugMap.put("state", "matchingIdTargetObjects");
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups());
            for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups())) {
              ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper().get(targetGroup.getMatchingId());
              if (provisioningGroupWrapper != null) {
                provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
              }
            }
            
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities());
            for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities())) {
              ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper().get(targetEntity.getMatchingId());
              if (provisioningEntityWrapper != null) {
                provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);
              }
            }

            this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships());
            for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships())) {
              ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper().get(targetMembership.getMatchingId());
              if (provisioningMembershipWrapper != null) {
                provisioningMembershipWrapper.setTargetProvisioningMembership(targetMembership);
              }
            }

          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdTargetObjects);
          }
      
          // ######### STEP 30: create groups / entities
          debugMap.put("state", "insertGroups");
          createMissingGroupsFull();
  
          debugMap.put("state", "insertEntities");
          createMissingEntitiesFull();
  
          // ######### STEP 31: retrieve target group and entity link
          try {
            debugMap.put("state", "retrieveTargetGroupLink");
            this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
            
            debugMap.put("state", "retrieveTargetEntityLink");
            this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
          }
            
          // ######### STEP 32: translate grouper memberships to target format
          try {
            debugMap.put("state", "translateGrouperMembershipsToTarget");
  
            {
              List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(false);
              List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
                  grouperProvisioningMemberships, true);
              this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
            }    
  
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
          }

          // ######### STEP 33: based on configs manipulate the defaults, types, etc for grouper target memberships translated attributes and fields
          try {
            debugMap.put("state", "manipulateGrouperTargetMembershipAttributes");

            List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForMemberships(grouperTargetMemberships);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(grouperTargetMemberships, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(grouperTargetMemberships);
            
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMembershipsAttributes);
          }
          
          // ######### STEP 34: calculate the matching id of grouper translated membership data
          try {
            debugMap.put("state", "matchingIdGrouperMemberships");
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false));
            
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
          }

          // ######### STEP 35: index matching ID of grouper and target objects
          debugMap.put("state", "indexMatchingIdOfGrouperObjects");
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();
          
          
          for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships())) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper().get(targetMembership.getMatchingId());
            if (provisioningMembershipWrapper != null) {
              provisioningMembershipWrapper.setTargetProvisioningMembership(targetMembership);
            }
            
          }
            
          
          // ######## Retrieve memberships from target that are recalc where the group is not recalc
          try {
            debugMap.put("state", "retrieveTargetIncrementalMembershipsWithRecalcWhereGroupIsNotRecalc");
            long start = System.currentTimeMillis();
            grouperProvisioningLogicIncremental.retrieveTargetIncrementalMembershipsWithRecalcWhereGroupIsNotRecalc();
            long retrieveTargetDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveTargetDataMillis", retrieveTargetDataMillis);
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetIncrementalMembershipsWithRecalcWhereGroupIsNotRecalc);
          }
          
          {
            // index the memberships
//            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();

            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
            }
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
            }
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectMembershipsFull(
                  this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers(),
                  this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers(),
                  this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
            }
            
            // validate memberships
//            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateMemberships(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(), false);

            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, true);
          }
          
          // ######### STEP 36: compare target objects
          try {
            debugMap.put("state", "compareTargetObjectsIncremental");
            this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
          }
          
          this.countInsertsUpdatesDeletes();
      
          this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafes();

          // ######### STEP 37: send changes to target
          RuntimeException runtimeException = null;
          try {
            debugMap.put("state", "sendChangesToTarget");
            TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
            targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
            targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
            targetDaoSendChangesToTargetRequest.setTargetObjectReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
            targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
            this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
          } catch (RuntimeException e) {
            runtimeException = e;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
            //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);
  
          }
        }
        
        {
          Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

          GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
          gcGrouperSyncJob.setErrorMessage(null);
          gcGrouperSyncJob.setErrorTimestamp(null);
          gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
          if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
            gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
          }
          gcGrouperSyncJob.setPercentComplete(100);

          // do this in the right spot, after assigning correct sync info about sync
          int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
          this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
      
          this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
        }

        // TODO flesh this out, resolve subjects, linked cached data, etc, try individually again
//        this.getGrouperProvisioner().retrieveTargetDao().resolveErrors();
//        this.getGrouperProvisioner().retrieveTargetDao().sendErrorFixesToTarget();

//        this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedGroupsToTarget();
//        this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedMembershipsToTarget();

        // make sure the sync objects are correct
//        new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

//        // step 1
//        debugMap.put("state", "retrieveData");
//        this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
//        
//        this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));
    //
//        if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
//          debugMap.put("interrupted", true);
//          debugMap.put("state", "done");
//          gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
//          return;
//        }
        if (GrouperClientUtils.isBlank(this.getGrouperProvisioner().getGcGrouperSyncLog().getStatus())) {
          this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.SUCCESS);
        }

      }
  
    }
    
    // ######### STEP 36: acknowledge messages
    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().acknowledgeMessagesProcessed();

  }

  public void storeAllSyncObjects() {
    {
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }
  }

  public void runFullSyncFromIncremental() {
    
    // end out this provisioner
    storeAllSyncObjects();
    this.getGrouperProvisioner().provisionFinallyBlock();
    
    // run a full sync
    GrouperProvisioner grouperProvisionerFullSync = GrouperProvisioner.retrieveProvisioner(this.getGrouperProvisioner().getConfigId());
    grouperProvisionerFullSync.setGcGrouperSyncHeartbeat(this.getGrouperProvisioner().getGcGrouperSyncHeartbeat());
    Map<String, Object> newDebugMap = new LinkedHashMap<String, Object>();
    newDebugMap.put("incrementalDebugMap", GrouperUtil.mapToString(this.getGrouperProvisioner().getDebugMap()) + "\n\n");
    
    grouperProvisionerFullSync.setDebugMap(newDebugMap);
    grouperProvisionerFullSync.setGrouperProvisioningOutput(this.getGrouperProvisioner().getGrouperProvisioningOutput());
    grouperProvisionerFullSync.provision(GrouperProvisioningType.fullProvisionFull);
  }

  public void createMissingGroupsFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertGroups(), false)) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningGroupWrapper> missingGroupWrappers = new ArrayList<ProvisioningGroupWrapper>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      
      ProvisioningGroup provisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      
      if (provisioningGroup == null || !provisioningGroupWrapper.isRecalc()) {
        continue;
      }
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();

      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }

      ProvisioningGroup targetGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
      
      if (targetGroup != null) {
        continue;
      }
      
      missingGroups.add(provisioningGroup);
      missingGroupWrappers.add(provisioningGroupWrapper);
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }

    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingGroupsForCreate", GrouperUtil.length(missingGroups));
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroupsForCreate);

    List<ProvisioningGroup> grouperTargetGroupsToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, true);

    if (GrouperUtil.length(grouperTargetGroupsToInsert) == 0) {
      this.grouperProvisioner.getDebugMap().put("groupTranslationEndedInNoGroupsOnInsert", true);
      return;
    }
    
    translateAndManipulateMembershipsForGroupsEntitiesCreate();
    
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroupsToInsert, null);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroupsToInsert, false, true, false);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroupsToInsert);

    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroupsToInsert);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroupsToInsert);
    
    // validate
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(grouperTargetGroupsToInsert, true, false);
    
    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetGroupsForCreate);

    //lets create these
    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
        
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
    }
    
    List<ProvisioningGroup> targetGroups = null;
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
      //retrieve so we have a copy
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, true));
      
      targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
      
      if (GrouperUtil.length(grouperTargetGroupsToInsert) != GrouperUtil.length(targetGroups)) {
        // maybe this should be an exception???
        throw new RuntimeException("Searched for " + GrouperUtil.length(grouperTargetGroupsToInsert) + " but retrieved " + GrouperUtil.length(targetGroups) + " maybe a config is off?");
      }
      
      registerRetrievedGroups(grouperTargetGroupsToInsert, targetGroups);
      
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(targetGroups, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(targetGroups);

      // index
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
      
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().setProvisioningGroups(targetGroups);
      
    }

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsCreated);
     
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
      Map<Object, ProvisioningGroupWrapper> matchingIdToProvisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();
      
      // match these up with retrieved groups
      // set these in the wrapper so they are linked with grouper group
      for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
        
        // look up the grouper group that looked this up
        ProvisioningGroupWrapper provisioningGroupWrapper = matchingIdToProvisioningGroupWrapper.get(targetGroup.getMatchingId());
        
        // not sure why it wouldnt match or exist...
        provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
        
        // this is already created!  :)
        provisioningGroupWrapper.setCreate(false);
      }
    }
    
  }

  //TODO duplicate method in the translator. Try to merge to have only one copy.
  public void translateAndManipulateMembershipsForGroupsEntitiesCreate() {
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isCreateGroupsAndEntitiesBeforeTranslatingMemberships()) {
      
      Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
      
      try {
        debugMap.put("state", "translateGrouperMembershipsToTarget");
        {
          List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>(this.getGrouperProvisioner().
              retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(true));
          
          List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
              grouperProvisioningMemberships, false);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
        }    

      } finally {
        this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
      }

      try {
        debugMap.put("state", "manipulateGrouperMembershipTargetAttributes");
        List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(true);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForMemberships(grouperTargetMemberships);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(grouperTargetMemberships, true, false, false);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(grouperTargetMemberships);
    
      } finally {
        this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMembershipsAttributes);
      }

      try {
        debugMap.put("state", "matchingIdGrouperMemberships");
        this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(true));
      } finally {
        this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
      }

      // index the memberships
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();

    }
  }

  /**
   * 
   * @param grouperTargetGroupsToInsert
   * @param targetProvisioningGroups
   */
  public void registerRetrievedGroups(
      List<ProvisioningGroup> grouperTargetGroups,
      List<ProvisioningGroup> targetProvisioningGroups) {
    
    GrouperProvisioningConfigurationAttribute searchAttribute = null;
    
    for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
      new Collection[] {
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().values(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values()}) {
    
      // look for required fields
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
          grouperProvisioningConfigurationAttributes) {
        
        if (grouperProvisioningConfigurationAttribute.isSearchAttribute()) {
          searchAttribute = grouperProvisioningConfigurationAttribute;
          break;
        }
        
        //default is id I guess
        if (!grouperProvisioningConfigurationAttribute.isAttribute() && "id".equals(grouperProvisioningConfigurationAttribute.getName())) {
          searchAttribute = grouperProvisioningConfigurationAttribute;
        }
      }
    }
    
    if (searchAttribute == null) {
      throw new RuntimeException("Identify a group search attribute!");
    }

    Map<Object, ProvisioningGroup> searchAttributeValueToGrouperTargetGroup = new HashMap<Object, ProvisioningGroup>();

    // index by search attribute
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      Object searchAttributeValue = grouperTargetGroup.retrieveFieldOrAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        searchAttributeValueToGrouperTargetGroup.put(searchAttributeValue, grouperTargetGroup);
      }
    }

    for (ProvisioningGroup targetProvisioningGroup : GrouperUtil.nonNull(targetProvisioningGroups)) {
      Object searchAttributeValue = targetProvisioningGroup.retrieveFieldOrAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        ProvisioningGroup grouperTargetGroup = searchAttributeValueToGrouperTargetGroup.get(searchAttributeValue);
        if (grouperTargetGroup != null) {
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
          if (provisioningGroupWrapper != null) {
            targetProvisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
            provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
          }
        }
      }
    }    
  }

  /**
   * 
   * @param grouperTargetEntitiesToInsert
   * @param targetProvisioningEntities
   */
  public void registerRetrievedEntities(
      List<ProvisioningEntity> grouperTargetEntities,
      List<ProvisioningEntity> targetProvisioningEntities) {
    
    GrouperProvisioningConfigurationAttribute searchAttribute = null;
    
    for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
      new Collection[] {
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().values(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()}) {
    
      // look for required fields
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
          grouperProvisioningConfigurationAttributes) {
        
        if (grouperProvisioningConfigurationAttribute.isSearchAttribute()) {
          searchAttribute = grouperProvisioningConfigurationAttribute;
          break;
        }
        
        //default is id I guess
        if (!grouperProvisioningConfigurationAttribute.isAttribute() && "id".equals(grouperProvisioningConfigurationAttribute.getName())) {
          searchAttribute = grouperProvisioningConfigurationAttribute;
        }
      }
    }
    
    if (searchAttribute == null) {
      throw new RuntimeException("Identify an entity search attribute!");
    }

    Map<Object, ProvisioningEntity> searchAttributeValueToGrouperTargetEntity = new HashMap<Object, ProvisioningEntity>();

    // index by search attribute
    for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(grouperTargetEntities)) {
      Object searchAttributeValue = grouperTargetEntity.retrieveFieldOrAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        searchAttributeValueToGrouperTargetEntity.put(searchAttributeValue, grouperTargetEntity);
      }
    }

    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(targetProvisioningEntities)) {
      Object searchAttributeValue = targetProvisioningEntity.retrieveFieldOrAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        ProvisioningEntity grouperTargetEntity = searchAttributeValueToGrouperTargetEntity.get(searchAttributeValue);
        if (grouperTargetEntity != null) {
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
          if (provisioningEntityWrapper != null) {
            targetProvisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
            provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
          }
        }
      }
    }    
  }

  
  public void createMissingEntitiesFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertEntities(), false)) {
      return;
    }
      
    
    //do we have missing entities?
    List<ProvisioningEntity> missingEntities = new ArrayList<ProvisioningEntity>();
    List<ProvisioningEntityWrapper> missingEntityWrappers = new ArrayList<ProvisioningEntityWrapper>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      
      ProvisioningEntity provisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      if (provisioningEntity == null || !provisioningEntityWrapper.isRecalc() || provisioningEntityWrapper.isDelete()) {
        continue;
      }
      
      // shouldnt be null at this point
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      
      if (!gcGrouperSyncMember.isProvisionable()) {
        continue;
      }
      ProvisioningEntity targetEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      
      if (targetEntity != null) {
        continue;
      }
      
      missingEntities.add(provisioningEntity);
      missingEntityWrappers.add(provisioningEntityWrapper);    
    }
    if (GrouperUtil.length(missingEntities) == 0) {
      return;
    }
    
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingEntitiesForCreate", GrouperUtil.length(missingEntities));
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningEntities(missingEntities);
    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingEntitiesForCreate);
    
    // translate
    List<ProvisioningEntity> grouperTargetEntitiesToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(missingEntities, false, true);
    
    if (GrouperUtil.length(grouperTargetEntitiesToInsert) == 0) {
      this.grouperProvisioner.getDebugMap().put("groupTranslationEndedInNoEntitiesOnInsert", true);
      return;
    }
    
    
    translateAndManipulateMembershipsForGroupsEntitiesCreate();
    
    
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntitiesToInsert, null);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntitiesToInsert, false, true, false);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntitiesToInsert);
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(grouperTargetEntitiesToInsert);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningEntities(grouperTargetEntitiesToInsert);
    // validate
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(grouperTargetEntitiesToInsert, true, null);
    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForEntitiesToInsert(grouperTargetEntitiesToInsert);
        
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetEntitiesForCreate);
    //lets create these
    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertEntities(new TargetDaoInsertEntitiesRequest(grouperTargetEntitiesToInsert));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertEntities(grouperTargetEntitiesToInsert, false);
        
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
    }
    
    List<ProvisioningEntity> targetEntities = null;
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
     //retrieve so we have a copy
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToInsert, false));
      
      targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
      
      if (GrouperUtil.length(grouperTargetEntitiesToInsert) != GrouperUtil.length(targetEntities)) {
        // maybe this should be an exception???
        throw new RuntimeException("Searched for " + GrouperUtil.length(grouperTargetEntitiesToInsert) + " but retrieved " + GrouperUtil.length(targetEntities) + " maybe a config is off?");
      }

      registerRetrievedEntities(grouperTargetEntitiesToInsert, targetEntities);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(targetEntities, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(targetEntities);
      // index
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(targetEntities);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().setProvisioningEntities(targetEntities);
    }
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetEntitiesCreated);
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
      Map<Object, ProvisioningEntityWrapper> matchingIdToProvisioningEntityWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper();
      
      // match these up with retrieved entities
      // set these in the wrapper so they are linked with grouper entity
      for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
        
        // look up the grouper group that looked this up
        ProvisioningEntityWrapper provisioningEntityWrapper = matchingIdToProvisioningEntityWrapper.get(targetEntity.getMatchingId());
        
        // not sure why it wouldnt match or exist...
        provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);
        // this is already created!  :)
        provisioningEntityWrapper.setCreate(false);
      }
    }
    
  }

  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  public void retrieveAllData() {
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse
            = GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter()
              .retrieveAllData(new TargetDaoRetrieveAllDataRequest());
          // retrieve all the target data and put in GrouperProvisioningDataTarget
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningDataTarget()
            .setTargetProvisioningObjects(targetDaoRetrieveAllDataResponse.getTargetData());
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    retrieveGrouperDataFull();
    
    enhanceEntityAttributesWithSqlResolver(true);
    
    enhanceEntityAttributesWithLdapResolver();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
    
    retrieveAllTargetAndGrouperDataPost();
    
    processTargetWrappers();
    
  }
  
  public void enhanceEntityAttributesWithSqlResolver(boolean isFullSync) {
    
    GrouperProvisioningConfigurationBase provisioningConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    if (!provisioningConfiguration.isResolveAttributesWithSql()) {
      return;
    }
    
    String dbConnectionName = null;
    String grouperAttributeThatMatchesRow = null;
    String subjectSearchMatchingColumn = null;
    String subjectSourceIdColumn = null;
    String tableOrViewName = null;
    String expression = null;
    String commaSeparatedColumns = null;
    String lastUpdatedColumn = null;
    String lastUpdatedColumnType = null;
    
    String globalSqlResolver = provisioningConfiguration.getGlobalSqlResolver();
    
    if (StringUtils.isNotBlank(globalSqlResolver)) {
      
      boolean isEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("entityAttributeResolver."+globalSqlResolver+".enabled", true);
      
      if (!isEnabled) {
        return;
      }
      
      dbConnectionName = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".sqlConfigId");
      
      grouperAttributeThatMatchesRow = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".grouperAttributeThatMatchesRow");

      subjectSearchMatchingColumn = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".subjectSearchMatchingColumn");

      tableOrViewName = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".tableOrViewName");

      commaSeparatedColumns = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".columnNames");

      subjectSourceIdColumn = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalSqlResolver+".subjectSourceIdColumn");

      lastUpdatedColumn = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalSqlResolver+".lastUpdatedColumn");
      
      lastUpdatedColumnType = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalSqlResolver+".lastUpdatedType");
        
    } else {
      
      dbConnectionName = provisioningConfiguration.getEntityAttributesSqlExternalSystem();
      
      grouperAttributeThatMatchesRow = provisioningConfiguration.getEntityAttributesSqlMappingEntityAttribute();

      subjectSearchMatchingColumn = provisioningConfiguration.getEntityAttributesSubjectSearchMatchingColumn();
      
      tableOrViewName = provisioningConfiguration.getEntityAttributesTableViewName();
      
      commaSeparatedColumns = provisioningConfiguration.getEntityAttributesColumnNames();
      
      subjectSourceIdColumn = provisioningConfiguration.getEntityAttributesSubjectSourceIdColumn();
      
      expression = provisioningConfiguration.getEntityAttributesSqlMappingExpression();
      
      lastUpdatedColumn = provisioningConfiguration.getEntityAttributesLastUpdatedColumn();

      lastUpdatedColumnType = provisioningConfiguration.getEntityAttributesLastUpdatedType();
      
    }
    
    Set<String> columnsWhichAreAttributes = GrouperUtil.splitTrimToSet(commaSeparatedColumns, ",");
    
    Set<String> columnNamesToFetch = GrouperUtil.splitTrimToSet(commaSeparatedColumns, ",");
    
    if (StringUtils.isNotBlank(lastUpdatedColumn) && !columnNamesToFetch.contains(lastUpdatedColumn.trim())) {
      columnNamesToFetch.add(lastUpdatedColumn.trim());
    }
    
    if (!columnNamesToFetch.contains(subjectSearchMatchingColumn.trim())) {
      columnNamesToFetch.add(subjectSearchMatchingColumn.trim());
    }
    
    String commaSeparatedColNames = GrouperUtil.setToString(columnNamesToFetch);
      
    boolean selectAllSqlOnFull = provisioningConfiguration.isSelectAllSqlOnFull();
    
    GrouperProvisioningLists grouperProvisioningObjects = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();
    
    List<ProvisioningEntity> provisioningEntities = grouperProvisioningObjects.getProvisioningEntities();
    
    StringBuilder sqlInitial = new StringBuilder("select ");
    sqlInitial.append(commaSeparatedColNames);
    sqlInitial.append(" from ");
    sqlInitial.append(tableOrViewName);
    
    List<Object[]> attributesFromTable = new ArrayList<Object[]>();
    
    if ( (isFullSync && !selectAllSqlOnFull) || !isFullSync) {
      if (provisioningEntities.size() == 0) {
        return;
      }
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(provisioningEntities.size(), 900);
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      
      for (int i = 0; i < numberOfBatches; i++) {
        
        List<ProvisioningEntity> currentBatchProvisioningEntities = GrouperUtil.batchList(provisioningEntities, 900, i);
        StringBuilder sql = new StringBuilder(sqlInitial);
        
        sql.append(" where "+ subjectSearchMatchingColumn + " in ( ");
        
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbConnectionName);
        
        for (int j=0; j<currentBatchProvisioningEntities.size();j++) {
          ProvisioningEntity provisioningEntity = currentBatchProvisioningEntities.get(j);
          
          String subjectMatchingIdentifier = null;
          if (StringUtils.isNotBlank(expression)) {
            elVariableMap.clear();
            elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
            Object object = this.getGrouperProvisioner().retrieveGrouperTranslator().runScript(expression, elVariableMap);
            subjectMatchingIdentifier = GrouperUtil.stringValue(object);
          } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectId")) {
              subjectMatchingIdentifier = provisioningEntity.getSubjectId();
          } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectIdentifier0")) {
            subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
          } else {
              throw new RuntimeException("invalid grouperAttributeThatMatchesRow: "+grouperAttributeThatMatchesRow + " expected 'subjectId' or 'subjectIdentifier0'");
          }
          
          gcDbAccess.addBindVar(subjectMatchingIdentifier);
          if (j>0) {
            sql.append(",");
          }
          sql.append("?");
        }
        sql.append(" ) ");
        attributesFromTable.addAll(gcDbAccess.sql(sql.toString()).selectList(Object[].class));
      
      }
    } else {
      attributesFromTable.addAll(new GcDbAccess().connectionName(dbConnectionName).sql(sqlInitial.toString()).selectList(Object[].class));
    }

    String[] colNamesFromAttributesTable = GrouperUtil.splitTrim(commaSeparatedColNames, ",");
    
    Map<MultiKey, Object[]> subjectSearchMatchingColumnToAttributes = new HashMap<MultiKey, Object[]>();
    
    int indexOfSubjectSearchMatchingColumn = GrouperUtil.indexOf(colNamesFromAttributesTable, subjectSearchMatchingColumn);
    
    for (Object[] oneRowOfAttributes: attributesFromTable) {
      Object subjectSearchMatchingValue = oneRowOfAttributes[indexOfSubjectSearchMatchingColumn];
      if (subjectSearchMatchingValue != null) {
        
        String subjectSearchMatchingValueString = GrouperUtil.stringValue(subjectSearchMatchingValue);
        
        MultiKey identifier = null;
        if (StringUtils.isNotBlank(subjectSourceIdColumn)) {
          identifier = new MultiKey(subjectSearchMatchingValueString, subjectSourceIdColumn);
        } else {
          identifier = new MultiKey(new String[] {subjectSearchMatchingValueString});
        }
        
        subjectSearchMatchingColumnToAttributes.put(identifier, oneRowOfAttributes);
      }
    }
    
    /**
     * subjectSearchMatchingColumnToAttributes looks like
     * test.subject.0 -> [school0, description0,....]
     */
    
    Map<String, Object> elVariableMap = new HashMap<String, Object>();
    
    for (ProvisioningEntity provisioningEntity: provisioningEntities) {
      
      String subjectMatchingIdentifier = null;
      
      if (StringUtils.isNotBlank(expression)) {
        elVariableMap.clear();
        elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
        Object object = this.getGrouperProvisioner().retrieveGrouperTranslator().runScript(expression, elVariableMap);
        subjectMatchingIdentifier = GrouperUtil.stringValue(object);
      } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectId")) {
          subjectMatchingIdentifier = provisioningEntity.getSubjectId();
      } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectIdentifier0")) {
        subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
      } else {
          throw new RuntimeException("invalid grouperAttributeThatMatchesRow: "+grouperAttributeThatMatchesRow + " expected 'subjectId' or 'subjectIdentifier0'");
      }
      
      MultiKey identifier = null;
      if (StringUtils.isNotBlank(subjectSourceIdColumn)) {
        String subjectSourceIdFromProvisioningEntity = provisioningEntity.retrieveAttributeValueString("subjectSourceId");
        identifier = new MultiKey(subjectMatchingIdentifier, subjectSourceIdFromProvisioningEntity);
      } else {
        identifier = new MultiKey(new String[] {subjectMatchingIdentifier});
      }
        
      Object[] attributeValues = subjectSearchMatchingColumnToAttributes.get(identifier);
      if (attributeValues != null) {
        int i = 0;
        for (String attributeName: colNamesFromAttributesTable) {
          
          if (columnsWhichAreAttributes.contains(attributeName)) {
            
            provisioningEntity.assignAttributeValue("entityAttributeResolverSql__"+attributeName.toLowerCase(), attributeValues[i]);
          }

          i++;
         
        }
      }
      
    }
        
  }
  
  public void enhanceEntityAttributesWithLdapResolver() {
    
    GrouperProvisioningConfigurationBase provisioningConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    if (!provisioningConfiguration.isResolveAttributesWithLdap()) {
      return;
    }
    
    GrouperProvisioningLists grouperProvisioningObjects = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();
    
    List<ProvisioningEntity> provisioningEntities = grouperProvisioningObjects.getProvisioningEntities();
    
    String globalLdapResolver = provisioningConfiguration.getGlobalLdapResolver();
    
    String ldapConfigId = null;
    String baseDn = null;
    String searchScope = null;
    String ldapAttributes = null;
    String subjectSearchMatchingAttribute = null;
    String subjectSourceId = null;
    String grouperAttributeThatMatchesRecord = null;
    String filterPart = null;
    String lastUpdatedAttribute = null;
    String multiValuedLdapAttributes = null;
    String expression = null;
    String lastUpdatedAttributeFormat = null;
    boolean filterAllLdapOnFull = true;
    
    if (StringUtils.isNotBlank(globalLdapResolver)) {
      
      boolean isEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("entityAttributeResolver."+globalLdapResolver+".enabled", true);
      
      if (!isEnabled) {
        return;
      }
      
      ldapConfigId = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".ldapConfigId");
      baseDn = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".baseDn");
      subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".subjectSourceId");
      searchScope = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".searchScope");
      ldapAttributes = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".ldapAttributes");
      subjectSearchMatchingAttribute = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".subjectSearchMatchingAttribute");
      grouperAttributeThatMatchesRecord = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".grouperAttributeThatMatchesRecord");
      filterPart = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".filterPart");
      lastUpdatedAttribute = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".lastUpdatedAttribute");
      lastUpdatedAttributeFormat = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".ldapLastUpdatedFormat");
      multiValuedLdapAttributes = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".multiValuedLdapAttributes");
      
    } else {
      
      ldapConfigId = provisioningConfiguration.getEntityAttributesLdapExternalSystem();
      baseDn = provisioningConfiguration.getEntityAttributesLdapBaseDn();
      subjectSourceId = provisioningConfiguration.getEntityAttributesLdapSubjectSource();
      searchScope = provisioningConfiguration.getEntityAttributesLdapSearchScope();
      ldapAttributes = provisioningConfiguration.getEntityAttributesLdapAttributes();
      subjectSearchMatchingAttribute = provisioningConfiguration.getEntityAttributesLdapMatchingSearchAttribute();
      grouperAttributeThatMatchesRecord = provisioningConfiguration.getEntityAttributesLdapMappingEntityAttribute();
      filterPart = provisioningConfiguration.getEntityAttributesLdapFilterPart();
      lastUpdatedAttribute = provisioningConfiguration.getEntityAttributesLdapLastUpdatedAttribute();
      lastUpdatedAttributeFormat = provisioningConfiguration.getEntityAttributesLdapLastUpdatedAttributeFormat();
      expression = provisioningConfiguration.getEntityAttributesLdapMatchingExpression();
    }
    
    List<LdapEntry> ldapEntries = new ArrayList<LdapEntry>();
    
    filterAllLdapOnFull = provisioningConfiguration.isFilterAllLDAPOnFull();
    
    Set<String> ldapAttributesSet = GrouperUtil.splitTrimToSet(ldapAttributes, ",");
    
    Set<String> multiValuedAttributesSet = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(multiValuedLdapAttributes, ","));
    
    if (StringUtils.isNotBlank(multiValuedLdapAttributes)) {
      ldapAttributesSet.addAll(multiValuedAttributesSet);
    }
    
    if (StringUtils.isNotBlank(subjectSearchMatchingAttribute)) {
      ldapAttributesSet.add(subjectSearchMatchingAttribute);
    }
    
    String[] ldapAttributesArray = GrouperUtil.toArray(ldapAttributesSet, String.class);
    
    LdapSearchScope ldapSearchScope = LdapSearchScope.valueOfIgnoreCase(searchScope, true);
    
    if (!filterAllLdapOnFull) {
      
      if (provisioningEntities.size() == 0) {
        return;
      }
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(provisioningEntities.size(), 900);
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      
      for (int i = 0; i < numberOfBatches; i++) {
        
        List<ProvisioningEntity> currentBatchProvisioningEntities = GrouperUtil.batchList(provisioningEntities, 900, i);
        
        String filter = null;
        if (StringUtils.isNotBlank(filterPart)) {
          filter = "(&";
          filterPart = filterPart.trim();
          if (filterPart.startsWith("(")) {
            filter += filterPart;
          } else {
            filter += "(" + filterPart + ")";
          }
        } else {
          filter = "(|";
        }
        
        
        for (int j=0; j<currentBatchProvisioningEntities.size(); j++) {
          
          ProvisioningEntity provisioningEntity = currentBatchProvisioningEntities.get(j);
          
          String subjectMatchingIdentifier = null;
          if (StringUtils.isNotBlank(expression)) {
            elVariableMap.clear();
            elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
            Object object = this.getGrouperProvisioner().retrieveGrouperTranslator().runScript(expression, elVariableMap);
            subjectMatchingIdentifier = GrouperUtil.stringValue(object);
          } else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectId")) {
              subjectMatchingIdentifier = provisioningEntity.getSubjectId();
          } else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectIdentifier0")) {
            subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
          } else {
              throw new RuntimeException("invalid grouperAttributeThatMatchesRecord: "+grouperAttributeThatMatchesRecord + " expected 'subjectId' or 'subjectIdentifier0'");
          }
          
          filter += "("+subjectSearchMatchingAttribute+"="+subjectMatchingIdentifier+")";  
        }
        
        filter += ")";
        ldapEntries.addAll(LdapSessionUtils.ldapSession().list(ldapConfigId, baseDn, ldapSearchScope, filter, ldapAttributesArray, null));
      
      }
    } else {
      
      String filter = null;
      if (StringUtils.isNotBlank(filterPart)) {
        filter = "(&";
        filterPart = filterPart.trim();
        if (filterPart.startsWith("(")) {
          filter += filterPart;
        } else {
          filter += "(" + filterPart + ")";
        }
        filter += "("+subjectSearchMatchingAttribute+"=*))"; 
        
      } else {
        filter = "("+subjectSearchMatchingAttribute+"=*)"; 
      }
      
      ldapEntries.addAll(LdapSessionUtils.ldapSession().list(ldapConfigId, baseDn, ldapSearchScope, filter, ldapAttributesArray, null));
      
    }
    
    Map<MultiKey, LdapEntry> identifierToLdapEntry = new HashMap<MultiKey, LdapEntry>();
    
    for (LdapEntry ldapEntry: GrouperUtil.nonNull(ldapEntries)) {
      
      LdapAttribute attribute = ldapEntry.getAttribute(subjectSearchMatchingAttribute);
      if (attribute != null) {
        
        Collection<String> stringValues = attribute.getStringValues();
        if (GrouperUtil.length(stringValues) == 1) {
          
          MultiKey identifier = null;
          if (StringUtils.isNotBlank(subjectSourceId)) {
            identifier = new MultiKey(stringValues.iterator().next(), subjectSourceId);
          } else {
            identifier = new MultiKey(new String[] {stringValues.iterator().next()});
          }
          identifierToLdapEntry.put(identifier, ldapEntry);
        }
      }
    }
    
    Map<String, Object> elVariableMap = new HashMap<String, Object>();
    
    for (ProvisioningEntity provisioningEntity: provisioningEntities) {
      
      String subjectMatchingIdentifier = null;
      if (StringUtils.isNotBlank(expression)) {
        elVariableMap.clear();
        elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
        Object object = this.getGrouperProvisioner().retrieveGrouperTranslator().runScript(expression, elVariableMap);
        subjectMatchingIdentifier = GrouperUtil.stringValue(object);
      } else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectId")) {
          subjectMatchingIdentifier = provisioningEntity.getSubjectId();
      } 
      else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectIdentifier0")) {
        subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
      } else {
          throw new RuntimeException("invalid grouperAttributeThatMatchesRecord: "+grouperAttributeThatMatchesRecord + " expected 'subjectId' or 'subjectIdentifier0'");
      }
        
      MultiKey identifier = null;
      if (StringUtils.isNotBlank(subjectSourceId)) {
        String subjectSourceIdFromProvisioningEntity = provisioningEntity.retrieveAttributeValueString("subjectSourceId");
        identifier = new MultiKey(subjectMatchingIdentifier, subjectSourceIdFromProvisioningEntity);
      } else {
        identifier = new MultiKey(new String[] {subjectMatchingIdentifier});
      }
      
      LdapEntry ldapEntry = identifierToLdapEntry.get(identifier);
      if (ldapEntry != null) {
        
        for (String ldapAttributeName: ldapAttributesArray) {
          
          if (StringUtils.equals(ldapAttributeName, "lastUpdatedAttribute")) {
            continue;
          }
          
          LdapAttribute attribute = ldapEntry.getAttribute(ldapAttributeName);
          if (attribute != null) {
            
            if (multiValuedAttributesSet.contains(attribute.getName())) {
              
              for ( String attributeValue: GrouperUtil.nonNull(attribute.getStringValues())) {
                provisioningEntity.addAttributeValue("entityAttributeResolverLdap__"+attribute.getName().toLowerCase(), attributeValue);
              }
              
            } else {
              
              if (GrouperUtil.length(attribute.getStringValues()) == 0) {
                continue;
              }
              
              if (GrouperUtil.length(attribute.getStringValues()) == 1) {
                provisioningEntity.assignAttributeValue("entityAttributeResolverLdap__"+attribute.getName().toLowerCase(), attribute.getStringValues().iterator().next());
              } else {
                
                String concatenatedAttributeValues = GrouperUtil.join(attribute.getStringValues().iterator(), ",");
                provisioningEntity.assignAttributeValue("entityAttributeResolverLdap__"+attribute.getName().toLowerCase(), concatenatedAttributeValues);
                
              }
              
             
            }
            
          }
        }
        
      }
      
    }
      
  }

  /** 
   * override this method to do some logic after all grouper and target data is retrieved (e.g. if there are DN overrides)
   */
  public void retrieveAllTargetAndGrouperDataPost() {
    
    
  }

  /**
   * take target data and add wrapper and add to data store
   */
  public void processTargetWrappers() {
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();
    
    // add wrappers for all groups
    for (ProvisioningGroup targetProvisioningGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups())) {
      ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningGroupWrappers.add(provisioningGroupWrapper);

      provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
    }

    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers();
    
    // add wrappers for all groups
    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningEntityWrappers.add(provisioningEntityWrapper);

      provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
    }
    
    Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers();
    
    // add wrappers for all groups
    for (ProvisioningMembership targetProvisioningMembership : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships())) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningMembershipWrappers.add(provisioningMembershipWrapper);

      provisioningMembershipWrapper.setTargetProvisioningMembership(targetProvisioningMembership);
    }
    
  }

  public void retrieveGrouperDataFull() {
    
    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataSyncGrouper
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataFull();
    
    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // put the sync objects in their respective wrapper objects
    // this is where additional wrapper objects can be added
    assignSyncObjectsToWrappers();

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 

    GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
    gcGrouperSync.setGroupCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups()));
    gcGrouperSync.setUserCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities()));
    gcGrouperSync.setRecordsCount(
        GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningMemberships())
        );
    
  }


  public void assignSyncObjectsToWrappers() {
    
    Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();
    assignSyncObjectsToWrappersGroups(grouperSyncGroupIdToProvisioningGroupWrapper);

    Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();
    assignSyncObjectsToWrappersMembers(grouperSyncMemberIdToProvisioningEntityWrapper);
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
    assignSyncObjectsToWrappersMemberships(grouperSyncGroupIdToProvisioningGroupWrapper,
        grouperSyncMemberIdToProvisioningEntityWrapper,
        groupUuidMemberUuidToProvisioningMembershipWrapper);
  }

  public void assignSyncObjectsToWrappersMemberships(
      Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper,
      Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper,
      Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper) {
    {

      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMemberships();

      Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

      if (GrouperUtil.length(gcGrouperSyncMemberships) > 0) {
        
        int syncMembershipReferenceMissing = 0;
        
        for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
          
          // data is not consistent just ignore for now
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperSyncGroupIdToProvisioningGroupWrapper.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
          if (provisioningGroupWrapper == null) {
            syncMembershipReferenceMissing++;
            continue;
          }
          GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
                    
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperSyncMemberIdToProvisioningEntityWrapper.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
          if (provisioningEntityWrapper == null) {
            syncMembershipReferenceMissing++;
            continue;
          }
          GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
          
          MultiKey groupIdMemberId = new MultiKey(gcGrouperSyncGroup.getGroupId(),
              gcGrouperSyncMember.getMemberId());
          
          ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
          
          if (provisioningMembershipWrapper == null) {
            provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
            provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
            groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupIdMemberId, provisioningMembershipWrapper);
            this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().add(provisioningMembershipWrapper);
          }
          
          provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          
          MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId());
          provisioningMembershipWrapper.setSyncGroupIdSyncMemberId(syncGroupIdSyncMemberId);
          grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper.put(syncGroupIdSyncMemberId, provisioningMembershipWrapper);
        }
        if (syncMembershipReferenceMissing > 0) {
          this.getGrouperProvisioner().getDebugMap().put("syncMembershipReferenceMissing", syncMembershipReferenceMissing);
        }
      }
      
    }
  }

  public void assignSyncObjectsToWrappersMembers(
      Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper) {
    {
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();

      // loop through sync groups
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMembers())) {
    
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(gcGrouperSyncMember.getMemberId());
        
        if (provisioningEntityWrapper == null) {
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
          memberUuidToProvisioningEntityWrapper.put(gcGrouperSyncMember.getMemberId(), provisioningEntityWrapper);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers().add(provisioningEntityWrapper);
        }
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);

        grouperSyncMemberIdToProvisioningEntityWrapper.put(gcGrouperSyncMember.getId(), provisioningEntityWrapper);
      }
    }
  }

  public void assignSyncObjectsToWrappersGroups(
      Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper) {
    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();

      // loop through sync groups
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncGroups())) {
    
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
        
        if (provisioningGroupWrapper == null) {
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
          groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(provisioningGroupWrapper);
        }
        provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
        
        grouperSyncGroupIdToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getId(), provisioningGroupWrapper);
      }
    }
  }
  
  public void retrieveGrouperDataIncremental() {

    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataGrouper
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataIncremental();

    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
//    // incrementals need to clone and setup sync objects as deletes
//    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().setupIncrementalClonesOfGroupProvisioningObjects();

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // put the sync objects in their respective wrapper objects
    assignSyncObjectsToWrappers();

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 
    
  }
  
  protected void countInsertsUpdatesDeletes() {
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships());

    {
      //TODO: Is this correct?
      //countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.replace, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces().getProvisioningMemberships());
      List<ProvisioningMembership> targetMemberships = new ArrayList<ProvisioningMembership>();
      
      Collection<List<ProvisioningMembership>> targetMembershipsLists = this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces().getProvisioningMemberships().values();
      for (List<ProvisioningMembership> provisioningMemberships: targetMembershipsLists) {
        targetMemberships.addAll(provisioningMemberships);
      }
      this.grouperProvisioner.getGrouperProvisioningOutput().addReplace(GrouperUtil.length(targetMemberships));  
        
    }
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningEntities());
    
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships());
    
  }
  
  protected void countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction provisioningObjectChangeAction, List<? extends ProvisioningUpdatable> provisioningUpdatables) {
    // maybe not count fields?
    if (provisioningUpdatables == null) {
      return;
    }
    switch(provisioningObjectChangeAction) {
      case insert:
        this.grouperProvisioner.getGrouperProvisioningOutput().addInsert(GrouperUtil.length(provisioningUpdatables));  
        break;
      case update:
        this.grouperProvisioner.getGrouperProvisioningOutput().addUpdate(GrouperUtil.length(provisioningUpdatables));  
        break;
      case replace:
        this.grouperProvisioner.getGrouperProvisioningOutput().addReplace(GrouperUtil.length(provisioningUpdatables));  
        break;
      case delete:
        this.grouperProvisioner.getGrouperProvisioningOutput().addDelete(GrouperUtil.length(provisioningUpdatables));  
        break;
    }
    for (ProvisioningUpdatable provisioningUpdatable : provisioningUpdatables) {
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(provisioningUpdatable.getInternal_objectChanges())) {
        switch (provisioningObjectChange.getProvisioningObjectChangeAction()) {
          case insert:
            this.grouperProvisioner.getGrouperProvisioningOutput().addInsert(1);  
            break;
          case update:
            this.grouperProvisioner.getGrouperProvisioningOutput().addUpdate(1);  
            break;
          case delete:
            this.grouperProvisioner.getGrouperProvisioningOutput().addDelete(1);  
            break;
          
        }
      }
    }
  }
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLogic.class);

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




  public void calculateProvisioningDataToDelete() {
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    this.calculateProvisioningMembershipsToDelete();
    
  }


  /**
   * take the sync members and see which ones do not correspond to a grouper member
   */
  public void calculateProvisioningEntitiesToDelete() {
  
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningMemberWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningEntitiesToDelete = 0;
    
    List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = 
        this.grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
    
    Map<String, ProvisioningEntityWrapper> memberIdToEntityWrapperToDelete = new HashMap<>();
  
    // loop through sync groups
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
  
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      // if a entity has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (grouperProvisioningEntity == null && gcGrouperSyncMember != null) {
        
        memberIdToEntityWrapperToDelete.put(gcGrouperSyncMember.getMemberId(), provisioningEntityWrapper);
        provisioningEntitiesToDelete++;
        
      }
    }
    
    if (memberIdToEntityWrapperToDelete.size() == 0) {
      return;
    }
    
    List<ProvisioningEntity> membersNonProvisionable = this.getGrouperProvisioner().retrieveGrouperDao().retrieveMembersNonProvisionable(memberIdToEntityWrapperToDelete.keySet());
    
    Map<String, ProvisioningEntity> memberIdToProvisioningEntityToDelete = new HashMap<>();
    
    for (ProvisioningEntity provisioningEntityNotProvisionable: GrouperUtil.nonNull(membersNonProvisionable)) {
      memberIdToProvisioningEntityToDelete.put(provisioningEntityNotProvisionable.getId(), provisioningEntityNotProvisionable);
    }
    
    for (String memberIdToDelete: memberIdToEntityWrapperToDelete.keySet()) {
      
      ProvisioningEntity grouperProvisioningEntity = memberIdToProvisioningEntityToDelete.get(memberIdToDelete);
      
      ProvisioningEntityWrapper provisioningEntityWrapper = memberIdToEntityWrapperToDelete.get(memberIdToDelete);
      
      if (grouperProvisioningEntity == null) {
        
        GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
        grouperProvisioningEntity = new ProvisioningEntity();
        grouperProvisioningEntity.setId(gcGrouperSyncMember.getMemberId());
        grouperProvisioningEntity.setSubjectId(gcGrouperSyncMember.getSubjectId());
        grouperProvisioningEntity.assignAttributeValue("subjectSourceId", gcGrouperSyncMember.getSourceId());
        if ("subjectIdentifier1".equals(grouperProvisioner.retrieveGrouperProvisioningBehavior().getSubjectIdentifierForMemberSyncTable())) {
          grouperProvisioningEntity.assignAttributeValue("subjectIdentifier1", gcGrouperSyncMember.getSubjectIdentifier());
        } else if ("subjectIdentifier2".equals(grouperProvisioner.retrieveGrouperProvisioningBehavior().getSubjectIdentifierForMemberSyncTable())) {
          grouperProvisioningEntity.assignAttributeValue("subjectIdentifier2", gcGrouperSyncMember.getSubjectIdentifier());
        } else {
          grouperProvisioningEntity.assignAttributeValue("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());
        }
        
        if (GrouperUtil.length(grouperProvisioningObjectMetadataItems) > 0) {
          
          String jsonMetadata = gcGrouperSyncMember.getMetadataJson();
          
          if (!StringUtils.isBlank(jsonMetadata) && !StringUtils.equals("{}", jsonMetadata)) {
            JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonMetadata);
            for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : grouperProvisioningObjectMetadataItems) {
              if (grouperProvisioningObjectMetadataItem.isShowForMember()) {
                
                String metadataItemName = grouperProvisioningObjectMetadataItem.getName();
                if (metadataItemName.startsWith("md_")) {
                  if (jsonNode.has(metadataItemName)) {
                    GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
                        GrouperUtil.defaultIfNull(grouperProvisioningObjectMetadataItem.getValueType(), GrouperProvisioningObjectMetadataItemValueType.STRING);
                    String value = GrouperUtil.jsonJacksonGetString(jsonNode, metadataItemName);
                    grouperProvisioningEntity.assignAttributeValue(metadataItemName, grouperProvisioningObjectMetadataItemValueType.convert(value));
                  }
                }
              }
            }
          }
        }
        
//        //TODO select in bulk from grouper members
//        Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), gcGrouperSyncMember.getMemberId(), false);
//        if (member != null) {
//          grouperProvisioningEntity.setName(member.getName());
//          grouperProvisioningEntity.setEmail(member.getEmail0());
//          
//          grouperProvisioningEntity.assignAttributeValue("description", member.getDescription());
//        }

      }
      
      provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
      provisioningEntityWrapper.setDelete(true);
      
      memberUuidToProvisioningMemberWrapper.put(grouperProvisioningEntity.getId(), provisioningEntityWrapper);
      
    }
        
    if (provisioningEntitiesToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntitiesToDelete", provisioningEntitiesToDelete);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningGroupsToDelete() {
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    
    int provisioningGroupsToDeleteCount = 0;
  
    List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = 
        this.grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();
  
    // loop through sync groups
    for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
  
      //TODO Perhaps look at grouper_groups information for unprovisionable groups that aren't deleted from grouper just like 
      // we did for entities.
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (grouperProvisioningGroup == null && gcGrouperSyncGroup != null) {
        
        provisioningGroupsToDeleteCount++;
        
        // create a provisioning group to delete
        grouperProvisioningGroup = new ProvisioningGroup();
        grouperProvisioningGroup.setId(gcGrouperSyncGroup.getGroupId());
        grouperProvisioningGroup.setName(gcGrouperSyncGroup.getGroupName());
        grouperProvisioningGroup.setIdIndex(gcGrouperSyncGroup.getGroupIdIndex());
        
        if (GrouperUtil.length(grouperProvisioningObjectMetadataItems) > 0) {
          
          String jsonMetadata = gcGrouperSyncGroup.getMetadataJson();
          
          if (!StringUtils.isBlank(jsonMetadata) && !StringUtils.equals("{}", jsonMetadata)) {
            JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonMetadata);
            for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : grouperProvisioningObjectMetadataItems) {
              if (grouperProvisioningObjectMetadataItem.isShowForMember()) {
                
                String metadataItemName = grouperProvisioningObjectMetadataItem.getName();
                if (metadataItemName.startsWith("md_")) {
                  if (jsonNode.has(metadataItemName)) {
                    GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
                        GrouperUtil.defaultIfNull(grouperProvisioningObjectMetadataItem.getValueType(), GrouperProvisioningObjectMetadataItemValueType.STRING);
                    String value = GrouperUtil.jsonJacksonGetString(jsonNode, metadataItemName);
                    grouperProvisioningGroup.assignAttributeValue(metadataItemName, grouperProvisioningObjectMetadataItemValueType.convert(value));
                  }
                }
              }
            }
          }
        }
        
        provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
        provisioningGroupWrapper.setDelete(true);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      }
      
    }
    if (provisioningGroupsToDeleteCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupsToDeleteCount", provisioningGroupsToDeleteCount);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningMembershipsToDelete() {
  
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> gcGrouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();
    Map<String, ProvisioningGroupWrapper> gcGrouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

    int provisioningMshipsToDelete = 0;
    
    // loop through sync groups
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {

      ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();

      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();

      if (grouperProvisioningMembership == null && gcGrouperSyncMembership != null) {

        provisioningMshipsToDelete++;
        
        ProvisioningGroupWrapper provisioningGroupWrapper = gcGrouperSyncGroupIdToProvisioningGroupWrapper.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
        if (provisioningGroupWrapper == null) {
          throw new RuntimeException("Cant find groupId: '" + gcGrouperSyncMembership.getGrouperSyncGroupId() + "'");
        }
        ProvisioningEntityWrapper provisioningEntityWrapper = gcGrouperSyncMemberIdToProvisioningEntityWrapper.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
        if (provisioningEntityWrapper == null) {
          throw new RuntimeException("Cant find entityId: '" + gcGrouperSyncMembership.getGrouperSyncMemberId() + "'");
        }

        GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
        GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();

        // these can be null if in the index
        
        String groupId = gcGrouperSyncGroup.getGroupId();
        String memberId = gcGrouperSyncMember.getMemberId();


        // create a provisioning group to delete
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
        provisioningMembership.setProvisioningGroupId(groupId);
        provisioningMembership.setProvisioningEntityId(memberId);
        
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
          provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
        } else {
          throw new RuntimeException("Cant find provisioning group: '" + groupId + "'");
        }
  
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
          provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
        } else {
          throw new RuntimeException("Cant find provisioning entity: '" + memberId + "'");
        }
          
        provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
        provisioningMembershipWrapper.setDelete(true);
        
        groupUuidMemberUuidToProvisioningMembershipWrapper.put(provisioningMembershipWrapper.getGroupIdMemberId(), provisioningMembershipWrapper);
        
      }
      provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
    }      
    if (provisioningMshipsToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningMshipsToDelete", provisioningMshipsToDelete);
    }
    
    
  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingObjectsIncremental() {
    retrieveMissingGroupsIncremental();
    retrieveMissingEntitiesIncremental();
  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingGroupsIncremental() {
    
    // first lets see if we should even be doing this
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroupMissingIncremental()) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups())) {
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
      
      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }
      if (this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().groupLinkMissing(gcGrouperSyncGroup)) {
        missingGroups.add(provisioningGroup);
      }
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingIncrementalGroupsForRetrieve", missingGroups);
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroups);

    // translate
    List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroups);
    
    // log this
    //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroups);
    
    //lets retrieve these
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroups, false));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
    
    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingRetrieved().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsRetrieved);
    
    Map<Object, ProvisioningGroup> matchingIdToGrouperTargetGroup = new HashMap<Object, ProvisioningGroup>();
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      matchingIdToGrouperTargetGroup.put(grouperTargetGroup.getMatchingId(), grouperTargetGroup);
    }
    
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroup grouperTargetGroup = matchingIdToGrouperTargetGroup.get(targetGroup.getMatchingId());
      
      // not sure why it wouldnt match or exist...
      grouperTargetGroup.getProvisioningGroupWrapper().setTargetProvisioningGroup(targetGroup);
    }

  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingEntitiesIncremental() {
    
  }

}
