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
   * THIS DOESNT WORK since the logic wants to retrieve an entity after creating it and the ID is not returned
   * after creating it, so the ID is not known and that is what is used to search for an entity.
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
      if (StringUtils.isBlank(recordId)) {
        ProvisioningEntity targetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();
        if (targetEntity == null) {
          return new TargetDaoRetrieveEntityResponse();
        }
        recordId = targetEntity.getId();
      }
      GrouperUtil.assertion(!StringUtils.isBlank(recordId), "id is required to retrieve entity");
      
      String[] pennIdPennKeyNameOrgTitle = this.logicGateCommands.recordLookup(recordId);

      ProvisioningEntity provisioningEntity = new ProvisioningEntity();
      provisioningEntity.setId(recordId);
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

      Set<String> recordSearchAll = this.logicGateCommands.recordSearchAll();
      Map<String, String[]> recordData = this.logicGateCommands.readRecordData(recordSearchAll);
      for (String recordId : recordData.keySet()) {
        String[] pennIdPennKeyNameOrgTitle = recordData.get(recordId);
        ProvisioningEntity provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(recordId);
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

  
  public LogicGateCommands getLogicGateCommands() {
    return logicGateCommands;
  }

  
  public TheState getTheState() {
    return theState;
  }

  public static class TheState {
    Map<String, Object> debugMap = new LinkedHashMap<>();

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

      return new LogicGateCommands(debugMap, url, client, secret, userAttributes);
    }
  }
  
  public static class LogicGateCommands {
  
    private String accessToken = null;
    private String client;
    private String secret;
    private String url;
    private Map<String, Object> debugMap = null;
    private Set<String> userAttributes = null;
    /**
     * keep track of these for later
     */
    private Set<String> recordIdsToDeleteDuplicate = new HashSet<String>();
    
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

    public void deleteDuplicateRecords() {
      for (String recordId : new HashSet<>(recordIdsToDeleteDuplicate)) {
        deleteRecord(recordId);
        recordIdsToDeleteDuplicate.remove(recordId);
      }
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
    
        grouperHttpClient.addHeader("Authorization", "Bearer " + accessToken());
    
        grouperHttpClient.executeRequest();
        
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

    private String accessToken() {
      if (this.accessToken == null) {
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
    
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
        grouperHttpClient.assignUrl(this.url + "/v1/records/" + recordId);
        grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
        
        executeRequest(grouperHttpClient, 200, -1, "recordLookup");
    
        //System.out.println(grouperHttpClient.getResponseBody());
        
        JsonNode jsonNode = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
        
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

    public void recordCreate(Map<String, String> attributes) {
    
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

    public Map<String, String[]> readRecordData(Set<String> recordIds) {
      
      Map<String, String[]> recordIdToAttributes = new HashMap<String, String[]>();
      Set<String> pennIds = new HashSet<String>();
      
      for (String recordId : GrouperUtil.nonNull(recordIds)) {
        
        String[] pennIdPennKeyNameOrgTitle = recordLookup(recordId);
        String pennId = pennIdPennKeyNameOrgTitle[0];
        
        // ignore external contacts
        if (StringUtils.isBlank(pennId)) {
          GrouperUtil.mapAddValue(debugMap, "recordCountWithNoPennid", 1);
          continue;
        }
        // ignore dupes
        if (pennIds.contains(pennId)) {
          recordIdsToDeleteDuplicate.add(recordId);
        } else {
          recordIdToAttributes.put(recordId, pennIdPennKeyNameOrgTitle);
        }
        debugMap.put("recordCountNonDupeTarget", recordIdToAttributes.size());
      }
      return recordIdToAttributes;
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
    //grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    
  }

  public void initDao() {
    
    if (this.theState != null) {
      return;
    }
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();

    this.theState = new TheState();
    
    this.logicGateCommands = this.theState.initTheState(this.getGrouperProvisioner().getConfigId());

  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(
      TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    initDao();
    
    this.logicGateCommands.deleteDuplicateRecords();
    
    long startNanos = System.nanoTime();

    
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
    String recordId = targetEntity.getId();
    
    try {
      
      GrouperUtil.assertion(!StringUtils.isBlank(recordId), "Cannot delete an entity with blank recordId");
      
      this.logicGateCommands.deleteRecord(recordId);
      
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
    
    this.logicGateCommands.deleteDuplicateRecords();

    long startNanos = System.nanoTime();

    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();
    
    try {

      Map<String, String> attributes = new HashMap<String, String>();
      for (String attributeName : this.logicGateCommands.getUserAttributes()) {

        String attributeValue = targetEntity.retrieveAttributeValueString(attributeName);
        attributes.put(attributeName, attributeValue);
        
      }
      
      
      this.logicGateCommands.recordCreate(attributes);
      
      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      
      // we cant insert into local table since we do not know the id
      
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
    
    this.logicGateCommands.deleteDuplicateRecords();

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
