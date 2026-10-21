package edu.internet2.middleware.grouper.app.provisioningExamples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * https://docs.logicgate.com/
 */
public class LogicGateRecordsProvisionerDao extends GrouperProvisionerTargetDaoBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(LogicGateRecordsProvisionerDao.class);

  /**
   * Retrieve one record by its LogicGate record id.  The provisioning logic calls this right after an insert
   * to read the new record back; insertEntity sets the new record id on the target entity so this can find it.
   * Without that read-back the inserted entity stays unmatched and gets inserted again later in the same run.
   */
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    
    initDao();

    long startNanos = System.nanoTime();

    try {

      String recordId = null;
      
      // if the attribute and value there, then all set
      if (Strings.CS.equals(targetDaoRetrieveEntityRequest.getSearchAttribute(), "id")) {
        recordId = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());
      }
      ProvisioningEntity targetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();
      if (StringUtils.isBlank(recordId) && targetEntity != null) {
        recordId = targetEntity.getId();
      }
      // no cached record id (e.g. incremental for someone whose id was never stored): find it by the key
      // attribute (Penn ID) in the local copy of the LogicGate records from the last full sync, so an
      // existing person is not inserted again
      if (StringUtils.isBlank(recordId) && targetEntity != null) {
        recordId = this.recordIdFromGrouperTable(targetEntity);
      }
      if (StringUtils.isBlank(recordId)) {
        return new TargetDaoRetrieveEntityResponse();
      }

      JsonNode recordNode = this.logicGateCommands.recordLookupNode(recordId);

      // a record in the inactive step is a removed contact, treat as not there
      if (!this.logicGateCommands.isLiveStep(GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonGetNode(recordNode, "step"), "id"))) {
        return new TargetDaoRetrieveEntityResponse();
      }

      String[] pennIdPennKeyNameOrgTitle = this.logicGateCommands.recordLookupAttributes(recordNode);

      ProvisioningEntity provisioningEntity = new ProvisioningEntity();
      provisioningEntity.setId(recordId);
      // the logic matches a read-back to the inserted entity on the search attribute "id", which is an
      // attribute, not the entity id field.  Without this every insert is flagged as not retrievable and the
      // incremental inserts it again on its next run
      provisioningEntity.assignAttributeValue("id", recordId);
      int index=0;
      for (String attributeName : this.getLogicGateCommands().getUserAttributes()) {
        provisioningEntity.assignAttributeValue(attributeName, pennIdPennKeyNameOrgTitle[index++]);
      }
      return new TargetDaoRetrieveEntityResponse(provisioningEntity);      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }


  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    initDao();

    long startNanos = System.nanoTime();

    try {
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      // people who should have a live record: provisionable in this provisioner's sync table.  The key attribute
      // (Penn ID) is translated from the subject id, so the subject id is the key
      Set<String> provisionableKeys = new HashSet<String>(new GcDbAccess().sql(
          "select gsm.subject_id from grouper_sync_member gsm where gsm.grouper_sync_id = ? and gsm.provisionable = 'T'")
          .addBindVar(this.getGrouperProvisioner().getGcGrouperSync().getId()).selectList(String.class));

      // the cleanup deletes the inactive records of people who are not provisionable, and these deletes are not
      // seen by the provisioner failsafe.  An empty list means something is wrong, not that everyone left
      GrouperUtil.assertion(provisionableKeys.size() > 0, "No provisionable people in grouper_sync_member for "
          + this.getGrouperProvisioner().getConfigId() + ", not cleaning up LogicGate records");

      // read every record (v2, fields inline), fix duplicates and inactive records, one live record per Penn ID
      Map<String, String[]> recordData = this.logicGateCommands.recordRetrieveAllV2(provisionableKeys);
      for (String recordId : recordData.keySet()) {
        String[] pennIdPennKeyNameOrgTitle = recordData.get(recordId);
        ProvisioningEntity provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(recordId);
        // "id" is the search / first matching attribute, set it like retrieveEntity does
        provisioningEntity.assignAttributeValue("id", recordId);
        int index=0;
        for (String attributeName : this.getLogicGateCommands().getUserAttributes()) {
          provisioningEntity.assignAttributeValue(attributeName, pennIdPennKeyNameOrgTitle[index++]);
        }
        results.add(provisioningEntity);
      }
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }

  }


  private LogicGateCommands logicGateCommands;

  /**
   * find the record id for an entity by its key attribute (Penn ID) in prov_logicgate_record, the local copy
   * of the LogicGate records written by the last full sync (and by inserts since)
   * @param targetEntity
   * @return the record id or null if not loading to the table or not found or not unique
   */
  private String recordIdFromGrouperTable(ProvisioningEntity targetEntity) {
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLoadEntitiesToGrouperTable()) {
      return null;
    }
    String keyAttribute = this.logicGateCommands.getUserAttributes().iterator().next();
    String keyValue = targetEntity.retrieveAttributeValueString(keyAttribute);
    if (StringUtils.isBlank(keyValue)) {
      return null;
    }
    String keyColumnName = keyAttribute.replaceAll("[^A-Za-z0-9]", "_");
    List<String> recordIds = new GcDbAccess().sql("select id from prov_logicgate_record where config_id = ? and "
        + keyColumnName + " = ?")
      .addBindVar(this.getGrouperProvisioner().getConfigId())
      .addBindVar(keyValue)
      .selectList(String.class);
    return GrouperUtil.length(recordIds) == 1 ? recordIds.get(0) : null;
  }

  /**
   * add a just-created record to prov_logicgate_record so it can be found by Penn ID before the next full sync
   * @param recordId
   * @param attributes user attribute name to value
   */
  private void insertGrouperTableRow(String recordId, Map<String, String> attributes) {
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLoadEntitiesToGrouperTable()) {
      return;
    }
    try {
      List<String> columnNames = new ArrayList<String>();
      List<Object> bindVars = new ArrayList<Object>();
      columnNames.add("config_id");
      bindVars.add(this.getGrouperProvisioner().getConfigId());
      columnNames.add("id");
      bindVars.add(recordId);
      for (String attr : this.logicGateCommands.getUserAttributes()) {
        columnNames.add(attr.replaceAll("[^A-Za-z0-9]", "_"));
        bindVars.add(attributes.get(attr));
      }
      GcDbAccess gcDbAccess = new GcDbAccess().sql("insert into prov_logicgate_record (" + String.join(", ", columnNames)
          + ") values (" + StringUtils.repeat("?", ", ", columnNames.size()) + ")");
      for (Object bindVar : bindVars) {
        gcDbAccess.addBindVar(bindVar);
      }
      gcDbAccess.executeSql();
    } catch (RuntimeException e) {
      // the full sync reloads this table, so a failure here only matters until then
      LOG.error("Could not insert into prov table for record: " + recordId, e);
    }
  }

  
  public LogicGateCommands getLogicGateCommands() {
    return logicGateCommands;
  }

  
  public TheState getTheState() {
    return theState;
  }

  public static class TheState {
    // synchronized: the cleanup updates counts from several threads
    Map<String, Object> debugMap = java.util.Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * 
     * @param theProvisionerConfigIdPrefix e.g. penn
     */
    public LogicGateCommands initTheState(String theProvisionerConfigIdPrefix) {
      
      String url = GrouperConfig.retrieveConfig().propertyValueString(theProvisionerConfigIdPrefix + ".logicGateApi.Url");
      url = GrouperUtil.stripLastSlashIfExists(url);
  
      String client = GrouperConfig.retrieveConfig().propertyValueString(theProvisionerConfigIdPrefix + ".logicGateApi.client");
      String secret = GrouperConfig.retrieveConfig().propertyValueString(theProvisionerConfigIdPrefix + ".logicGateApi.secret");

      String userAttributes = GrouperConfig.retrieveConfig().propertyValueString(theProvisionerConfigIdPrefix + ".logicGateApi.userAttributes");

      LogicGateCommands logicGateCommands = new LogicGateCommands(debugMap, url, client, secret, userAttributes);

      // cleanup settings: see the LogicGate section of grouper.base.properties
      logicGateCommands.configId = theProvisionerConfigIdPrefix;
      logicGateCommands.maxDeletesPerRun = GrouperConfig.retrieveConfig().propertyValueInt(theProvisionerConfigIdPrefix + ".logicGateApi.maxDeletesPerRun", -1);
      logicGateCommands.auditRetentionDays = GrouperConfig.retrieveConfig().propertyValueInt(theProvisionerConfigIdPrefix + ".logicGateApi.auditRetentionDays", 730);
      logicGateCommands.reportEmails = GrouperConfig.retrieveConfig().propertyValueString(theProvisionerConfigIdPrefix + ".logicGateApi.reportEmails");
      logicGateCommands.threads = Math.max(1, GrouperConfig.retrieveConfig().propertyValueInt(theProvisionerConfigIdPrefix + ".logicGateApi.threads", 5));
      logicGateCommands.reverseLinkScanThreshold = GrouperConfig.retrieveConfig().propertyValueInt(theProvisionerConfigIdPrefix + ".logicGateApi.reverseLinkScanThreshold", 200);
      return logicGateCommands;
    }
  }
  
  public static class LogicGateCommands {
  
    private String accessToken = null;
    private String client;
    private String secret;
    private String url;
    private Map<String, Object> debugMap = null;
    private Set<String> userAttributes = null;
    // ===================== cleanup / audit state =====================
    // The provisioner keeps LogicGate at one live record per person and no inactive records: extra copies and
    // people who left are deleted, after moving their direct links to the record that is kept.  Every change
    // is written to prov_logicgate_audit with a snapshot of the record, and changes that touched workflow
    // links are emailed in a report.  readOnly on the provisioner turns all of it into audit rows only.

    /** provisioner config id, e.g. logicGateContacts */
    private String configId;

    /** provisioner instance id, groups the audit rows of one run */
    private String runId;

    /** true if the provisioner is readOnly: audit what would happen, change nothing */
    private boolean readOnly;

    /** stop deleting after this many deletes in one run, -1 for no limit */
    private int maxDeletesPerRun = -1;

    /** delete audit rows older than this many days, -1 to keep forever */
    private int auditRetentionDays = 730;

    /** comma separated email addresses for the report, blank to not send */
    private String reportEmails;

    /** threads for the per person cleanup */
    private int threads = 5;

    /** if more records than this need their links, scan the linking workflows instead of asking per record */
    private int reverseLinkScanThreshold = 200;

    /** deletes done this run, for maxDeletesPerRun */
    private java.util.concurrent.atomic.AtomicInteger deletesThisRun = new java.util.concurrent.atomic.AtomicInteger();

    /** lines for the report email, one per notable change */
    private List<String> reportLines = java.util.Collections.synchronizedList(new ArrayList<String>());

    /** workflow id to name of the workflows a contact can be linked to (the other workflows in its application) */
    private Map<String, String> linkWorkflowIdToName = null;

    public Set<String> getUserAttributes() {
      return userAttributes;
    }

    public LogicGateCommands(Map<String, Object> theDebugMap, String theUrl, String theClient, String theSecret, String theUserAttributes) {
      this.url = theUrl;
      this.client = theClient;
      this.secret = theSecret;
      this.debugMap = theDebugMap;
      
      this.userAttributes = GrouperUtil.splitTrimToSet(theUserAttributes, ",");
      
      if (contactsWorkflowId == null) {
        contactsWorkflowId = findWorkflow("Contacts");
      }
      
      if (stepIdCreateContact == null) {
        findSteps();
      }
      
      for (String userAttribute : this.userAttributes) {
        String fieldId = fieldNameToId.get(userAttribute);
        if (fieldId == null) {
          findFields();
          fieldId = fieldNameToId.get(userAttribute);
          GrouperUtil.assertion(fieldId != null, "Cant find field id for user attribute: '" + userAttribute + "'");
        }
      }

    }
    
    public void deleteRecord(String recordId) {
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/records/" + recordId + "/move/" + LogicGateRecordsProvisionerDao.stepIdDeleteContact);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.put);
      
      executeRequest(grouperHttpClient, 200, 201, "delete");
    
    }

    public String findStep(ArrayNode stepNodes, String stepName) {
    
      for (int i=0;i<stepNodes.size();i++) {
        JsonNode stepNode = stepNodes.get(i);
        stepNode = GrouperUtil.jsonJacksonGetNode(stepNode, "step");
        String name = GrouperUtil.jsonJacksonGetString(stepNode, "name");
        String id = GrouperUtil.jsonJacksonGetString(stepNode, "id");
        if (Strings.CS.equals(name, stepName)) {
          return id;
        }
      }
    
      //  [
      //   {
      //      "step":{
      //         "id":"ndp4mlh5",
      //         "active":true,
      //         "created":1694715864860,
      //         "updated":1698345358203,
      //         "name":"Create Contact",
      //         "priority":1,
      //         "userGroupOperationType":"FULL_ACCESS",
      //         "xpos":55,
      //         "ypos":55,
      //         "isPublic":false,
      //         "commentsEnabled":false,
      //         "documentsEnabled":false,
      //         "externalUserMfaRequired":false,
      //         "uxriEnabled":false,
      //         "sla":{
      //            "enabled":false,
      //            "duration":0
      //         },
      //         "workflow":null,
      //         "workflowId":null,
      //         "assignableUserType":"APP_USERS",
      //         "stepType":"ORIGIN",
      //         "end":false,
      //         "chain":false,
      //         "origin":true
      //      },
      //      "records":0,
      //      "end":false
      //   },
      //   {
      //      "step":{
      //         "id":"b1F5eJnY",
      //         "active":true,
      //         "created":1698248201435,
      //         "updated":1698345503665,
      //         "name":"Inactive Contacts Repository",
      //         "priority":2,
      //         "userGroupOperationType":"FULL_ACCESS",
      //         "xpos":295,
      //         "ypos":55,
      //         "isPublic":false,
      //         "commentsEnabled":false,
      //         "documentsEnabled":false,
      //         "externalUserMfaRequired":false,
      //         "uxriEnabled":false,
      //         "sla":{
      //            "enabled":false,
      //            "duration":0
      //         },
      //         "workflow":null,
      //         "workflowId":null,
      //         "assignableUserType":"APP_USERS",
      //         "stepType":"END",
      //         "end":true,
      //         "chain":false,
      //         "origin":false
      //      },
      //      "records":0,
      //      "end":true
      //   },
      //   {
      //      "step":{
      //         "id":"ir8Mev3u",
      //         "active":true,
      //         "created":1694715864858,
      //         "updated":1698248196846,
      //         "name":"Active Contacts Repository",
      //         "priority":2,
      //         "userGroupOperationType":"FULL_ACCESS",
      //         "xpos":200,
      //         "ypos":55,
      //         "isPublic":false,
      //         "commentsEnabled":false,
      //         "documentsEnabled":false,
      //         "externalUserMfaRequired":false,
      //         "uxriEnabled":false,
      //         "sla":{
      //            "enabled":false,
      //            "duration":0
      //         },
      //         "workflow":null,
      //         "workflowId":null,
      //         "assignableUserType":"APP_USERS",
      //         "stepType":"END",
      //         "end":true,
      //         "chain":false,
      //         "origin":false
      //      },
      //      "records":6,
      //      "end":true
      //   }
      //]
      throw new RuntimeException("Cant find step! '" + stepName + "'");
    
    }

    public void findSteps() {
    
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/records/search/summarize/workflows/" + contactsWorkflowId + "/steps");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      grouperHttpClient.addHeader("Accept", "application/json");
      executeRequest(grouperHttpClient, 200, -1, "findSteps");
    
      //System.out.println(grouperHttpClient.getResponseBody());
      
      JsonNode jsonNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
      
      ArrayNode stepNodes = (ArrayNode)jsonNode;
      
    
      stepIdCreateContact = findStep(stepNodes, "Create Contact");
      stepIdActiveContacts = findStep(stepNodes, "Active Contacts Repository");
      stepIdDeleteContact = findStep(stepNodes, "Inactive Contacts Repository");
      
      //  [
      //   {
      //      "step":{
      //         "id":"ndp4mlh5",
      //         "active":true,
      //         "created":1694715864860,
      //         "updated":1698345358203,
      //         "name":"Create Contact",
      //         "priority":1,
      //         "userGroupOperationType":"FULL_ACCESS",
      //         "xpos":55,
      //         "ypos":55,
      //         "isPublic":false,
      //         "commentsEnabled":false,
      //         "documentsEnabled":false,
      //         "externalUserMfaRequired":false,
      //         "uxriEnabled":false,
      //         "sla":{
      //            "enabled":false,
      //            "duration":0
      //         },
      //         "workflow":null,
      //         "workflowId":null,
      //         "assignableUserType":"APP_USERS",
      //         "stepType":"ORIGIN",
      //         "end":false,
      //         "chain":false,
      //         "origin":true
      //      },
      //      "records":0,
      //      "end":false
      //   },
      //   {
      //      "step":{
      //         "id":"b1F5eJnY",
      //         "active":true,
      //         "created":1698248201435,
      //         "updated":1698345503665,
      //         "name":"Inactive Contacts Repository",
      //         "priority":2,
      //         "userGroupOperationType":"FULL_ACCESS",
      //         "xpos":295,
      //         "ypos":55,
      //         "isPublic":false,
      //         "commentsEnabled":false,
      //         "documentsEnabled":false,
      //         "externalUserMfaRequired":false,
      //         "uxriEnabled":false,
      //         "sla":{
      //            "enabled":false,
      //            "duration":0
      //         },
      //         "workflow":null,
      //         "workflowId":null,
      //         "assignableUserType":"APP_USERS",
      //         "stepType":"END",
      //         "end":true,
      //         "chain":false,
      //         "origin":false
      //      },
      //      "records":0,
      //      "end":true
      //   },
      //   {
      //      "step":{
      //         "id":"ir8Mev3u",
      //         "active":true,
      //         "created":1694715864858,
      //         "updated":1698248196846,
      //         "name":"Active Contacts Repository",
      //         "priority":2,
      //         "userGroupOperationType":"FULL_ACCESS",
      //         "xpos":200,
      //         "ypos":55,
      //         "isPublic":false,
      //         "commentsEnabled":false,
      //         "documentsEnabled":false,
      //         "externalUserMfaRequired":false,
      //         "uxriEnabled":false,
      //         "sla":{
      //            "enabled":false,
      //            "duration":0
      //         },
      //         "workflow":null,
      //         "workflowId":null,
      //         "assignableUserType":"APP_USERS",
      //         "stepType":"END",
      //         "end":true,
      //         "chain":false,
      //         "origin":false
      //      },
      //      "records":6,
      //      "end":true
      //   }
      //]
    
    }

    public String findWorkflow(String workflowName) {
    
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/workflows");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      grouperHttpClient.addHeader("Accept", "application/json");
      
      executeRequest(grouperHttpClient, 200, -1, "findWorkflow");
    
      //System.out.println(grouperHttpClient.getResponseBody());
      
      JsonNode jsonNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
      ArrayNode workflowNodes = (ArrayNode)jsonNode;
      for (int i=0;i<workflowNodes.size();i++) {
        JsonNode workflowNode = workflowNodes.get(i);
        String name = GrouperUtil.jsonJacksonGetString(workflowNode, "name");
        String id = GrouperUtil.jsonJacksonGetString(workflowNode, "id");
        if (StringUtils.equals(name, workflowName)) {
          return id;
        }
      }
      //  {
      //    "id":"QwFrqpww",
      //    "name":"Contacts",
      //    "recordPrefix":"Contact",
      //    "xpos":948,
      //    "ypos":182,
      //    "workflowType":null,
      //    "application":{
      //       "id":"xm3oEjVM",
      //       "name":"Mission Continuity",
      //       "color":"#a8002a",
      //       "icon":"fas__fa-medkit",
      //       "hasDashboard":null,
      //       "type":"OPERATIONAL_RESILIENCY",
      //       "live":false,
      //       "permissionsEnabled":false,
      //       "workflows":[
      //          
      //       ]
      //    },
      //    "records":6,
      //    "recordsComplete":6,
      //    "recordsOverdue":0,
      //    "steps":3
      // }
      throw new RuntimeException("Cant find workflow! '" + workflowName + "'");
      
    }

    public void executeRequest(GrouperHttpClient grouperHttpClient, int responseCode0, int responseCode1, String operation) {
      
      long start = System.nanoTime();
    
      GrouperHttpClientLog grouperHttpCallLog = new GrouperHttpClientLog();
    
      GrouperHttpClient.logStart(grouperHttpCallLog);
      try {
    
        String tokenUsed = accessToken();
        grouperHttpClient.addHeader("Authorization", "Bearer " + tokenUsed);

        grouperHttpClient.executeRequest();

        // LogicGate invalidates a client's token when another token is issued for the same client (e.g. the
        // incremental sync or another job authenticating), so on 401 get a new token and retry once
        if (grouperHttpClient.getResponseCode() == 401) {
          GrouperUtil.mapAddValue(this.debugMap, "reauthenticateOn401Count", 1);
          grouperHttpClient.addHeader("Authorization", "Bearer " + refreshAccessToken(tokenUsed));
          grouperHttpClient.executeRequest();
        }

        GrouperUtil.mapAddValue(this.debugMap, operation + "Millis", (long)((System.nanoTime()-start)/1000000L));
        GrouperUtil.mapAddValue(this.debugMap, operation + "Count", 1);
    
        if (grouperHttpClient.getResponseCode() == responseCode0 || grouperHttpClient.getResponseCode() == responseCode1) {
          return;
        }
        
        throw new RuntimeException("Response code " + grouperHttpClient.getResponseCode() + " invalid, expecting: " + responseCode0 + (responseCode1 == -1 ? "" : (" or " + responseCode1)));
        
      } catch (Throwable t) {
        throw GrouperUtil.exceptionConvertToRuntime(t,  grouperHttpCallLog.getLog().toString());
      } finally {
        GrouperHttpClient.logEnd();
      }
    
    }

    private synchronized String accessToken() {
      if (this.accessToken == null) {
        this.accessToken = authenticate();
      }
      return this.accessToken;
    }

    /**
     * get a new token after a 401.  Writes run on several threads, so only re-authenticate if no other
     * thread already replaced the token that failed
     * @param tokenThatFailed
     * @return the current token
     */
    private synchronized String refreshAccessToken(String tokenThatFailed) {
      if (this.accessToken == null || Strings.CS.equals(this.accessToken, tokenThatFailed)) {
        this.accessToken = authenticate();
      }
      return this.accessToken;
    }

    public String authenticate() {
      
      long start = System.nanoTime();
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/account/token");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUser(this.client);
      grouperHttpClient.assignPassword(this.secret);
      
      grouperHttpClient.executeRequest();
      //System.out.println(grouperHttpClient.getResponseBody());
      
      JsonNode jsonNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
      
      //System.out.println(GrouperUtil.jsonJacksonGetLong(jsonNode, "expires_in"));
      //System.out.println(GrouperUtil.jsonJacksonGetString(jsonNode, "access_token"));
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonNode, "access_token");
      
      GrouperUtil.mapAddValue(debugMap, "authenticateMillis", (long)((System.nanoTime()-start)/1000000L));
      GrouperUtil.mapAddValue(debugMap, "authenticateCount", 1);
      
      return accessToken;
    
    }

    /**
       * 
       * @param theState
       * @param recordId
       * @return pennIdPennKeyNameOrgTitle
       */
      public String[] recordLookup(String recordId) {
        return recordLookupAttributes(recordLookupNode(recordId));
      }

      /**
       * GET one record (v1), includes the current step and the field values
       * @param recordId
       * @return the record json
       */
      public JsonNode recordLookupNode(String recordId) {

        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

        grouperHttpClient.assignUrl(this.url + "/v1/records/" + recordId);
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);

        executeRequest(grouperHttpClient, 200, -1, "recordLookup");

        return GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
      }

      /**
       * @param jsonNode a v1 record
       * @return the user attribute values in userAttributes order
       */
      public String[] recordLookupAttributes(JsonNode jsonNode) {

        ArrayNode currentValueMapsNodes = (ArrayNode)GrouperUtil.jsonJacksonGetNode(jsonNode, "currentValueMaps");
    
        String[] pennIdPennKeyNameOrgTitleArray = new String[this.userAttributes.size()];
        
        int index =0;
        for (String userAttribute : this.userAttributes) {
          
          // pennIdPennKeyNameOrgTitleArray[0] = StringUtils.defaultString(recordLookupHelper(currentValueMapsNodes, "Penn ID"));
          pennIdPennKeyNameOrgTitleArray[index++] = StringUtils.defaultString(recordLookupHelper(currentValueMapsNodes, userAttribute));
        }
        
        
        return pennIdPennKeyNameOrgTitleArray;
        
        
    //    {
    //      "id":"MsXDs9J4",
    //      "active":true,
    //      "created":1698453865527,
    //      "updated":null,
    //      "sequenceId":7,
    //      "dueDate":null,
    //      "stepDueDate":null,
    //      "userDate":null,
    //      "activeDate":null,
    //      "enteredStepDate":1698453895000,
    //      "completedDate":1698453895000,
    //      "lastCompletedDate":1698453895000,
    //      "step":{
    //         "id":"ir8Mev3u",
    //         "active":true,
    //         "created":1694715864858,
    //         "updated":1698248196846,
    //         "name":"Active Contacts Repository",
    //         "priority":2,
    //         "userGroupOperationType":"FULL_ACCESS",
    //         "xpos":200,
    //         "ypos":55,
    //         "isPublic":false,
    //         "commentsEnabled":false,
    //         "documentsEnabled":false,
    //         "externalUserMfaRequired":false,
    //         "uxriEnabled":false,
    //         "sla":{
    //            "enabled":false,
    //            "duration":0
    //         },
    //         "assignableUserType":"APP_USERS",
    //         "stepType":"END",
    //         "end":true,
    //         "chain":false,
    //         "origin":false
    //      },
    //      "origin":{
    //         "id":"ndp4mlh5",
    //         "active":true,
    //         "created":1694715864860,
    //         "updated":1698345358203,
    //         "name":"Create Contact",
    //         "priority":1,
    //         "userGroupOperationType":"FULL_ACCESS",
    //         "xpos":55,
    //         "ypos":55,
    //         "isPublic":false,
    //         "commentsEnabled":false,
    //         "documentsEnabled":false,
    //         "externalUserMfaRequired":false,
    //         "uxriEnabled":false,
    //         "sla":{
    //            "enabled":false,
    //            "duration":0
    //         },
    //         "assignableUserType":"APP_USERS",
    //         "stepType":"ORIGIN",
    //         "end":false,
    //         "chain":false,
    //         "origin":true
    //      },
    //      "creator":{
    //         "status":"Active",
    //         "id":"BerYFbgQ",
    //         "active":true,
    //         "created":1698347073689,
    //         "updated":1698458359021,
    //         "valueType":"User",
    //         "discriminator":"Active",
    //         "textValue":"Chris Hyzer (mchyzer@upenn.edu)",
    //         "numericValue":1.0,
    //         "isDefault":false,
    //         "archived":false,
    //         "priority":0,
    //         "email":"mchyzer@upenn.edu",
    //         "company":null,
    //         "imageUrl":null,
    //         "status":"Active",
    //         "tier":"SECONDARY",
    //         "pricingUserTier":"POWER",
    //         "first":"Chris",
    //         "last":"Hyzer",
    //         "mfaEnabled":false,
    //         "mfaSetup":false,
    //         "autoprovisioned":false,
    //         "scimStatus":null,
    //         "lastLogin":null,
    //         "lastDeactivated":null,
    //         "name":"Chris Hyzer",
    //         "locked":false,
    //         "serviceAccount":false,
    //         "stepPermissionSetIds":[
    //            
    //         ],
    //         "disabled":false,
    //         "roleIds":[
    //            
    //         ],
    //         "external":false,
    //         "superUser":false,
    //         "empty":false,
    //         "fieldId":null,
    //         "idOrTransientId":"BerYFbgQ",
    //         "transientIdOrId":"BerYFbgQ"
    //      },
    //      "user":null,
    //      "secondaryAssignees":[
    //         
    //      ],
    //      "userGroups":[
    //         
    //      ],
    //      "currentValueMaps":[
    //         {
    //            "id":"Cf2Dhhbl",
    //            "active":true,
    //            "created":1698453890464,
    //            "updated":null,
    //            "currentValues":[
    //               {
    //                  "id":"dGAR2fvM",
    //                  "active":true,
    //                  "created":1698453890459,
    //                  "updated":1698453890463,
    //                  "valueType":"Common",
    //                  "discriminator":"Common",
    //                  "textValue":"mchyzer",
    //                  "numericValue":null,
    //                  "isDefault":false,
    //                  "archived":false,
    //                  "priority":0,
    //                  "description":null,
    //                  "empty":false,
    //                  "fieldId":null,
    //                  "idOrTransientId":"dGAR2fvM",
    //                  "transientIdOrId":"dGAR2fvM"
    //               }
    //            ],
    //            "field":{
    //               "fieldType":"TEXT",
    //               "id":"CoVm58LF",
    //               "active":true,
    //               "created":1698347845398,
    //               "updated":null,
    //               "name":"PennKey",
    //               "labels":[
    //                  
    //               ],
    //               "label":"PennKey",
    //               "tooltip":null,
    //               "helpText":null,
    //               "format":"DECIMAL",
    //               "currency":null,
    //               "analysisEstimate":null,
    //               "analysisType":null,
    //               "crossWorkflowCalculation":false,
    //               "analysisOutputField":false
    //            },
    //            "expressionResult":null,
    //            "copyable":true
    //         },
    //         {
    //            "id":"ScfqERcC",
    //            "active":true,
    //            "created":1698453886332,
    //            "updated":null,
    //            "currentValues":[
    //               {
    //                  "id":"fI7VDJ6H",
    //                  "active":true,
    //                  "created":1698453886327,
    //                  "updated":1698453886330,
    //                  "valueType":"Common",
    //                  "discriminator":"Common",
    //                  "textValue":"Programmer",
    //                  "numericValue":null,
    //                  "isDefault":false,
    //                  "archived":false,
    //                  "priority":0,
    //                  "description":null,
    //                  "empty":false,
    //                  "fieldId":null,
    //                  "idOrTransientId":"fI7VDJ6H",
    //                  "transientIdOrId":"fI7VDJ6H"
    //               }
    //            ],
    //            "field":{
    //               "fieldType":"TEXT",
    //               "id":"eMuyO21R",
    //               "active":true,
    //               "created":1698347825058,
    //               "updated":null,
    //               "name":"Contact Title",
    //               "labels":[
    //                  
    //               ],
    //               "label":"Contact Title",
    //               "tooltip":null,
    //               "helpText":null,
    //               "format":"DECIMAL",
    //               "currency":null,
    //               "analysisEstimate":null,
    //               "analysisType":null,
    //               "crossWorkflowCalculation":false,
    //               "analysisOutputField":false
    //            },
    //            "expressionResult":null,
    //            "copyable":true
    //         },
    //         {
    //            "id":"iygP6xQ9",
    //            "active":true,
    //            "created":1698453882985,
    //            "updated":null,
    //            "currentValues":[
    //               {
    //                  "id":"oT8RHACt",
    //                  "active":true,
    //                  "created":1698453882979,
    //                  "updated":1698453882983,
    //                  "valueType":"Common",
    //                  "discriminator":"Common",
    //                  "textValue":"ISC",
    //                  "numericValue":null,
    //                  "isDefault":false,
    //                  "archived":false,
    //                  "priority":0,
    //                  "description":null,
    //                  "empty":false,
    //                  "fieldId":null,
    //                  "idOrTransientId":"oT8RHACt",
    //                  "transientIdOrId":"oT8RHACt"
    //               }
    //            ],
    //            "field":{
    //               "fieldType":"TEXT",
    //               "id":"CQ43SsXJ",
    //               "active":true,
    //               "created":1698347808353,
    //               "updated":null,
    //               "name":"Contact Organization",
    //               "labels":[
    //                  
    //               ],
    //               "label":"Contact Organization",
    //               "tooltip":null,
    //               "helpText":null,
    //               "format":"DECIMAL",
    //               "currency":null,
    //               "analysisEstimate":null,
    //               "analysisType":null,
    //               "crossWorkflowCalculation":false,
    //               "analysisOutputField":false
    //            },
    //            "expressionResult":null,
    //            "copyable":true
    //         },
    //         {
    //            "id":"ug5fGf3O",
    //            "active":true,
    //            "created":1698453881626,
    //            "updated":null,
    //            "currentValues":[
    //               {
    //                  "id":"E7j6f2mx",
    //                  "active":true,
    //                  "created":1698453881620,
    //                  "updated":1698453881624,
    //                  "valueType":"Common",
    //                  "discriminator":"Common",
    //                  "textValue":"Chris Hyzer",
    //                  "numericValue":null,
    //                  "isDefault":false,
    //                  "archived":false,
    //                  "priority":0,
    //                  "description":null,
    //                  "empty":false,
    //                  "fieldId":null,
    //                  "idOrTransientId":"E7j6f2mx",
    //                  "transientIdOrId":"E7j6f2mx"
    //               }
    //            ],
    //            "field":{
    //               "fieldType":"TEXT",
    //               "id":"JNS9REj4",
    //               "active":true,
    //               "created":1694793755583,
    //               "updated":1694795284140,
    //               "name":"Contact Name",
    //               "labels":[
    //                  
    //               ],
    //               "label":"Contact Name",
    //               "tooltip":"Format: FirstName LastName",
    //               "helpText":"",
    //               "format":"DECIMAL",
    //               "currency":null,
    //               "analysisEstimate":null,
    //               "analysisType":null,
    //               "crossWorkflowCalculation":false,
    //               "analysisOutputField":false
    //            },
    //            "expressionResult":null,
    //            "copyable":true
    //         },
    //         {
    //            "id":"amVNvxut",
    //            "active":true,
    //            "created":1698453877486,
    //            "updated":null,
    //            "currentValues":[
    //               {
    //                  "id":"ttUmL6Pv",
    //                  "active":true,
    //                  "created":1698453877401,
    //                  "updated":1698453877469,
    //                  "valueType":"Common",
    //                  "discriminator":"Common",
    //                  "textValue":"10021368",
    //                  "numericValue":null,
    //                  "isDefault":false,
    //                  "archived":false,
    //                  "priority":0,
    //                  "description":null,
    //                  "empty":false,
    //                  "fieldId":null,
    //                  "idOrTransientId":"ttUmL6Pv",
    //                  "transientIdOrId":"ttUmL6Pv"
    //               }
    //            ],
    //            "field":{
    //               "fieldType":"TEXT",
    //               "id":"Dc9LsPyL",
    //               "active":true,
    //               "created":1698347602860,
    //               "updated":null,
    //               "name":"Penn ID",
    //               "labels":[
    //                  
    //               ],
    //               "label":"Penn ID",
    //               "tooltip":null,
    //               "helpText":null,
    //               "format":"DECIMAL",
    //               "currency":null,
    //               "analysisEstimate":null,
    //               "analysisType":null,
    //               "crossWorkflowCalculation":false,
    //               "analysisOutputField":false
    //            },
    //            "expressionResult":null,
    //            "copyable":true
    //         }
    //      ],
    //      "assignee":null,
    //      "assigneeType":null,
    //      "daysUntilDue":{
    //         "value":0,
    //         "label":"COMPLETED"
    //      },
    //      "activeForRecord":null,
    //      "workflow":{
    //         "id":"QwFrqpww",
    //         "active":true,
    //         "created":1694715864869,
    //         "updated":1698345496091,
    //         "name":"Contacts",
    //         "recordPrefix":"Contact",
    //         "allowGroups":false,
    //         "requireGroups":false,
    //         "xpos":948,
    //         "ypos":182,
    //         "priority":8,
    //         "sla":{
    //            "enabled":false,
    //            "duration":0
    //         },
    //         "allowAec":false,
    //         "sequence":null,
    //         "application":null,
    //         "userGroups":[
    //            
    //         ],
    //         "primaryField":{
    //            "id":"o46OTlDw",
    //            "active":true,
    //            "created":1698454748238,
    //            "updated":null,
    //            "field":{
    //               "fieldType":"TEXT",
    //               "id":"Dc9LsPyL",
    //               "active":true,
    //               "created":1698347602860,
    //               "updated":null,
    //               "name":"Penn ID",
    //               "labels":[
    //                  
    //               ],
    //               "label":"Penn ID",
    //               "tooltip":null,
    //               "helpText":null,
    //               "format":"DECIMAL",
    //               "currency":null,
    //               "analysisEstimate":null,
    //               "analysisType":null,
    //               "crossWorkflowCalculation":false,
    //               "analysisOutputField":false
    //            },
    //            "systemField":null,
    //            "name":"Penn ID"
    //         },
    //         "workflowType":"WORKFLOW"
    //      },
    //      "name":"Contact-7",
    //      "viewableTitle":"10021368",
    //      "status":"COMPLETE",
    //      "initialStateEndStep":false,
    //      "workflowDueDate":null,
    //      "recordType":"RECORD",
    //      "isPublic":false
    //   }
      }

    public String recordLookupHelper(ArrayNode currentValueMapsNodes, String fieldNameToFind) {
        
        
        for (int i=0;i<currentValueMapsNodes.size();i++) {
          JsonNode currentValueMapNode = currentValueMapsNodes.get(i);
    
          JsonNode fieldNode = GrouperUtil.jsonJacksonGetNode(currentValueMapNode, "field");
    
          String fieldName = GrouperUtil.jsonJacksonGetString(fieldNode, "name");
          if (!StringUtils.equals(fieldName, fieldNameToFind)) {
            continue;
          }
    
          ArrayNode currentValuesNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(currentValueMapNode, "currentValues");
          if (currentValueMapNode == null || currentValuesNode.size() != 1) {
            continue;
          }
          
          JsonNode currentValueNode = currentValuesNode.get(0);
          String currentValue = GrouperUtil.jsonJacksonGetString(currentValueNode, "textValue");
          return currentValue;
    
        }
      
        
    //    "currentValueMaps":[
    //{
    // "id":"Cf2Dhhbl",
    // "active":true,
    // "created":1698453890464,
    // "updated":null,
    // "currentValues":[
    //    {
    //       "id":"dGAR2fvM",
    //       "active":true,
    //       "created":1698453890459,
    //       "updated":1698453890463,
    //       "valueType":"Common",
    //       "discriminator":"Common",
    //       "textValue":"mchyzer",
    //       "numericValue":null,
    //       "isDefault":false,
    //       "archived":false,
    //       "priority":0,
    //       "description":null,
    //       "empty":false,
    //       "fieldId":null,
    //       "idOrTransientId":"dGAR2fvM",
    //       "transientIdOrId":"dGAR2fvM"
    //    }
    // ],
    // "field":{
    //    "fieldType":"TEXT",
    //    "id":"CoVm58LF",
    //    "active":true,
    //    "created":1698347845398,
    //    "updated":null,
    //    "name":"PennKey",
    //    "labels":[
    //       
    //    ],
    //    "label":"PennKey",
    //    "tooltip":null,
    //    "helpText":null,
    //    "format":"DECIMAL",
    //    "currency":null,
    //    "analysisEstimate":null,
    //    "analysisType":null,
    //    "crossWorkflowCalculation":false,
    //    "analysisOutputField":false
    // },
    // "expressionResult":null,
    // "copyable":true
    //},
        return null;
      }

    public String findField(ArrayNode fieldNodes, String fieldName) {
    
      for (int i=0;i<fieldNodes.size();i++) {
        JsonNode fieldNode = fieldNodes.get(i);
        String name = GrouperUtil.jsonJacksonGetString(fieldNode, "name");
        String id = GrouperUtil.jsonJacksonGetString(fieldNode, "id");
        if (StringUtils.equals(name, fieldName)) {
          return id;
        }
      }
    
      //  [
      //   {
      //      "fieldType":"TEXT",
      //      "id":"CoVm58LF",
      //      "name":"PennKey",
      //      "labels":[
      //         "Field",
      //         "Infinite",
      //         "TextField"
      //      ],
      //      "label":"PennKey",
      //      "tooltip":null,
      //      "helpText":null,
      //      "format":"DECIMAL",
      //      "currency":null,
      //      "currentValues":[
      //         
      //      ],
      //      "defaultValues":[
      //         
      //      ],
      //      "analysisEstimate":null,
      //      "analysisType":null,
      //      "operators":[
      //         "NULL",
      //         "NOT_NULL",
      //         "EQUALS",
      //         "NOT_EQUALS",
      //         "CONTAINS",
      //         "DOES_NOT_CONTAIN"
      //      ],
      //      "convertibleTo":[
      //         "TEXT_AREA"
      //      ],
      //      "pattern":null,
      //      "message":null,
      //      "fieldType":"TEXT",
      //      "discrete":false,
      //      "valueType":"Common",
      //      "global":false,
      //      "crossWorkflowCalculation":false,
      //      "analysisOutputField":false
      //   },
      throw new RuntimeException("Cant find field! '" + fieldName + "'");
    
    }

    public void findFields() {
    
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/fields/workflow/" + contactsWorkflowId + "/values");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      grouperHttpClient.addHeader("Accept", "application/json");
      executeRequest(grouperHttpClient, 200, -1, "");
    
      JsonNode jsonNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
      
      ArrayNode fieldNodes = (ArrayNode)jsonNode;
      
      for (String userAttribute : this.userAttributes) {
        // theState.fieldIdPennKey = findField(fieldNodes, "PennKey");
        String fieldId = findField(fieldNodes, userAttribute);
        fieldNameToId.put(userAttribute, fieldId);
      }
    
      //  [
      //   {
      //      "fieldType":"TEXT",
      //      "id":"CoVm58LF",
      //      "name":"PennKey",
      //      "labels":[
      //         "Field",
      //         "Infinite",
      //         "TextField"
      //      ],
      //      "label":"PennKey",
      //      "tooltip":null,
      //      "helpText":null,
      //      "format":"DECIMAL",
      //      "currency":null,
      //      "currentValues":[
      //         
      //      ],
      //      "defaultValues":[
      //         
      //      ],
      //      "analysisEstimate":null,
      //      "analysisType":null,
      //      "operators":[
      //         "NULL",
      //         "NOT_NULL",
      //         "EQUALS",
      //         "NOT_EQUALS",
      //         "CONTAINS",
      //         "DOES_NOT_CONTAIN"
      //      ],
      //      "convertibleTo":[
      //         "TEXT_AREA"
      //      ],
      //      "pattern":null,
      //      "message":null,
      //      "fieldType":"TEXT",
      //      "discrete":false,
      //      "valueType":"Common",
      //      "global":false,
      //      "crossWorkflowCalculation":false,
      //      "analysisOutputField":false
      //   },
    
    }

    /**
     * create a contact record in the create step
     * @param attributes user attribute name to value
     * @return the new record id (the response is the record), or null if the response did not have one
     */
    public String recordCreate(Map<String, String> attributes) {

      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/records/public");
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      
      ObjectNode jsonNode = GrouperUtil.jsonJacksonNode();
      ObjectNode stepNode = GrouperUtil.jsonJacksonNode();
      jsonNode.set("step", stepNode);
      GrouperUtil.jsonJacksonAssignString(stepNode, "id", stepIdCreateContact);
    
      ArrayNode currentValueMaps = GrouperUtil.jsonJacksonArrayNode();
      jsonNode.set("currentValueMaps", currentValueMaps);
      
      for (String userAttribute : this.userAttributes) {
        String fieldId = fieldNameToId.get(userAttribute);
        if (fieldId == null) {
          throw new RuntimeException("Cant find fieldId for userAttribute: " + userAttribute);
        }
        String attributeValue = attributes.get(userAttribute);
        currentValueMaps.add(valueMapNodeCreate(fieldId, GrouperUtil.defaultString(attributeValue)));
      }
      
      String requestBody = GrouperUtil.jsonJacksonToString(jsonNode);
      grouperHttpClient.assignBody(requestBody);
      
      // step, workflow, currentValueMaps
      
      //  {
      //    "step": {
      //      "id": "STEP_ID"
      //    },
      //    "currentValueMaps": [
      //      {
      //        "currentValues": [
      //          {
      //            "textValue": "John Doe",
      //            "discriminator": "Common"
      //          }
      //        ],
      //        "field": {
      //          "id": "TEXT_FIELD_ID",
      //          "fieldType": "TEXT"
      //        }
      //      },
      //      {
      //        "currentValues": [
      //          {
      //            "id": "SELECTED_CURRENT_VALUE_ID",
      //            "textValue": "Developer",
      //            "discriminator": "Common"
      //          }
      //        ],
      //        "field": {
      //          "id": "SELECT_FIELD_ID",
      //          "fieldType": "SELECT"
      //          }
      //      }
      //    ]
      //  }
      //  
    
      executeRequest(grouperHttpClient, 200, 201, "recordCreate");

      // the response is the created record, same shape as GET /v1/records/{id}, id at the top level
      String recordId = null;
      try {
        recordId = GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody()), "id");
      } catch (RuntimeException re) {
        LOG.error("Could not parse record id from create response", re);
      }
      if (StringUtils.isBlank(recordId)) {
        GrouperUtil.mapAddValue(this.debugMap, "recordCreateNoIdInResponse", 1);
      }
      return recordId;
    }

    /**
     * key (Penn ID) to record id for records created in this run.  The provisioning logic can ask to insert
     * the same person again later in the run if it could not read the new record back, which made two records
     * per person.  This makes the second ask reuse the first record instead of creating another one.
     */
    private Map<String, String> keyToRecordIdCreatedThisRun = new java.util.concurrent.ConcurrentHashMap<String, String>();

    /**
     * @param key Penn ID
     * @return record id if this person was already created in this run (empty string if created but no id came back)
     */
    public String recordIdCreatedThisRun(String key) {
      return key == null ? null : this.keyToRecordIdCreatedThisRun.get(key);
    }

    /**
     * @param key Penn ID
     * @param recordId the new record id, may be null
     */
    public void recordCreatedThisRun(String key, String recordId) {
      if (key != null) {
        this.keyToRecordIdCreatedThisRun.put(key, StringUtils.defaultString(recordId));
      }
    }

    public ObjectNode valueMapNodeCreate(String fieldId, String fieldValue) {
    
      //      {
      //        "currentValues": [
      //          {
      //            "textValue": "John Doe",
      //            "discriminator": "Common"
      //          }
      //        ],
      //        "field": {
      //          "id": "TEXT_FIELD_ID",
      //          "fieldType": "TEXT"
      //        }
      //      }
    
      ObjectNode currentValueMapNode = GrouperUtil.jsonJacksonNode();
      
      ArrayNode currentValuesArrayNode = GrouperUtil.jsonJacksonArrayNode();
      currentValueMapNode.set("currentValues", currentValuesArrayNode);
      
      ObjectNode currentValueNode = GrouperUtil.jsonJacksonNode();
      currentValuesArrayNode.add((ObjectNode)currentValueNode);
      GrouperUtil.jsonJacksonAssignString(currentValueNode, "textValue", fieldValue);
      GrouperUtil.jsonJacksonAssignString(currentValueNode, "discriminator", "Common");
      
      ObjectNode fieldNode = GrouperUtil.jsonJacksonNode();
      currentValueMapNode.set("field", fieldNode);
      GrouperUtil.jsonJacksonAssignString(fieldNode, "id", fieldId);
      GrouperUtil.jsonJacksonAssignString(fieldNode, "fieldType", "TEXT");
      
      return currentValueMapNode;
    
    }

    public void recordUpdate(String recordId, Map<String, String> attributes) {
    
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/records/" + recordId);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.patch);
      grouperHttpClient.addHeader("Content-Type", "application/json");
      
      //  [
      //   {
      //     "fieldId": "string",
      //     "values": [
      //       "string"
      //     ]
      //   }
      // ]
      
      ArrayNode patchValueArrayNode = GrouperUtil.jsonJacksonArrayNode();
    
      for (String userAttribute : this.userAttributes) {
        String fieldId = fieldNameToId.get(userAttribute);
        if (fieldId == null) {
          throw new RuntimeException("Cant find fieldId for userAttribute: " + userAttribute);
        }
        if (!attributes.containsKey(userAttribute)) {
          continue;
        }
        String attributeValue = attributes.get(userAttribute);
        if (attributeValue != null) {
          patchValueArrayNode.add((ObjectNode)recordUpdateNode(fieldId, StringUtils.defaultString(attributeValue)));
        }
      }
            
      String requestBody = GrouperUtil.jsonJacksonToString(patchValueArrayNode);
      grouperHttpClient.assignBody(requestBody);
    
      executeRequest(grouperHttpClient, 200, 204, "recordUpdate");
    
    }

    public ObjectNode recordUpdateNode(String fieldId, String value) {
      //   {
      //     "fieldId": "string",
      //     "values": [
      //       "string"
      //     ]
      //   }
      // ]
      
      ObjectNode jsonNode = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(jsonNode, "fieldId", fieldId);
      ArrayNode valuesArrayNode = GrouperUtil.jsonJacksonArrayNode();
      
      jsonNode.set("values", valuesArrayNode);
    
      valuesArrayNode.add((String)value);
      
      return jsonNode;
    
    }

    /**
     * page zero indexed
     * @param url
     * @param accessToken
     * @param page
     * @param size
     * @return record ids
     */
    public Set<String> recordSearch(String stepId, int page, int size, int[] recordsRetrievedSize) {
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
      grouperHttpClient.assignUrl(this.url + "/v1/records/search?workflow=" + contactsWorkflowId + "&page=" + page + "&size=" + size);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      
      executeRequest(grouperHttpClient, 200, -1, "recordSearch");
    
      
      String responseBody = grouperHttpClient.getResponseBody();
      
      Set<String> recordIds = new LinkedHashSet<String>();
    
      JsonNode resultNode = GrouperUtil.jsonJacksonNode(responseBody);
      ArrayNode contentNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(resultNode, "content");
      if (contentNode == null) {
        recordsRetrievedSize[0] = 0;
        return recordIds;
      }
      recordsRetrievedSize[0] = contentNode.size();
      for (int i=0;i<contentNode.size();i++) {
        JsonNode recordNode = contentNode.get(i);
        JsonNode recordEntryNode = GrouperUtil.jsonJacksonGetNode(recordNode, "record");
        JsonNode stepNode = GrouperUtil.jsonJacksonGetNode(recordEntryNode, "step");
        // b1F5eJnY
        String currentStepId = GrouperUtil.jsonJacksonGetString(stepNode, "id");
        // stepId is ir8Mev3u
        if (!StringUtils.equals(currentStepId, stepId)) {
          continue;
        }
        String recordId = GrouperUtil.jsonJacksonGetString(recordEntryNode, "id");
        recordIds.add((String)recordId);
      }
    
      return recordIds;
      //System.out.println(responseBody);
      
      //  {
      //    "properties":[
      //       {
      //          "header":"Name",
      //          "fieldType":"TEXT",
      //          "systemField":null,
      //          "recordId":"X6YW1WDY",
      //          "url":"/records/X6YW1WDY",
      //          "rawValue":{
      //             "id":"wJ3PjT7c",
      //             "active":true,
      //             "created":1698347951519,
      //             "updated":1698347951524,
      //             "valueType":"Common",
      //             "discriminator":"Common",
      //             "textValue":"Matthew Graeff",
      //             "numericValue":null,
      //             "isDefault":false,
      //             "archived":false,
      //             "priority":0,
      //             "description":null,
      //             "empty":false,
      //             "fieldId":null,
      //             "idOrTransientId":"wJ3PjT7c",
      //             "transientIdOrId":"wJ3PjT7c"
      //          },
      //          "formattedValue":"Matthew Graeff",
      //          "formattedValueList":null
      //       },
      //       {
      //          "header":"Workflow",
      //          "fieldType":null,
      //          "systemField":"WORKFLOW_NAME",
      //          "recordId":"X6YW1WDY",
      //          "url":null,
      //          "rawValue":"Contacts",
      //          "formattedValue":"Contacts",
      //          "formattedValueList":null
      //       },
      //       {
      //          "header":"Current Step",
      //          "fieldType":null,
      //          "systemField":"STEP_NAME",
      //          "recordId":"X6YW1WDY",
      //          "url":null,
      //          "rawValue":"Active Contacts Repository",
      //          "formattedValue":"Active Contacts Repository",
      //          "formattedValueList":null
      //       },
      //       {
      //          "header":"Status",
      //          "fieldType":null,
      //          "systemField":"STATUS",
      //          "recordId":"X6YW1WDY",
      //          "url":null,
      //          "rawValue":"COMPLETE",
      //          "formattedValue":"COMPLETE",
      //          "formattedValueList":null
      //       },
      //       {
      //          "header":"User",
      //          "fieldType":null,
      //          "systemField":"USER_NAME",
      //          "recordId":"X6YW1WDY",
      //          "url":null,
      //          "rawValue":null,
      //          "formattedValue":"",
      //          "formattedValueList":null
      //       }
      //    ],
      //    "record":{
      //       "id":"X6YW1WDY",
      //       "depth":0,
      //       "name":"Contact-6",
      //       "dueDate":null,
      //       "user":false,
      //       "canEdit":true,
      //       "canRead":true,
      //       "step":{
      //          "id":"ir8Mev3u",
      //          "active":true,
      //          "created":1694715864858,
      //          "updated":1698248196846,
      //          "name":"Active Contacts Repository",
      //          "priority":2,
      //          "userGroupOperationType":"FULL_ACCESS",
      //          "xpos":200,
      //          "ypos":55,
      //          "isPublic":false,
      //          "commentsEnabled":false,
      //          "documentsEnabled":false,
      //          "externalUserMfaRequired":false,
      //          "uxriEnabled":false,
      //          "sla":{
      //             "enabled":false,
      //             "duration":0
      //          },
      //          "assignableUserType":"APP_USERS",
      //          "stepType":"END",
      //          "end":true,
      //          "chain":false,
      //          "origin":false
      //       },
      //       "workflow":{
      //          "id":"QwFrqpww",
      //          "active":true,
      //          "created":1694715864869,
      //          "updated":1698345496091,
      //          "name":"Contacts",
      //          "recordPrefix":"Contact",
      //          "allowGroups":false,
      //          "requireGroups":false,
      //          "xpos":948,
      //          "ypos":182,
      //          "priority":8,
      //          "sla":{
      //             "enabled":false,
      //             "duration":0
      //          },
      //          "allowAec":false,
      //          "primaryField":null,
      //          "workflowType":"WORKFLOW"
      //       },
      //       "application":null,
      //       "jiraKey":null,
      //       "stepEnd":true,
      //       "stepId":"ir8Mev3u"
      //    }
      // },
    
    }

    public Set<String> recordSearchAll() {
      
      Set<String> result = new LinkedHashSet<String>();
      int recordsRetrievedSize[] = new int[1];
      for (int i=0;i<1000;i++) {
        
        // this retrieves records but the step id might not match, so it might return no records
        // but there could be more anyways
        Set<String> localResult = recordSearch(stepIdActiveContacts, i, 1000, recordsRetrievedSize);

        if (recordsRetrievedSize[0] == 0) {
          this.debugMap.put("recordsToLookup", result.size());
          return result;
        }
        result.addAll(localResult);
        
      }
      throw new RuntimeException("Cant search for all records!");
    }

    /**
     * @param stepId
     * @return true if a record in this step is a current contact (active, or created and not yet moved on)
     */
    public boolean isLiveStep(String stepId) {
      return stepId != null && (Strings.CS.equals(stepId, stepIdActiveContacts) || Strings.CS.equals(stepId, stepIdCreateContact));
    }

    /**
     * One LogicGate contact record as read from the v2 list
     */
    public static class LogicGateRecord {

      /** record id */
      private String id;

      /** key attribute value (Penn ID), blank for external contacts */
      private String key;

      /** current step id */
      private String stepId;

      /** created millis, Long.MAX_VALUE if unknown */
      private long created = Long.MAX_VALUE;

      /** user attribute values in userAttributes order */
      private String[] attributeValues;

      /** small json of the record for the audit snapshot */
      private ObjectNode snapshot;

      /** direct links as [linked record id, linked workflow id], null if not looked up yet */
      private List<String[]> links;

      public String getId() {
        return this.id;
      }

      public String getKey() {
        return this.key;
      }
    }

    /**
     * Retrieve all contact records and fix them up so LogicGate has one live record per person and no inactive
     * records, then return the live record of each person for the provisioner to match on Penn ID.
     *
     * Records are read with the v2 list per step (fields inline, no GET per record).  Per Penn ID:
     * <ul>
     * <li>one live record (active or create step) is kept: the one with the most direct links, then the oldest.
     * If the person has no live record but is provisionable in Grouper, the best inactive record is moved back to
     * active (reactivate) instead of having the provisioner create a new one</li>
     * <li>every other record of that person is deleted, after its direct links (contact groups and workflow records
     * like action plans) are added to the kept record</li>
     * <li>a person who is not provisionable and has only inactive records left: the records are deleted.  Their
     * workflow links are audited and reported, since nobody is left to move them to</li>
     * </ul>
     * Every change is written to prov_logicgate_audit with a snapshot.  If the provisioner is readOnly nothing is
     * changed and the audit rows are written with read_only = 'T'.  Records with no Penn ID (external contacts) are
     * left alone.
     *
     * @param provisionableKeys Penn IDs that are provisionable in Grouper (should have a live record)
     * @return record id to user attribute values (userAttributes order), one record per Penn ID
     */
    public Map<String, String[]> recordRetrieveAllV2(Set<String> provisionableKeys) {

      long startMillis = System.currentTimeMillis();

      // read every step, live steps first
      List<LogicGateRecord> allRecords = new ArrayList<LogicGateRecord>();
      allRecords.addAll(readStepV2(stepIdActiveContacts));
      allRecords.addAll(readStepV2(stepIdCreateContact));
      allRecords.addAll(readStepV2(stepIdDeleteContact));
      debugMap.put("readRecordsMillis", System.currentTimeMillis() - startMillis);

      // group by key, external contacts (no Penn ID) are not ours to clean up
      Map<String, List<LogicGateRecord>> keyToRecords = new HashMap<String, List<LogicGateRecord>>();
      for (LogicGateRecord logicGateRecord : allRecords) {
        if (StringUtils.isBlank(logicGateRecord.key)) {
          GrouperUtil.mapAddValue(debugMap, "recordCountWithNoKey", 1);
          continue;
        }
        List<LogicGateRecord> records = keyToRecords.get(logicGateRecord.key);
        if (records == null) {
          records = new ArrayList<LogicGateRecord>();
          keyToRecords.put(logicGateRecord.key, records);
        }
        records.add(logicGateRecord);
      }

      // people that need work: more than one record, or any inactive record
      List<String> keysToFix = new ArrayList<String>();
      List<LogicGateRecord> recordsNeedingLinks = new ArrayList<LogicGateRecord>();
      for (String key : keyToRecords.keySet()) {
        List<LogicGateRecord> records = keyToRecords.get(key);
        boolean hasInactive = false;
        for (LogicGateRecord logicGateRecord : records) {
          hasInactive = hasInactive || !isLiveStep(logicGateRecord.stepId);
        }
        if (records.size() > 1 || hasInactive) {
          keysToFix.add(key);
          recordsNeedingLinks.addAll(records);
        }
      }
      debugMap.put("keysToFix", keysToFix.size());

      // look up direct links of every record that might be kept or removed
      long linksStartMillis = System.currentTimeMillis();
      loadLinks(recordsNeedingLinks);
      debugMap.put("loadLinksMillis", System.currentTimeMillis() - linksStartMillis);

      // record id to attributes of the live record of each person, what the provisioner matches on
      Map<String, String[]> recordIdToAttributes = new java.util.concurrent.ConcurrentHashMap<String, String[]>();

      // people with nothing to fix: their one live record
      Set<String> keysToFixSet = new HashSet<String>(keysToFix);
      for (String key : keyToRecords.keySet()) {
        if (!keysToFixSet.contains(key)) {
          LogicGateRecord logicGateRecord = keyToRecords.get(key).get(0);
          recordIdToAttributes.put(logicGateRecord.id, logicGateRecord.attributeValues);
        }
      }

      // fix the others on a few threads, one person at a time per thread
      long fixStartMillis = System.currentTimeMillis();
      java.util.concurrent.ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(this.threads);
      try {
        List<java.util.concurrent.Future<?>> futures = new ArrayList<java.util.concurrent.Future<?>>();
        for (final String key : keysToFix) {
          final List<LogicGateRecord> records = keyToRecords.get(key);
          final boolean provisionable = provisionableKeys.contains(key);
          futures.add(executorService.submit(new Runnable() {
            @Override
            public void run() {
              LogicGateRecord kept = fixPerson(key, records, provisionable);
              if (kept != null) {
                recordIdToAttributes.put(kept.id, kept.attributeValues);
              }
            }
          }));
        }
        for (java.util.concurrent.Future<?> future : futures) {
          future.get();
        }
      } catch (Exception e) {
        throw new RuntimeException("Error fixing LogicGate records", e);
      } finally {
        executorService.shutdown();
      }
      debugMap.put("fixPeopleMillis", System.currentTimeMillis() - fixStartMillis);

      auditRetention();
      sendReport();

      debugMap.put("recordCountNonDupeTarget", recordIdToAttributes.size());
      return recordIdToAttributes;
    }

    /**
     * Fix one person: keep one live record, move the links of the others to it, delete the others.  Errors are
     * audited and do not stop the other people
     * @param key Penn ID
     * @param records all records of the person
     * @param provisionable true if the person should have a live record
     * @return the live record to give the provisioner, or null if none
     */
    private LogicGateRecord fixPerson(String key, List<LogicGateRecord> records, boolean provisionable) {

      List<LogicGateRecord> liveRecords = new ArrayList<LogicGateRecord>();
      List<LogicGateRecord> inactiveRecords = new ArrayList<LogicGateRecord>();
      for (LogicGateRecord logicGateRecord : records) {
        (isLiveStep(logicGateRecord.stepId) ? liveRecords : inactiveRecords).add(logicGateRecord);
      }

      // which record is kept.  A live record wins over an inactive one: it is already what the provisioner manages,
      // and the links of the others are moved to it anyway
      LogicGateRecord kept = null;
      if (liveRecords.size() > 0) {
        kept = bestRecord(liveRecords);
      } else if (provisionable) {
        kept = bestRecord(inactiveRecords);
      }

      // the live record returned to the provisioner, set once it is known to be live
      LogicGateRecord liveKept = kept != null && isLiveStep(kept.stepId) ? kept : null;

      try {

        // a provisionable person with only inactive records: bring the best one back instead of creating one
        if (kept != null && liveKept == null) {
          if (this.readOnly) {
            audit("reactivate", "report_only", kept, null, null, "provisionable, only inactive records", null);
          } else {
            recordMove(kept.id, stepIdActiveContacts);
            audit("reactivate", "ok", kept, null, null, "provisionable, only inactive records", null);
            liveKept = kept;
          }
        }

        // every other record: move its links to the kept record, then delete it
        for (LogicGateRecord logicGateRecord : records) {
          if (logicGateRecord == kept) {
            continue;
          }
          removeRecord(logicGateRecord, kept);
        }

      } catch (RuntimeException re) {
        GrouperUtil.mapAddValue(debugMap, "fixPersonErrors", 1);
        audit("error", "error", kept, null, null, "fixing person " + key, GrouperUtil.getFullStackTrace(re));
        this.reportLines.add("ERROR fixing Penn ID " + key + ": " + re.getMessage());
        LOG.error("Error fixing LogicGate records for " + key, re);
      }
      return liveKept;
    }

    /**
     * Remove one record: add its direct links to the kept record (if there is one), then delete it
     * @param logicGateRecord record to remove
     * @param kept record that is kept for the person, or null if the person left
     */
    private void removeRecord(LogicGateRecord logicGateRecord, LogicGateRecord kept) {

      // links of the kept record, so we do not link something twice
      Set<String> keptLinkedIds = new HashSet<String>();
      for (String[] link : GrouperUtil.nonNull(kept == null ? null : kept.links)) {
        keptLinkedIds.add(link[0]);
      }

      int workflowLinkCount = workflowLinkCount(logicGateRecord);

      // move the links
      if (kept != null) {
        for (String[] link : GrouperUtil.nonNull(logicGateRecord.links)) {
          if (keptLinkedIds.contains(link[0])) {
            continue;
          }
          String reason = "moved from " + logicGateRecord.id + " (" + this.linkWorkflowIdToName.get(link[1]) + ")";
          if (this.readOnly) {
            audit("link", "report_only", kept, link[0], link[1], reason, null);
          } else {
            try {
              linkToRecord(link[0], link[1], kept.id);
              audit("link", "ok", kept, link[0], link[1], reason, null);
              keptLinkedIds.add(link[0]);
            } catch (RuntimeException re) {
              // do not delete a record whose link could not be moved, it would lose the link
              audit("link", "error", kept, link[0], link[1], reason, GrouperUtil.getFullStackTrace(re));
              this.reportLines.add("ERROR linking " + this.linkWorkflowIdToName.get(link[1]) + " record " + link[0]
                  + " to kept record " + kept.id + " (Penn ID " + kept.key + "), " + logicGateRecord.id + " was not deleted: " + re.getMessage());
              return;
            }
          }
        }
      }

      String reason = kept != null ? "extra copy, kept " + kept.id : "person left";

      // delete
      if (this.readOnly) {
        audit("delete", "report_only", logicGateRecord, kept == null ? null : kept.id, null, reason, null);
      } else if (!reserveDelete()) {
        audit("delete", "skipped_limit", logicGateRecord, kept == null ? null : kept.id, null, reason, null);
        return;
      } else {
        recordDelete(logicGateRecord.id);
        audit("delete", "ok", logicGateRecord, kept == null ? null : kept.id, null, reason, null);
      }
      GrouperUtil.mapAddValue(debugMap, this.readOnly ? "deletesReadOnly" : "deletes", 1);

      // workflow links of a person who left are not moved anywhere, someone should know
      if (kept == null && workflowLinkCount > 0) {
        this.reportLines.add((this.readOnly ? "(read only) would delete " : "Deleted ") + recordLabel(logicGateRecord)
            + ", who left, with " + workflowLinkCount + " workflow link(s): " + linkSummary(logicGateRecord, false));
      } else if (kept != null && workflowLinkCount > 0) {
        this.reportLines.add((this.readOnly ? "(read only) would move " : "Moved ") + workflowLinkCount + " workflow link(s) from "
            + recordLabel(logicGateRecord) + " to " + kept.id + ": " + linkSummary(logicGateRecord, false));
      }
    }

    /**
     * @param logicGateRecord
     * @return name, Penn ID and record id for a report line
     */
    private String recordLabel(LogicGateRecord logicGateRecord) {
      String name = logicGateRecord.snapshot == null ? null : GrouperUtil.jsonJacksonGetString(logicGateRecord.snapshot, "name");
      return StringUtils.defaultString(name) + " (Penn ID " + logicGateRecord.key + ", record " + logicGateRecord.id + ")";
    }

    /**
     * @param logicGateRecord
     * @param groups true for contact group links, false for workflow links
     * @return e.g. Action Plan: abc123, def456; Business Impact Analysis: ghi789
     */
    private String linkSummary(LogicGateRecord logicGateRecord, boolean groups) {
      Map<String, List<String>> workflowNameToIds = new java.util.TreeMap<String, List<String>>();
      for (String[] link : GrouperUtil.nonNull(logicGateRecord.links)) {
        if (isContactGroupWorkflow(link[1]) != groups) {
          continue;
        }
        String workflowName = this.linkWorkflowIdToName.get(link[1]);
        List<String> ids = workflowNameToIds.get(workflowName);
        if (ids == null) {
          ids = new ArrayList<String>();
          workflowNameToIds.put(workflowName, ids);
        }
        ids.add(link[0]);
      }
      List<String> parts = new ArrayList<String>();
      for (String workflowName : workflowNameToIds.keySet()) {
        parts.add(workflowName + ": " + String.join(", ", workflowNameToIds.get(workflowName)));
      }
      return String.join("; ", parts);
    }

    /** names of the workflows whose links are contact group memberships */
    private Set<String> contactGroupWorkflowNames = GrouperUtil.toSet("Contact Groups", "Organizational Level Contact Group");

    /**
     * @param workflowId
     * @return true if links from this workflow are contact group memberships
     */
    private boolean isContactGroupWorkflow(String workflowId) {
      return this.contactGroupWorkflowNames.contains(this.linkWorkflowIdToName.get(workflowId));
    }

    /**
     * @param logicGateRecord
     * @return number of direct links that are not contact groups
     */
    private int workflowLinkCount(LogicGateRecord logicGateRecord) {
      int count = 0;
      for (String[] link : GrouperUtil.nonNull(logicGateRecord.links)) {
        if (!isContactGroupWorkflow(link[1])) {
          count++;
        }
      }
      return count;
    }

    /**
     * @param records
     * @return the record with the most direct links, then the oldest
     */
    private LogicGateRecord bestRecord(List<LogicGateRecord> records) {
      LogicGateRecord best = null;
      for (LogicGateRecord logicGateRecord : records) {
        if (best == null) {
          best = logicGateRecord;
          continue;
        }
        int links = GrouperUtil.length(logicGateRecord.links);
        int bestLinks = GrouperUtil.length(best.links);
        if (links > bestLinks || (links == bestLinks && logicGateRecord.created < best.created)) {
          best = logicGateRecord;
        }
      }
      return best;
    }

    /**
     * @return true if another delete is allowed this run (maxDeletesPerRun)
     */
    private boolean reserveDelete() {
      if (this.maxDeletesPerRun < 0) {
        return true;
      }
      return this.deletesThisRun.incrementAndGet() <= this.maxDeletesPerRun;
    }

    /**
     * read every record in a step with the v2 list (field values inline).  Fails if fewer records came back than
     * LogicGate says are there: a short read would make people look missing
     * @param stepId
     * @return the records
     */
    private List<LogicGateRecord> readStepV2(String stepId) {

      List<LogicGateRecord> result = new ArrayList<LogicGateRecord>();
      Set<String> recordIdsThisStep = new HashSet<String>();
      long totalElements = -1;
      for (int page=0;page<10000;page++) {

        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignUrl(this.url + "/v2/records?workflow-id=" + contactsWorkflowId + "&step-id=" + stepId
            + "&page=" + page + "&size=1000");
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
        grouperHttpClient.addHeader("Accept", "application/json");
        executeRequest(grouperHttpClient, 200, -1, "recordListV2");

        JsonNode resultNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
        JsonNode pageNode = GrouperUtil.jsonJacksonGetNode(resultNode, "page");
        if (page == 0) {
          totalElements = GrouperUtil.jsonJacksonGetLong(pageNode, "totalElements", -1L);
        }
        ArrayNode contentNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(resultNode, "content");
        if (contentNode == null || contentNode.size() == 0) {
          break;
        }
        for (int i=0;i<contentNode.size();i++) {
          JsonNode recordNode = contentNode.get(i);
          if (this.applicationId == null) {
            this.applicationId = GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonGetNode(recordNode, "application"), "id");
          }
          LogicGateRecord logicGateRecord = new LogicGateRecord();
          logicGateRecord.id = GrouperUtil.jsonJacksonGetString(recordNode, "id");
          logicGateRecord.stepId = stepId;
          logicGateRecord.attributeValues = recordV2attributes(recordNode);
          logicGateRecord.key = logicGateRecord.attributeValues[0];
          Long created = GrouperUtil.jsonJacksonGetLong(GrouperUtil.jsonJacksonGetNode(recordNode, "dates"), "created");
          logicGateRecord.created = created == null ? Long.MAX_VALUE : created;
          logicGateRecord.snapshot = snapshotV2(recordNode);
          if (recordIdsThisStep.add(logicGateRecord.id)) {
            result.add(logicGateRecord);
          }
        }
        if (page + 1 >= GrouperUtil.jsonJacksonGetLong(pageNode, "totalPages", 0L)) {
          break;
        }
      }
      if (recordIdsThisStep.size() < totalElements) {
        throw new RuntimeException("Only retrieved " + recordIdsThisStep.size() + " of " + totalElements
            + " records in step " + stepId + ", not continuing");
      }
      GrouperUtil.mapAddValue(debugMap, "recordCountStep_" + stepId, recordIdsThisStep.size());
      return result;
    }

    /** LogicGate application id of the contacts workflow, from the first record read */
    private String applicationId;

    /**
     * @param recordNode v2 record
     * @return small json for the audit snapshot: id, name, sequence number, step, created, field name to value
     */
    private ObjectNode snapshotV2(JsonNode recordNode) {
      ObjectNode snapshot = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(snapshot, "id", GrouperUtil.jsonJacksonGetString(recordNode, "id"));
      GrouperUtil.jsonJacksonAssignString(snapshot, "name", GrouperUtil.jsonJacksonGetString(recordNode, "name"));
      GrouperUtil.jsonJacksonAssignString(snapshot, "recordName", GrouperUtil.jsonJacksonGetString(recordNode, "recordName"));
      GrouperUtil.jsonJacksonAssignLong(snapshot, "sequenceNumber", GrouperUtil.jsonJacksonGetLong(recordNode, "sequenceNumber"));
      JsonNode stepNode = GrouperUtil.jsonJacksonGetNode(recordNode, "currentStep");
      GrouperUtil.jsonJacksonAssignString(snapshot, "stepId", GrouperUtil.jsonJacksonGetString(stepNode, "id"));
      GrouperUtil.jsonJacksonAssignString(snapshot, "stepName", GrouperUtil.jsonJacksonGetString(stepNode, "name"));
      GrouperUtil.jsonJacksonAssignLong(snapshot, "created", GrouperUtil.jsonJacksonGetLong(GrouperUtil.jsonJacksonGetNode(recordNode, "dates"), "created"));
      ObjectNode fieldsNode = GrouperUtil.jsonJacksonNode();
      ArrayNode fieldsArray = (ArrayNode)GrouperUtil.jsonJacksonGetNode(recordNode, "fields");
      for (int i=0;fieldsArray != null && i<fieldsArray.size();i++) {
        JsonNode fieldNode = fieldsArray.get(i);
        ArrayNode valuesNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(fieldNode, "values");
        if (valuesNode != null && valuesNode.size() > 0) {
          GrouperUtil.jsonJacksonAssignString(fieldsNode, GrouperUtil.jsonJacksonGetString(fieldNode, "name"),
              GrouperUtil.jsonJacksonGetString(valuesNode.get(0), "textValue"));
        }
      }
      snapshot.set("fields", fieldsNode);
      return snapshot;
    }

    /**
     * the workflows a contact can be linked to: the other workflows in the contacts application
     */
    private synchronized void findLinkWorkflows() {
      if (this.linkWorkflowIdToName != null) {
        return;
      }
      GrouperUtil.assertion(!StringUtils.isBlank(this.applicationId), "LogicGate application id of the contacts workflow is not known");
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignUrl(this.url + "/v2/workflows?application-id=" + this.applicationId + "&size=1000");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      grouperHttpClient.addHeader("Accept", "application/json");
      executeRequest(grouperHttpClient, 200, -1, "workflowList");
      ArrayNode contentNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody()), "content");
      Map<String, String> result = new LinkedHashMap<String, String>();
      for (int i=0;contentNode != null && i<contentNode.size();i++) {
        String workflowId = GrouperUtil.jsonJacksonGetString(contentNode.get(i), "id");
        if (!Strings.CS.equals(workflowId, contactsWorkflowId)) {
          result.put(workflowId, GrouperUtil.jsonJacksonGetString(contentNode.get(i), "name"));
        }
      }
      this.linkWorkflowIdToName = result;
      debugMap.put("linkWorkflowCount", result.size());
    }

    /**
     * Look up the direct links of these records.  Asking per record costs one call per linking workflow, so for a
     * lot of records (the first cleanup) it is far cheaper to go the other way: read every record of the linking
     * workflows and ask each which contacts it links to
     * @param records
     */
    private void loadLinks(List<LogicGateRecord> records) {
      if (records.size() == 0) {
        return;
      }
      findLinkWorkflows();
      if (records.size() > this.reverseLinkScanThreshold) {
        Map<String, List<String[]>> contactIdToLinks = reverseLinkScan();
        for (LogicGateRecord logicGateRecord : records) {
          logicGateRecord.links = GrouperUtil.nonNull(contactIdToLinks.get(logicGateRecord.id));
        }
        debugMap.put("linkLookup", "reverseScan");
        return;
      }
      debugMap.put("linkLookup", "perRecord");
      runOnThreads(records, new java.util.function.Consumer<LogicGateRecord>() {
        @Override
        public void accept(LogicGateRecord logicGateRecord) {
          logicGateRecord.links = directLinks(logicGateRecord.id);
        }
      });
    }

    /**
     * @param <T>
     * @param items
     * @param consumer run for each item on this.threads threads
     */
    private <T> void runOnThreads(List<T> items, final java.util.function.Consumer<T> consumer) {
      java.util.concurrent.ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(this.threads);
      try {
        List<java.util.concurrent.Future<?>> futures = new ArrayList<java.util.concurrent.Future<?>>();
        for (final T item : items) {
          futures.add(executorService.submit(new Runnable() {
            @Override
            public void run() {
              consumer.accept(item);
            }
          }));
        }
        for (java.util.concurrent.Future<?> future : futures) {
          future.get();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        executorService.shutdown();
      }
    }

    /**
     * @param recordId a contact record
     * @return its direct links as [linked record id, workflow id], from every linking workflow
     */
    private List<String[]> directLinks(String recordId) {
      List<String[]> result = new ArrayList<String[]>();
      for (String workflowId : this.linkWorkflowIdToName.keySet()) {
        for (String linkedId : linkedRecordIds(recordId, workflowId)) {
          result.add(new String[] {linkedId, workflowId});
        }
      }
      return result;
    }

    /**
     * @param recordId
     * @param workflowId
     * @return ids of the records in that workflow directly linked to the record (depth 1), all pages
     */
    private List<String> linkedRecordIds(String recordId, String workflowId) {
      List<String> result = new ArrayList<String>();
      for (int page=0;page<1000;page++) {
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignUrl(this.url + "/v2/records/" + recordId + "/linked?workflow-id=" + workflowId
            + "&depth=1&page=" + page + "&size=1000");
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
        grouperHttpClient.addHeader("Accept", "application/json");
        executeRequest(grouperHttpClient, 200, -1, "recordLinked");
        JsonNode resultNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
        ArrayNode contentNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(resultNode, "content");
        for (int i=0;contentNode != null && i<contentNode.size();i++) {
          result.add(GrouperUtil.jsonJacksonGetString(contentNode.get(i), "id"));
        }
        if (contentNode == null || page + 1 >= GrouperUtil.jsonJacksonGetLong(GrouperUtil.jsonJacksonGetNode(resultNode, "page"), "totalPages", 0L)) {
          break;
        }
      }
      return result;
    }

    /**
     * read every record of every linking workflow and ask which contacts it directly links to
     * @return contact record id to its direct links as [linked record id, workflow id]
     */
    private Map<String, List<String[]>> reverseLinkScan() {
      final Map<String, List<String[]>> result = new java.util.concurrent.ConcurrentHashMap<String, List<String[]>>();
      List<String[]> linkingRecords = new ArrayList<String[]>();
      for (String workflowId : this.linkWorkflowIdToName.keySet()) {
        for (int page=0;page<10000;page++) {
          GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
          grouperHttpClient.assignUrl(this.url + "/v2/records?workflow-id=" + workflowId + "&page=" + page + "&size=1000");
          grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
          grouperHttpClient.addHeader("Accept", "application/json");
          executeRequest(grouperHttpClient, 200, -1, "linkingRecordList");
          JsonNode resultNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
          ArrayNode contentNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(resultNode, "content");
          for (int i=0;contentNode != null && i<contentNode.size();i++) {
            linkingRecords.add(new String[] {GrouperUtil.jsonJacksonGetString(contentNode.get(i), "id"), workflowId});
          }
          if (contentNode == null || page + 1 >= GrouperUtil.jsonJacksonGetLong(GrouperUtil.jsonJacksonGetNode(resultNode, "page"), "totalPages", 0L)) {
            break;
          }
        }
      }
      debugMap.put("reverseLinkScanRecords", linkingRecords.size());
      runOnThreads(linkingRecords, new java.util.function.Consumer<String[]>() {
        @Override
        public void accept(String[] linkingRecord) {
          for (String contactId : linkedRecordIds(linkingRecord[0], contactsWorkflowId)) {
            result.computeIfAbsent(contactId, k -> java.util.Collections.synchronizedList(new ArrayList<String[]>())).add(linkingRecord);
          }
        }
      });
      return result;
    }

    /**
     * link a record (contact group, action plan, ...) to a contact record.  batch-link fails silently if the
     * direction is wrong, so check it took and try the other direction once
     * @param linkedRecordId the contact group / workflow record
     * @param linkedWorkflowId its workflow
     * @param contactRecordId the contact record that is kept
     */
    private void linkToRecord(String linkedRecordId, String linkedWorkflowId, String contactRecordId) {
      for (boolean linkAsChildren : new boolean[] {false, true}) {
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
        grouperHttpClient.assignUrl(this.url + "/v1/records/" + linkedRecordId + "/batch-link");
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
        grouperHttpClient.addHeader("Content-Type", "application/json");
        ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
        ArrayNode recordIdsNode = GrouperUtil.jsonJacksonArrayNode();
        recordIdsNode.add(contactRecordId);
        bodyNode.set("recordIds", recordIdsNode);
        GrouperUtil.jsonJacksonAssignString(bodyNode, "workflowId", contactsWorkflowId);
        bodyNode.put("linkAsChildren", linkAsChildren);
        grouperHttpClient.assignBody(GrouperUtil.jsonJacksonToString(bodyNode));
        executeRequest(grouperHttpClient, 200, 201, "recordBatchLink");
        if (linkedRecordIds(contactRecordId, linkedWorkflowId).contains(linkedRecordId)) {
          return;
        }
      }
      throw new RuntimeException("Linked " + linkedRecordId + " to " + contactRecordId + " but the link is not there");
    }

    /**
     * permanently delete a record (what the LogicGate UI calls: DELETE with no query params; with map and
     * linked-record-id the same url unlinks instead)
     * @param recordId
     */
    private void recordDelete(String recordId) {
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignUrl(this.url + "/v1/records/" + recordId);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.delete);
      executeRequest(grouperHttpClient, 200, 204, "recordDelete");
    }

    /**
     * @param recordId
     * @param stepId step to move the record to
     */
    private void recordMove(String recordId, String stepId) {
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignUrl(this.url + "/v1/records/" + recordId + "/move/" + stepId);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.put);
      executeRequest(grouperHttpClient, 200, 201, "recordMove");
    }

    /**
     * write a row to prov_logicgate_audit.  Never fails the caller: the change in LogicGate already happened
     * @param action create, reactivate, delete, link, error
     * @param status ok, error, report_only, skipped_limit
     * @param logicGateRecord record acted on (snapshot taken from it), may be null
     * @param relatedRecordId linked record or kept record
     * @param relatedWorkflowId workflow of relatedRecordId
     * @param reason
     * @param errorMessage
     */
    public void audit(String action, String status, LogicGateRecord logicGateRecord, String relatedRecordId,
        String relatedWorkflowId, String reason, String errorMessage) {
      try {
        String snapshot = null;
        Integer groupLinkCount = null;
        Integer workflowLinkCount = null;
        if (logicGateRecord != null && logicGateRecord.snapshot != null) {
          ObjectNode snapshotNode = logicGateRecord.snapshot.deepCopy();
          if (logicGateRecord.links != null) {
            ArrayNode linksNode = GrouperUtil.jsonJacksonArrayNode();
            for (String[] link : logicGateRecord.links) {
              ObjectNode linkNode = GrouperUtil.jsonJacksonNode();
              GrouperUtil.jsonJacksonAssignString(linkNode, "recordId", link[0]);
              GrouperUtil.jsonJacksonAssignString(linkNode, "workflowId", link[1]);
              GrouperUtil.jsonJacksonAssignString(linkNode, "workflowName", this.linkWorkflowIdToName == null ? null : this.linkWorkflowIdToName.get(link[1]));
              linksNode.add(linkNode);
            }
            snapshotNode.set("links", linksNode);
            workflowLinkCount = workflowLinkCount(logicGateRecord);
            groupLinkCount = logicGateRecord.links.size() - workflowLinkCount;
          }
          snapshot = GrouperUtil.jsonJacksonToString(snapshotNode);
        }
        new GcDbAccess().sql("insert into prov_logicgate_audit (config_id, run_id, read_only, action, status, record_id, key_value, "
            + "related_record_id, related_workflow_id, reason, group_link_count, workflow_link_count, record_snapshot, error_message) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
          .addBindVar(this.configId)
          .addBindVar(this.runId)
          .addBindVar(this.readOnly ? "T" : "F")
          .addBindVar(action)
          .addBindVar(status)
          .addBindVar(logicGateRecord == null ? null : logicGateRecord.id)
          .addBindVar(logicGateRecord == null ? null : logicGateRecord.key)
          .addBindVar(relatedRecordId)
          .addBindVar(relatedWorkflowId)
          .addBindVar(StringUtils.abbreviate(reason, 400))
          .addBindVar(groupLinkCount)
          .addBindVar(workflowLinkCount)
          .addBindVar(snapshot)
          .addBindVar(StringUtils.abbreviate(errorMessage, 4000))
          .executeSql();
      } catch (RuntimeException re) {
        LOG.error("Could not write prov_logicgate_audit row: " + action + ", " + (logicGateRecord == null ? null : logicGateRecord.id), re);
        GrouperUtil.mapAddValue(debugMap, "auditErrors", 1);
      }
    }

    /**
     * delete audit rows older than auditRetentionDays
     */
    private void auditRetention() {
      if (this.auditRetentionDays < 0) {
        return;
      }
      try {
        int count = new GcDbAccess().sql("delete from prov_logicgate_audit where config_id = ? and action_time < ?")
          .addBindVar(this.configId)
          .addBindVar(new java.sql.Timestamp(System.currentTimeMillis() - this.auditRetentionDays * 24L * 60 * 60 * 1000))
          .executeSql();
        debugMap.put("auditRowsPurged", count);
      } catch (RuntimeException re) {
        LOG.error("Could not purge prov_logicgate_audit", re);
      }
    }

    /**
     * email the report lines of this run, if any and if reportEmails is set
     */
    public void sendReport() {
      if (this.reportLines.size() == 0 || StringUtils.isBlank(this.reportEmails)) {
        return;
      }
      List<String> lines = new ArrayList<String>(this.reportLines);
      java.util.Collections.sort(lines);
      StringBuilder body = new StringBuilder();
      body.append("LogicGate contacts sync (").append(this.configId).append(") ")
        .append(this.readOnly ? "would make (read only run) " : "made ")
        .append("these changes that touched links or failed.  All changes, with a snapshot of each record, are in table "
            + "prov_logicgate_audit where run_id = '").append(this.runId).append("'.\n\n");
      int max = 500;
      for (int i=0;i<lines.size() && i<max;i++) {
        body.append(lines.get(i)).append("\n");
      }
      if (lines.size() > max) {
        body.append("\n... and ").append(lines.size() - max).append(" more, see prov_logicgate_audit\n");
      }
      try {
        new GrouperEmail().setTo(this.reportEmails)
          .setSubject((this.readOnly ? "[read only] " : "") + "LogicGate contacts sync: " + lines.size() + " change(s) to review")
          .setBody(body.toString()).send();
        debugMap.put("reportLines", lines.size());
      } catch (RuntimeException re) {
        LOG.error("Could not send LogicGate report", re);
      }
      this.reportLines.clear();
    }

    /**
     * @param readOnly1 true if the provisioner is readOnly
     * @param runId1 provisioner instance id
     */
    public void assignRunState(boolean readOnly1, String runId1) {
      this.readOnly = readOnly1;
      this.runId = runId1;
    }

    /**
     * @return the report lines, for writes done outside the full retrieve (e.g. a person who left)
     */
    public List<String> getReportLines() {
      return this.reportLines;
    }

    /**
     * build a record from a v1 GET (for incremental deletes), with its direct links
     * @param recordId
     * @return the record
     */
    public LogicGateRecord recordForAudit(String recordId) {
      // v2 GET has the same shape as the v2 list (fields inline, application), so the snapshot matches the full sync's
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignUrl(this.url + "/v2/records/" + recordId);
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
      grouperHttpClient.addHeader("Accept", "application/json");
      executeRequest(grouperHttpClient, 200, -1, "recordGetV2");
      JsonNode recordNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());

      LogicGateRecord logicGateRecord = new LogicGateRecord();
      logicGateRecord.id = recordId;
      logicGateRecord.attributeValues = recordV2attributes(recordNode);
      logicGateRecord.key = logicGateRecord.attributeValues[0];
      logicGateRecord.stepId = GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonGetNode(recordNode, "currentStep"), "id");
      Long created = GrouperUtil.jsonJacksonGetLong(GrouperUtil.jsonJacksonGetNode(recordNode, "dates"), "created");
      logicGateRecord.created = created == null ? Long.MAX_VALUE : created;
      logicGateRecord.snapshot = snapshotV2(recordNode);
      if (this.applicationId == null) {
        this.applicationId = GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonGetNode(recordNode, "application"), "id");
      }
      findLinkWorkflows();
      logicGateRecord.links = directLinks(recordId);
      return logicGateRecord;
    }

    /**
     * a person left: delete their record (after auditing it and its links), reporting any workflow links
     * @param recordId
     */
    public void removeLeaver(String recordId) {
      LogicGateRecord logicGateRecord = null;
      try {
        logicGateRecord = recordForAudit(recordId);
      } catch (RuntimeException re) {
        // still delete, just with a thinner audit row
        LOG.error("Could not read record before delete: " + recordId, re);
        logicGateRecord = new LogicGateRecord();
        logicGateRecord.id = recordId;
      }
      removeRecord(logicGateRecord, null);
      if (this.deletesThisRun.get() > this.maxDeletesPerRun && this.maxDeletesPerRun >= 0) {
        throw new RuntimeException("Over logicGateApi.maxDeletesPerRun (" + this.maxDeletesPerRun + "), " + recordId + " not deleted");
      }
    }

    /**
     * @param recordNode a record from the v2 list, which has a fields array with name and values
     * @return the user attribute values in userAttributes order (blank if not set)
     */
    public String[] recordV2attributes(JsonNode recordNode) {
      String[] result = new String[this.userAttributes.size()];
      ArrayNode fieldsNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(recordNode, "fields");
      int index = 0;
      for (String userAttribute : this.userAttributes) {
        String value = null;
        for (int i=0;fieldsNode != null && i<fieldsNode.size();i++) {
          JsonNode fieldNode = fieldsNode.get(i);
          if (!Strings.CS.equals(GrouperUtil.jsonJacksonGetString(fieldNode, "name"), userAttribute)) {
            continue;
          }
          ArrayNode valuesNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(fieldNode, "values");
          if (valuesNode != null && valuesNode.size() == 1) {
            value = GrouperUtil.jsonJacksonGetString(valuesNode.get(0), "textValue");
          }
          break;
        }
        result[index++] = StringUtils.defaultString(value);
      }
      return result;
    }

  }

  private static String stepIdCreateContact = null;
  
  private static String stepIdActiveContacts = null;
  
  private static String stepIdDeleteContact = null;
  
  private static Map<String, String> fieldNameToId = new HashMap<String, String>();
  
  private static String contactsWorkflowId = null;
  
  private TheState theState = null;
  
  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    // needed so the logic can read a just-inserted record back (by the id insertEntity assigns)
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    
  }

  public void initDao() {
    
    if (this.theState != null) {
      return;
    }
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();

    this.theState = new TheState();
    
    this.logicGateCommands = this.theState.initTheState(this.getGrouperProvisioner().getConfigId());

    // readOnly: change nothing in LogicGate, only write audit rows with read_only = 'T'
    this.logicGateCommands.assignRunState(
        grouperProvisioner.retrieveGrouperProvisioningConfiguration().isReadOnly(), grouperProvisioner.getInstanceId());
  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(
      TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    initDao();

    long startNanos = System.nanoTime();


    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
    String recordId = targetEntity.getId();

    try {

      GrouperUtil.assertion(!StringUtils.isBlank(recordId), "Cannot delete an entity with blank recordId");

      // the person left: delete the record (audited with its links, workflow links reported)
      this.logicGateCommands.removeLeaver(recordId);
      this.logicGateCommands.sendReport();

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      try {

        if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLoadEntitiesToGrouperTable()) {
          if (!StringUtils.isBlank(recordId)) {
            new GcDbAccess().sql("delete from prov_logicgate_record where config_id = ? and id = ?")
              .addBindVar(this.getGrouperProvisioner().getConfigId())
              .addBindVar(recordId)
              .executeSql();
          } else {
            String firstAttribute = this.getLogicGateCommands().getUserAttributes().iterator().next();
            String firstColumnName = firstAttribute.replaceAll("[^A-Za-z0-9]", "_");
            int count = new GcDbAccess().sql("delete from prov_logicgate_record where config_id = ? and " + firstColumnName + " = ?")
              .addBindVar(this.getGrouperProvisioner().getConfigId())
              .addBindVar(targetEntity.retrieveAttributeValueString(firstAttribute))
              .select(int.class);
            if (count == 1) {
              new GcDbAccess().sql("delete from prov_logicgate_record where config_id = ? and " + firstColumnName + " = ?")
                .addBindVar(this.getGrouperProvisioner().getConfigId())
                .addBindVar(targetEntity.retrieveAttributeValueString(firstAttribute))
                .executeSql();
            }
          }
          
        }
      } catch (RuntimeException e) {
        LOG.error("Could not delete from prov table for this entity: " + targetEntity, e);
        // ignore
      }
      
    } catch (Exception exception) {
      
      targetEntity.setProvisioned(false);
      targetEntity.setException(exception);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
    
    TargetDaoDeleteEntityResponse targetDaoDeleteEntityResponse = new TargetDaoDeleteEntityResponse();
    return targetDaoDeleteEntityResponse;
  }


  @Override
  public TargetDaoInsertEntityResponse insertEntity(
      TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    initDao();

    long startNanos = System.nanoTime();

    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();
    
    try {

      Map<String, String> attributes = new HashMap<String, String>();
      for (String attributeName : this.logicGateCommands.getUserAttributes()) {

        String attributeValue = targetEntity.retrieveAttributeValueString(attributeName);
        attributes.put(attributeName, attributeValue);

      }

      // key is the first user attribute (Penn ID)
      String key = attributes.get(this.logicGateCommands.getUserAttributes().iterator().next());

      // never create the same person twice in one run
      String recordId = this.logicGateCommands.recordIdCreatedThisRun(key);
      if (recordId == null) {
        recordId = this.logicGateCommands.recordCreate(attributes);
        this.logicGateCommands.recordCreatedThisRun(key, recordId);
        LogicGateCommands.LogicGateRecord createdRecord = new LogicGateCommands.LogicGateRecord();
        createdRecord.id = recordId;
        createdRecord.key = key;
        this.logicGateCommands.audit("create", "ok", createdRecord, null, null, "in Grouper, no record in LogicGate", null);
      } else {
        GrouperUtil.mapAddValue(this.logicGateCommands.debugMap, "recordCreateSkippedAlreadyCreatedThisRun", 1);
      }

      // set the new record id so the logic can read the record back and cache the id (matching attribute "id")
      if (!StringUtils.isBlank(recordId)) {
        targetEntity.setId(recordId);
        targetEntity.assignAttributeValue("id", recordId);
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      // add to the local copy of the records so an incremental before the next full sync can find it by Penn ID
      if (!StringUtils.isBlank(recordId)) {
        this.insertGrouperTableRow(recordId, attributes);
      }

    } catch (Exception exception) {
      
      targetEntity.setProvisioned(false);
      targetEntity.setException(exception);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
    
    TargetDaoInsertEntityResponse targetDaoInsertEntityResponse = new TargetDaoInsertEntityResponse();

    return targetDaoInsertEntityResponse;
    
  }


  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }


  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }


  @Override
  public TargetDaoUpdateEntityResponse updateEntity(
      TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    initDao();

    long startNanos = System.nanoTime();

    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();
    String recordId = targetEntity.getId();

    try {
      
      GrouperUtil.assertion(!StringUtils.isBlank(recordId), "Cannot update an entity with blank recordId");
      
      Map<String, String> attributes = new HashMap<String, String>();
      
      for (ProvisioningObjectChange provisioningObjectChange : targetEntity.getInternal_objectChanges()) {
        String attributeName = provisioningObjectChange.getAttributeName();
        if (this.logicGateCommands.getUserAttributes().contains(attributeName)) {
          attributes.put(attributeName, targetEntity.retrieveAttributeValueString(attributeName));
        }
      }
      this.logicGateCommands.recordUpdate(recordId, attributes);
      
      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      try {

        if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLoadEntitiesToGrouperTable()) {
          Set<String> userAttributes = this.getLogicGateCommands().getUserAttributes();

          // Build column assignments like: "col1 = ?, col2 = ?, ..."
          List<String> setClauses = new ArrayList<>();
          List<Object> bindVars = new ArrayList<>();

          for (String attr : userAttributes) {
            String columnName = attr.replaceAll("[^A-Za-z0-9]", "_");
            setClauses.add(columnName + " = ?");

            // assuming you can get each attribute’s value dynamically, e.g.:
            Object value = targetEntity.retrieveAttributeValueString(attr);
            bindVars.add(value);
          }

          // Build SQL statement dynamically
          String tableName = "prov_logicgate_record"; // or from logicGateCommands if dynamic
          String sql = "update " + tableName + " set " + String.join(", ", setClauses)
            + " where config_id = ? and id = ?";

          // Add the WHERE clause bind variables
          bindVars.add(this.getGrouperProvisioner().getConfigId());
          bindVars.add(recordId);

          // Execute using Grouper JDBC
          GcDbAccess dbAccess = new GcDbAccess().sql(sql);

          // Add all bind variables in order
          for (Object var : bindVars) {
            dbAccess.addBindVar(var);
          }

          // Execute
          dbAccess.executeSql();
          
        }
      } catch (RuntimeException e) {
        LOG.error("Could not update from prov table for this entity: " + targetEntity, e);
        // ignore
      }
      return new TargetDaoUpdateEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }

  }
  
  public static void main(String[] args) {
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("logicGateContacts");
    grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
    LogicGateRecordsProvisionerDao logicGateRecordsProvisionerDao = (LogicGateRecordsProvisionerDao) grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();
    logicGateRecordsProvisionerDao.initDao();
    TargetDaoRetrieveAllEntitiesResponse allEntities = logicGateRecordsProvisionerDao.retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest());
    System.out.println(allEntities);
  }  
}
