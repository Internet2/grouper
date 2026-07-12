---
title: "Grouper ABAC Crashplan deprovisioning example"
space: Grouper
pageId: 28548902
version: 13
lastUpdated: 2026-07-01T05:43:17.335Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548902/Grouper+ABAC+Crashplan+deprovisioning+example
---

This was presented at 2023 Internet2 TechEx in Minneapolis. This is ABAC in v5.

## High level summary

1. GSH scripted daemon gets data from the target and loads tables in the Grouper database
2. Subjects which do not match are created as Grouper local entities
3. Data from the target is loaded as data fields for ABAC
4. Policy groups are created with ABAC scripts to determine who should be reviewed for deprovisioning or who should be deprovisioned

## Data table - penn_crashplan_user

```
CREATE TABLE public.penn_crashplan_user (
	user_id int8 NOT NULL,
	user_uid varchar(100) NOT NULL,
	status_active varchar(1) NOT NULL DEFAULT 'F'::character varying,
	status_blocked varchar(1) NOT NULL DEFAULT 'F'::character varying,
	username varchar(100) NULL,
	org_name varchar(200) NULL,
	user_active varchar(1) NOT NULL DEFAULT 'F'::character varying,
	"admin" varchar(1) NOT NULL DEFAULT 'F'::character varying,
	penn_id varchar(100) NULL,
	external_subject_name varchar(1024) NULL,
	CONSTRAINT penn_crashplan_user_pk PRIMARY KEY (user_id)
);
CREATE INDEX penn_crashplan_user_penn_id_idx ON public.penn_crashplan_user USING btree (penn_id);

```

Generate randomized data with spreadsheet macros

SQL insert statements

## Data table - penn_crashplan_role

```
CREATE TABLE public.penn_crashplan_role (
	user_id int8 NOT NULL,
	role_name varchar(200) NOT NULL,
	CONSTRAINT penn_crashplan_role_pk PRIMARY KEY (user_id, role_name)
);

ALTER TABLE public.penn_crashplan_role ADD CONSTRAINT penn_crashplan_role_fk FOREIGN KEY (user_id) REFERENCES public.penn_crashplan_user(user_id) ON DELETE CASCADE;
```

Generate randomized data with spreadsheet macros

SQL insert statements

## Sync those tables from crashplan

This is if you are running live. Otherwise just use the insert statements above for test data

```
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncFromData;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

//uncomment to compile in eclipse (and last line)
//public class Test58crashplan {
  
  class TheState {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    //  String crashPlanClientId = "key-028abc-xyz-e09d8e";
    //  String crashPlanClientSecret = "abc";
    //  String crashPlanUrl = "https://console.us2.crashplan.com";
    String crashPlanClientId = GrouperConfig.retrieveConfig().propertyValueString("crashPlanClientId");
    String crashPlanClientSecret = GrouperConfig.retrieveConfig().propertyValueString("crashPlanClientSecret");
    String crashPlanUrl = GrouperUtil.stripLastSlashIfExists(GrouperConfig.retrieveConfig().propertyValueString("crashPlanUrl"));

    String crashPlanLocalEntityFolderName = "penn:isc:ait:apps:crashPlan:service:emailsNotMatched";
    String crashPlanLoaderGroupNameOrg = "penn:isc:ait:apps:crashPlan:service:loader:orgLoader";
    String crashPlanLoaderGroupNameRole = "penn:isc:ait:apps:crashPlan:service:loader:roleLoader";
    String crashPlanLoaderGroupNameStatus = "penn:isc:ait:apps:crashPlan:service:loader:statusLoader";
    String crashPlanLoaderGroupNameAdmin = "penn:isc:ait:apps:crashPlan:service:loader:adminLoader";
 
    String crashPlanDeactiveUserGroupName = "penn:isc:ait:apps:crashPlan:service:policy:deactivateUsers";
    String crashPlanBlockUserGroupName = "penn:isc:ait:apps:crashPlan:service:policy:blockUsers";
    
    Stem crashPlanLocalEntityFolder = null;
    
    int crashPlanTokenExpireMinutes = GrouperConfig.retrieveConfig().propertyValueInt("crashPlanTokenExpireMinutes", 10);
    int crashPlanPageSize = GrouperConfig.retrieveConfig().propertyValueInt("crashPlanPageSize", 1000);

    // admins, whether they are active or not
    Set<BigDecimal> userIdsAdmins = new HashSet<>();
    

    String currentToken = null;
    long currentTokenRetrieved = -1;

    int USER_ID_INDEX = 0;
    int USER_UID_INDEX = 1;
    int STATUS_ACTIVE_INDEX = 2;
    int STATUS_BLOCKED_INDEX = 3;
    int USERNAME_INDEX = 4;
    int ORG_NAME_INDEX = 5;
    int ADMIN_INDEX = 6;
    int PENN_ID_INDEX = 7;
    int EXTERNAL_SUBJECT_NAME_INDEX = 8;
    
    List<Object[]> wsRows = new ArrayList<>();
    List<String> userNames = new ArrayList<String>();
    
    Map<BigDecimal, Set<String>> userIdToRoleNames = new HashMap<>();
    
    GrouperSession grouperSession = null;

  }
  
  String retrieveAccessToken(TheState theState) {

    if (theState.currentToken != null && (System.currentTimeMillis() - theState.currentTokenRetrieved) / (1000 * 60) < theState.crashPlanTokenExpireMinutes) {
      GrouperUtil.mapAddValue(theState.debugMap, "accessTokenCache", 1);
      return theState.currentToken;
    }
    GrouperUtil.mapAddValue(theState.debugMap, "accessTokenRetrieve", 1);
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient().addHeader("Accept", "application/json").
      addHeader("Content-Type", "application/json").assignUser(theState.crashPlanClientId).assignPassword(theState.crashPlanClientSecret).
      assignUrl(theState.crashPlanUrl + "/api/v3/oauth/token?grant_type=client_credentials").executeRequest();
    //curl -X POST -k -H 'Accept: application/json' -H 'Content-Type: application/json' -H 'Authorization: Basic abc:xyz' 
    // -i 'https://console.us2.crashplan.com/api/v3/oauth/token?grant_type=client_credentials'

    String responseBody = grouperHttpClient.getResponseBody();
    if (grouperHttpClient.getResponseCode() != 200) {
      throw new RuntimeException("Response code for retrieveToken: " + grouperHttpClient.getResponseCode() + ", " + responseBody);
    }
    JsonNode jsonJacksonNode = GrouperUtil.jsonJacksonNode(responseBody);
    String accessToken = GrouperUtil.jsonJacksonGetString(jsonJacksonNode, "access_token");
    GrouperUtil.assertion(!StringUtils.isBlank(accessToken), "Token is blank!");
    theState.currentToken = accessToken;
    theState.currentTokenRetrieved = System.currentTimeMillis();
    return theState.currentToken;
  }
  

  void retrieveAdmins(TheState theState) {
    
    int timeToLive=100000/theState.crashPlanPageSize;
    int pgNum = 1;
    while (true) {
      
      // dont endless loop
      if (timeToLive-- < 0) {
        throw new RuntimeException("Endless loop");
      }        

      // get an access token each time so it isnt expired
      String accessToken = retrieveAccessToken(theState);
      
      // make the call
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignGrouperHttpMethod(GrouperHttpMethod.get).addHeader("Accept", "application/json").
          addHeader("Content-Type", "application/json").addHeader("Authorization", "Bearer " + accessToken).
          assignUrl(theState.crashPlanUrl + "/api/v1/User?admins=true&pgSize=" + theState.crashPlanPageSize + "&pgNum=" + pgNum).executeRequest();
      
      // make sure valid response
      String responseBody = grouperHttpClient.getResponseBody();
      if (grouperHttpClient.getResponseCode() != 200) {
        throw new RuntimeException("Response code for retrieve admins: " + grouperHttpClient.getResponseCode() + ", " + responseBody);
      }
      //{"metadata":{"timestamp":"2023-09-15T01:45:56.460Z","params":{"pgNum":"6","pgSize":"1000"}},"data":{"totalCount":4972,"users":[]}}

      // make sure server supports page size
      JsonNode jsonJacksonNode = GrouperUtil.jsonJacksonNode(responseBody);
      int pageSizeFromResponse = GrouperUtil.jsonJacksonGetIntegerFromJsonPointer(jsonJacksonNode, "/metadata/params/pgSize");
      GrouperUtil.assertion(pageSizeFromResponse == theState.crashPlanPageSize, "Page size different " + pageSizeFromResponse + " != " + theState.crashPlanPageSize);

      // get the users
      JsonNode usersNodeJsonNode = GrouperUtil.jsonJacksonGetNodeFromJsonPointer(jsonJacksonNode, "/data/users");

      // if no users we are done
      if (usersNodeJsonNode == null) {
        break;
      }
      ArrayNode usersNode = (ArrayNode)usersNodeJsonNode;
      if (usersNode.size() == 0) {
        break;
      }
      for (int i=0;i<usersNode.size();i++) {
        //  {
        //    "userId":13224985,
        JsonNode userNode = usersNode.get(i);
        BigDecimal userId = new BigDecimal(GrouperUtil.jsonJacksonGetLong(userNode, "userId"));
        theState.userIdsAdmins.add(userId);
      }  
      pgNum++;
    }
    theState.debugMap.put("adminsRetrieve", GrouperUtil.length(theState.userIdsAdmins));
  }
  
  void retrieveUsers(TheState theState) {
    
    int timeToLive=100000/theState.crashPlanPageSize;
    int pgNum = 1;
    while (true) {
      
      // dont endless loop
      if (timeToLive-- < 0) {
        throw new RuntimeException("Endless loop");
      }        

      // get an access token each time so it isnt expired
      String accessToken = retrieveAccessToken(theState);
      
      // make the call
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignGrouperHttpMethod(GrouperHttpMethod.get).addHeader("Accept", "application/json").
          addHeader("Content-Type", "application/json").addHeader("Authorization", "Bearer " + accessToken).
          assignUrl(theState.crashPlanUrl + "/api/v1/User?incRoles=true&pgSize=" + theState.crashPlanPageSize + "&pgNum=" + pgNum).executeRequest();
      
      // make sure valid response
      String responseBody = grouperHttpClient.getResponseBody();
      if (grouperHttpClient.getResponseCode() != 200) {
        throw new RuntimeException("Response code: " + grouperHttpClient.getResponseCode() + ", " + responseBody);
      }
      //{"metadata":{"timestamp":"2023-09-15T01:45:56.460Z","params":{"pgNum":"6","pgSize":"1000"}},"data":{"totalCount":4972,"users":[]}}

      // make sure server supports page size
      JsonNode jsonJacksonNode = GrouperUtil.jsonJacksonNode(responseBody);
      int pageSizeFromResponse = GrouperUtil.jsonJacksonGetIntegerFromJsonPointer(jsonJacksonNode, "/metadata/params/pgSize");
      GrouperUtil.assertion(pageSizeFromResponse == theState.crashPlanPageSize, "Page size different " + pageSizeFromResponse + " != " + theState.crashPlanPageSize);

      // get the users
      JsonNode usersNodeJsonNode = GrouperUtil.jsonJacksonGetNodeFromJsonPointer(jsonJacksonNode, "/data/users");

      // if no users we are done
      if (usersNodeJsonNode == null) {
        break;
      }
      ArrayNode usersNode = (ArrayNode)usersNodeJsonNode;
      if (usersNode.size() == 0) {
        break;
      }
      for (int i=0;i<usersNode.size();i++) {
        JsonNode userNode = usersNode.get(i);
        
        Object[] wsRow = new Object[9];
        theState.wsRows.add(wsRow);
                
        //  int USER_ID_INDEX = 0;
        // {
        //  "userId":13224985,
        
        BigDecimal userId = new BigDecimal(GrouperUtil.jsonJacksonGetLong(userNode, "userId"));
        wsRow[theState.USER_ID_INDEX] = userId;
        
        //  int USER_UID_INDEX = 1;
        //  "userUid":"933696115269786900",
        
        wsRow[theState.USER_UID_INDEX] = GrouperUtil.jsonJacksonGetString(userNode, "userUid");

        // int STATUS_ACTIVE_INDEX = 2;
        //  "active":true,
        wsRow[theState.STATUS_ACTIVE_INDEX] = GrouperUtil.jsonJacksonGetBoolean(userNode, "active") ? "T" : "F";

        //  int STATUS_BLOCKED_INDEX = 3;
        //  "blocked":false,
        wsRow[theState.STATUS_BLOCKED_INDEX] = GrouperUtil.jsonJacksonGetBoolean(userNode, "blocked") ? "T" : "F";
        
         //  int USERNAME_INDEX = 4;
        //  "username":"{60b78e88-ead8-445c-9cfd-0b87f74ea6cd}@wharton.upenn.edu",
        wsRow[theState.USERNAME_INDEX] = GrouperUtil.jsonJacksonGetString(userNode, "username");
        theState.userNames.add((String)wsRow[theState.USERNAME_INDEX]);
        
        //  "orgName":"CTS-Client Support",
        //  int ORG_NAME_INDEX = 5;
        wsRow[theState.ORG_NAME_INDEX] = GrouperUtil.jsonJacksonGetString(userNode, "orgName");

        //  int ADMIN_INDEX = 6;
        //  "roles":[
        //           "org-admin",
        //           "desktop-user",
        //           "proe-user"
        //        ],
        Set<String> roles = GrouperUtil.nonNull(GrouperUtil.jsonJacksonGetStringSet(userNode, "roles"));
        boolean isAdmin = theState.userIdsAdmins.contains(wsRow[theState.USER_ID_INDEX]);
        for (String role : GrouperUtil.nonNull(roles)) {
          if (role.toLowerCase().contains("admin")) {
            isAdmin=true;
          }
        }
        wsRow[theState.ADMIN_INDEX] = isAdmin ? "T" : "F";
        
        theState.userIdToRoleNames.put(userId, roles);
        
      }
      pgNum++;
    }
  }
  
  public void retrievePennids(TheState theState) {
    
    Map<String, String> pennkeyToPennid = new HashMap<String, String>();
    
    List<Object[]> wsRowsFromPenn = new ArrayList<>();
    for (Object[] wsRow : theState.wsRows) {
      String username = (String)wsRow[theState.USERNAME_INDEX];
      if (username.contains("@") && (username.endsWith("@upenn.edu") || username.endsWith(".upenn.edu") ))  {
        wsRowsFromPenn.add(wsRow);
      }
    }
    
    int batchSize = 1000;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(wsRowsFromPenn, batchSize, false);
    for (int i=0;i<numberOfBatches;i++) {

      List<Object[]> batchOfRows = GrouperUtil.batchList(wsRowsFromPenn, batchSize, i);
      
      String sql = "select subject_id, subject_identifier0 from grouper_members where subject_source = 'pennperson' and subject_identifier0 in (" +
          GrouperClientUtils.appendQuestions(GrouperUtil.length(batchOfRows)) + ")";
    
      GcDbAccess gcDbAccess = new GcDbAccess().sql(sql);
            
      for (Object[] wsRow : batchOfRows) {
        String username = (String)wsRow[theState.USERNAME_INDEX];
        String pennkey = GrouperUtil.prefixOrSuffix(username, "@", true);
        gcDbAccess.addBindVar(pennkey);
      }
      
      List<Object[]> pennIdPennkeys = gcDbAccess.selectList(Object[].class);
      for (Object[] pennIdPennkey : GrouperUtil.nonNull(pennIdPennkeys)) {
        String pennId = (String)pennIdPennkey[0];
        String pennkey = (String)pennIdPennkey[1];
        pennkeyToPennid.put(pennkey, pennId);
      }
    }
    theState.debugMap.put("pennIdsFound", pennkeyToPennid.size());
    for (Object[] wsRow : wsRowsFromPenn) {
      String username = (String)wsRow[theState.USERNAME_INDEX];
      String pennkey = GrouperUtil.prefixOrSuffix(username, "@", true);
      String pennId = pennkeyToPennid.get(pennkey);
      if (!StringUtils.isBlank(pennId)) {
        wsRow[theState.PENN_ID_INDEX] = pennId;
      }
    }
  }

  public void retrieveExistingLocalEntities(TheState theState) {
    theState.crashPlanLocalEntityFolder = StemFinder.findByName(theState.grouperSession, theState.crashPlanLocalEntityFolderName, true);
    Map<String, String> displayExtensionToName = new HashMap<>();
    List<Object[]> nameDisplayExtensions = new GcDbAccess().
      sql("select name, display_extension from grouper_groups gg where gg.type_of_group = 'entity' and gg.parent_stem = ?").
      addBindVar(theState.crashPlanLocalEntityFolder.getId()).selectList(Object[].class);
    
    // put existing local entities in a map
    for (Object[] nameDisplayExtension : nameDisplayExtensions) {
      String name = (String)nameDisplayExtension[0];
      String displayExtension = (String)nameDisplayExtension[1];
      displayExtensionToName.put(displayExtension, name);
    }
    theState.debugMap.put("existingLocalEntities", displayExtensionToName.size());
    int matchedRows = 0;
    
    // see which accounts match
    for (Object[] wsRow : theState.wsRows) {
      String pennId = (String)wsRow[theState.PENN_ID_INDEX];
      if (!StringUtils.isBlank(pennId)) {
        continue;
      }
      String email = (String)wsRow[theState.USERNAME_INDEX];
      String name = displayExtensionToName.get(email);
      if (!StringUtils.isBlank(name)) {
        wsRow[theState.EXTERNAL_SUBJECT_NAME_INDEX] = name;
        matchedRows++;
      }
    }
    theState.debugMap.put("existingLocalEntityMatches", matchedRows);
    
    // remove matches
    for (Object[] wsRow : theState.wsRows) {
      String email = (String)wsRow[theState.USERNAME_INDEX];
      String externalSubjectName = (String)wsRow[theState.EXTERNAL_SUBJECT_NAME_INDEX];
      if (!StringUtils.isBlank(externalSubjectName)) {
        displayExtensionToName.remove(email);
      }
    }
    
    //delete unused
    theState.debugMap.put("deletedLocalEntities", displayExtensionToName.size());

    int logSize = 100;
    for (String externalSubjectName : displayExtensionToName.values()) {
      Group localEntity = GroupFinder.findByName(externalSubjectName, true);
      localEntity.delete();
      if (OtherJobScript.retrieveFromThreadLocal() != null) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(1);
      }

      if (logSize-- > 0) {
        theState.debugMap.put("deleted_" + GrouperUtil.extensionFromName(externalSubjectName), true);
      }
    }
    
  }

  public void createMissingLocalEntities(TheState theState) {
        
    int newLocalEntities = 0;
    
    // see which accounts need a local entity
    for (Object[] wsRow : theState.wsRows) {
      String email = (String)wsRow[theState.USERNAME_INDEX];
      String pennId = (String)wsRow[theState.PENN_ID_INDEX];
      String externalSubjectName = (String)wsRow[theState.EXTERNAL_SUBJECT_NAME_INDEX];
      if (StringUtils.isBlank(pennId) && StringUtils.isBlank(externalSubjectName)) {
        
        String extension = email.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        Group group = new GroupSave().assignName(theState.crashPlanLocalEntityFolderName + ":" + extension).
          assignDisplayExtension(email).assignTypeOfGroup(TypeOfGroup.entity).save();
        
        if (OtherJobScript.retrieveFromThreadLocal() != null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);
        }

        wsRow[theState.EXTERNAL_SUBJECT_NAME_INDEX] = group.getName();
        newLocalEntities++;
      }
    }
    theState.debugMap.put("localEntitesCreated", newLocalEntities);
        
  }

  public void syncWsRows(TheState theState) {
    
    List<String> columnNames = GrouperUtil.toList("user_id", "user_uid", "status_active", "status_blocked", 
        "username", "org_name", "admin", "penn_id", "external_subject_name");

    List<String> columnNamesPrimaryKey = GrouperUtil.toList("user_id");
  
    new GcTableSyncFromData().assignDebugMap(theState.debugMap).assignDebugMapPrefix("user_").assignConnectionName("grouper").assignTableName("penn_crashplan_user")
      .assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(theState.wsRows).sync();

    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(theState.wsRows));
    }
    
    columnNames = GrouperUtil.toList("user_id", "role_name");

    columnNamesPrimaryKey = GrouperUtil.toList("user_id, role_name");
  
    List<Object[]> userIdRoleNames = new ArrayList<>();

    for (BigDecimal userId : theState.userIdToRoleNames.keySet()) {
      Set<String> roles = theState.userIdToRoleNames.get(userId);
      for (String role : roles) {
        userIdRoleNames.add(GrouperUtil.toArrayObject(userId, role));
      }
    }
        
    new GcTableSyncFromData().assignDebugMap(theState.debugMap).assignDebugMapPrefix("role_").assignConnectionName("grouper").assignTableName("penn_crashplan_role")
      .assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(userIdRoleNames).sync();

    if (OtherJobScript.retrieveFromThreadLocal() != null) {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(userIdRoleNames));
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().
        addInsertCount(GrouperUtil.intValue(theState.debugMap.get("insertsCount"), 0));
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().
        addUpdateCount(GrouperUtil.intValue(theState.debugMap.get("updatesCount"), 0));
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().
        addDeleteCount(GrouperUtil.intValue(theState.debugMap.get("deletesCount"), 0));
      
    }

  }

  public void runLoaders(TheState theState) {
    
    // these will run in this process since this is a daemon itself and is running on the daemon server
    Group crashPlanLoaderGroupRole = GroupFinder.findByName(theState.crashPlanLoaderGroupNameRole, true);
    GrouperLoader.runJobOnceForGroup(theState.grouperSession, crashPlanLoaderGroupRole, false);

    Group crashPlanLoaderGroupAdmin = GroupFinder.findByName(theState.crashPlanLoaderGroupNameAdmin, true);
    GrouperLoader.runJobOnceForGroup(theState.grouperSession, crashPlanLoaderGroupAdmin, false);
    
    Group crashPlanLoaderGroupOrg = GroupFinder.findByName(theState.crashPlanLoaderGroupNameOrg, true);
    GrouperLoader.runJobOnceForGroup(theState.grouperSession, crashPlanLoaderGroupOrg, false);

    Group crashPlanLoaderGroupStatus = GroupFinder.findByName(theState.crashPlanLoaderGroupNameStatus, true);
    GrouperLoader.runJobOnceForGroup(theState.grouperSession, crashPlanLoaderGroupStatus, false);

  }

  public void blockAndDeactivateUsers(TheState theState) {

    Group usersToBlock = GroupFinder.findByName(theState.crashPlanBlockUserGroupName, true);
    
    Group usersToDeactivate = GroupFinder.findByName(theState.crashPlanDeactiveUserGroupName, true);
    
//    
//      // dont endless loop
//      if (timeToLive-- < 0) {
//        throw new RuntimeException("Endless loop");
//      }        
//
//      // get an access token each time so it isnt expired
//      String accessToken = retrieveAccessToken(theState);
//      
//      // make the call
//      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignGrouperHttpMethod(GrouperHttpMethod.get).addHeader("Accept", "application/json").
//          addHeader("Content-Type", "application/json").addHeader("Authorization", "Bearer " + accessToken).
//          assignUrl(theState.crashPlanUrl + "/api/v1/User?incRoles=true&pgSize=" + theState.crashPlanPageSize + "&pgNum=" + pgNum).executeRequest();
//      
//      // make sure valid response
//      String responseBody = grouperHttpClient.getResponseBody();
//      if (grouperHttpClient.getResponseCode() != 200) {
//        throw new RuntimeException("Response code: " + grouperHttpClient.getResponseCode() + ", " + responseBody);
//      }

  }
  
  public void runLogic() {

    final TheState theState = new TheState();
    try {
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          theState.grouperSession = theGrouperSession;
          retrieveAdmins(theState);
          retrieveUsers(theState);
          retrievePennids(theState);
          retrieveExistingLocalEntities(theState);
          createMissingLocalEntities(theState);
          syncWsRows(theState);
          runLoaders(theState);
          //blockAndDeactivateUsers(theState);
          return null;
        }
      });
    } catch (Exception e) {
      theState.debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw new RuntimeException(e);
    } finally {
      if (OtherJobScript.retrieveFromThreadLocal() != null) {
        OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(theState.debugMap));
      } else {
        System.out.println(GrouperUtil.mapToString(theState.debugMap));
      }
    }
    
  }
  
  runLogic();
  

//  public static void main(String[] args) {
//    new Test58crashplan().runLogic();
//  }
//
//}
```

## Represent emails which do not match eppns as Grouper local entities

Note the display extension is the email address (special chars) and the extension is similar with no special characters.

## Data field design - user rows

Data rows with single valued attributes. One row per user

| **subject_id** | **cp_active** | **cp_blocked** | **cp_known** | **cp_org** |
| --- | --- | --- | --- | --- |
| jsmith | T | F | T | med |
| abc123 | F | T | F | IT |
| kwilson | T | T | T | med |
| hgreen | T | F | T | dental |

Make a view on top of the table synced from target

```
CREATE OR REPLACE VIEW public.penn_crashplan_user_v
AS SELECT
        CASE
            WHEN pcu.penn_id IS NOT NULL THEN pcu.penn_id
            ELSE gg.id
        END AS subject_id,
    pcu.user_id AS cp_user_id,
    pcu.status_active AS cp_active,
    pcu.status_blocked AS cp_blocked,
        CASE
            WHEN pcu.penn_id IS NOT NULL THEN 'T'::text
            ELSE 'F'::text
        END AS cp_known,
    pcu.org_name AS cp_org
   FROM penn_crashplan_user pcu
     LEFT JOIN grouper_groups gg ON pcu.external_subject_name::text = gg.name::text;
```

## Data field design - user roles

Roles are multi-valued attributes assigned to users

| **subject_id** | **cp_role** |
| --- | --- |
| jsmith | user |
| abc123 | user |
| kwilson | user |
| kwilson | admin |
| hgreen | user |
| hgreen | owner |

Make a view on the role data

```
CREATE OR REPLACE VIEW public.penn_crashplan_roles_v
AS SELECT
        CASE
            WHEN pcu.penn_id IS NOT NULL THEN pcu.penn_id
            ELSE gg.id
        END AS subject_id,
    pcr.role_name AS cp_role
   FROM penn_crashplan_role pcr,
    penn_crashplan_user pcu
     LEFT JOIN grouper_groups gg ON pcu.external_subject_name::text = gg.name::text
  WHERE pcr.user_id = pcu.user_id;

```

## Privacy realm config

Either configure a privacy realm (or realms) for the data fields or re-use an existing one.

In this case it is just public for an example. But in the real world the attributes would be secured to the application team, power users, and helpdesk

grouper.properties

```
grouperPrivacyRealm.crashplan.privacyRealmName = crashplan
grouperPrivacyRealm.crashplan.privacyRealmPublic = true
```

## Data field config

```
grouperDataField.cp_active.fieldAliases = cp_active
grouperDataField.cp_active.fieldDataAssignableTo = individuals
grouperDataField.cp_active.fieldDataSource = provider
grouperDataField.cp_active.fieldDataStorePit = true
grouperDataField.cp_active.fieldDataStorePitDays = 200
grouperDataField.cp_active.fieldDataStructure = rowColumn
grouperDataField.cp_active.fieldDataType = boolean
grouperDataField.cp_active.fieldDataUse = access
grouperDataField.cp_active.fieldPrivacyRealm = crashplan
grouperDataField.cp_blocked.fieldAliases = cp_blocked
grouperDataField.cp_blocked.fieldDataAssignableTo = individuals
grouperDataField.cp_blocked.fieldDataSource = provider
grouperDataField.cp_blocked.fieldDataStorePit = true
grouperDataField.cp_blocked.fieldDataStorePitDays = 200
grouperDataField.cp_blocked.fieldDataStructure = rowColumn
grouperDataField.cp_blocked.fieldDataType = boolean
grouperDataField.cp_blocked.fieldDataUse = access
grouperDataField.cp_blocked.fieldPrivacyRealm = crashplan
grouperDataField.cp_known.fieldAliases = cp_known
grouperDataField.cp_known.fieldDataAssignableTo = individuals
grouperDataField.cp_known.fieldDataSource = provider
grouperDataField.cp_known.fieldDataStorePit = true
grouperDataField.cp_known.fieldDataStorePitDays = 200
grouperDataField.cp_known.fieldDataStructure = rowColumn
grouperDataField.cp_known.fieldDataType = boolean
grouperDataField.cp_known.fieldDataUse = access
grouperDataField.cp_known.fieldPrivacyRealm = crashplan
grouperDataField.cp_org.fieldAliases = cp_org
grouperDataField.cp_org.fieldDataAssignableTo = individuals
grouperDataField.cp_org.fieldDataSource = provider
grouperDataField.cp_org.fieldDataStorePit = true
grouperDataField.cp_org.fieldDataStorePitDays = 200
grouperDataField.cp_org.fieldDataStructure = rowColumn
grouperDataField.cp_org.fieldDataType = string
grouperDataField.cp_org.fieldDataUse = access
grouperDataField.cp_org.fieldPrivacyRealm = crashplan
grouperDataField.cp_role.fieldAliases = cp_role
grouperDataField.cp_role.fieldDataAssignableTo = individuals
grouperDataField.cp_role.fieldDataSource = provider
grouperDataField.cp_role.fieldDataStorePit = true
grouperDataField.cp_role.fieldDataStorePitDays = 200
grouperDataField.cp_role.fieldDataStructure = attribute
grouperDataField.cp_role.fieldDataType = string
grouperDataField.cp_role.fieldDataUse = access
grouperDataField.cp_role.fieldMultiValued = true
grouperDataField.cp_role.fieldPrivacyRealm = crashplan
grouperDataField.cp_user_id.fieldAliases = cp_user_id
grouperDataField.cp_user_id.fieldDataAssignableTo = individuals
grouperDataField.cp_user_id.fieldDataStorePit = true
grouperDataField.cp_user_id.fieldDataStorePitDays = 200
grouperDataField.cp_user_id.fieldDataStructure = rowColumn
grouperDataField.cp_user_id.fieldDataType = integer
grouperDataField.cp_user_id.fieldDataUse = informational
grouperDataField.cp_user_id.fieldPrivacyRealm = crashplan

```

## Data row config

We have one row configured. Some of the fields above are columns for this row

```
grouperDataRow.cp_user.rowAliases = cp_user
grouperDataRow.cp_user.rowDataField.0.colDataFieldConfigId = cp_user_id
grouperDataRow.cp_user.rowDataField.0.rowKeyField = true
grouperDataRow.cp_user.rowDataField.1.colDataFieldConfigId = cp_active
grouperDataRow.cp_user.rowDataField.2.colDataFieldConfigId = cp_blocked
grouperDataRow.cp_user.rowDataField.3.colDataFieldConfigId = cp_known
grouperDataRow.cp_user.rowDataField.4.colDataFieldConfigId = cp_org
grouperDataRow.cp_user.rowNumberOfDataFields = 5
grouperDataRow.cp_user.rowPrivacyRealm = crashplan

```

## Data provider config

The data provider links a daemon (full and incremental) with the queries needed to populate the data fields in Grouper from the system of record. In this case the system of record is two database tables in the Grouper database, which are fed from Crashplan via web service. We do this since there are SQL/LDAP data providers. We will add a GSH one which will remove a hop here but it does not exist as of v5.5.0. In this case we have one data provider with a couple queries

grouper.properties

```
grouperDataProvider.crashplan.name = crashplan
```

## Data provider query config

This data provider has two queries. One for the user rows, and one for the role attributes assigned to the user. After the query is run, there is a mapping between column name and data field.

```
grouperDataProviderQuery.crashplan_role.providerConfigId = crashplan
grouperDataProviderQuery.crashplan_role.providerQueryDataField.0.providerDataFieldAttribute = cp_role
grouperDataProviderQuery.crashplan_role.providerQueryDataField.0.providerDataFieldConfigId = cp_role
grouperDataProviderQuery.crashplan_role.providerQueryDataField.0.providerDataFieldMappingType = attribute
grouperDataProviderQuery.crashplan_role.providerQueryDataStructure = attribute
grouperDataProviderQuery.crashplan_role.providerQueryNumberOfDataFields = 1
grouperDataProviderQuery.crashplan_role.providerQuerySqlConfigId = grouper
grouperDataProviderQuery.crashplan_role.providerQuerySqlQuery = select subject_id, cp_role from penn_crashplan_roles_v
grouperDataProviderQuery.crashplan_role.providerQuerySubjectIdAttribute = subject_id
grouperDataProviderQuery.crashplan_role.providerQuerySubjectIdType = subjectId
grouperDataProviderQuery.crashplan_role.providerQueryType = sql
grouperDataProviderQuery.crashplan_user.providerConfigId = crashplan
grouperDataProviderQuery.crashplan_user.providerQueryDataField.0.providerDataFieldAttribute = cp_user_id
grouperDataProviderQuery.crashplan_user.providerQueryDataField.0.providerDataFieldConfigId = cp_user_id
grouperDataProviderQuery.crashplan_user.providerQueryDataField.0.providerDataFieldMappingType = attribute
grouperDataProviderQuery.crashplan_user.providerQueryDataField.1.providerDataFieldAttribute = cp_active
grouperDataProviderQuery.crashplan_user.providerQueryDataField.1.providerDataFieldConfigId = cp_active
grouperDataProviderQuery.crashplan_user.providerQueryDataField.1.providerDataFieldMappingType = attribute
grouperDataProviderQuery.crashplan_user.providerQueryDataField.2.providerDataFieldAttribute = cp_blocked
grouperDataProviderQuery.crashplan_user.providerQueryDataField.2.providerDataFieldConfigId = cp_blocked
grouperDataProviderQuery.crashplan_user.providerQueryDataField.2.providerDataFieldMappingType = attribute
grouperDataProviderQuery.crashplan_user.providerQueryDataField.3.providerDataFieldAttribute = cp_known
grouperDataProviderQuery.crashplan_user.providerQueryDataField.3.providerDataFieldConfigId = cp_known
grouperDataProviderQuery.crashplan_user.providerQueryDataField.3.providerDataFieldMappingType = attribute
grouperDataProviderQuery.crashplan_user.providerQueryDataField.4.providerDataFieldAttribute = cp_org
grouperDataProviderQuery.crashplan_user.providerQueryDataField.4.providerDataFieldConfigId = cp_org
grouperDataProviderQuery.crashplan_user.providerQueryDataField.4.providerDataFieldMappingType = attribute
grouperDataProviderQuery.crashplan_user.providerQueryDataStructure = row
grouperDataProviderQuery.crashplan_user.providerQueryNumberOfDataFields = 5
grouperDataProviderQuery.crashplan_user.providerQueryRowConfigId = cp_user
grouperDataProviderQuery.crashplan_user.providerQuerySqlConfigId = grouper
grouperDataProviderQuery.crashplan_user.providerQuerySqlQuery = select subject_id, cp_user_id, cp_active, cp_blocked, cp_known, cp_org from penn_crashplan_user_v
grouperDataProviderQuery.crashplan_user.providerQuerySubjectIdAttribute = subject_id
grouperDataProviderQuery.crashplan_user.providerQuerySubjectIdType = subjectId
grouperDataProviderQuery.crashplan_user.providerQueryType = sql

```

## Data provider change log queries

We are not using that in this example, but if you had triggers on tables or last updated dates, you can refresh data field values in Grouper in an incremental fashion

## Data provider daemon

When this runs, it keeps the data from the system of record in sync with data field assignments in grouper.

## Construct an ABAC policy

This will find people who are:

- active in the target
- account is not blocked
- known user (not an unmatched email address)
- in the school of medicine
- not a member of the institution anymore (gone),
  
  - or locked out
- have a license in Crashplan

```
entity.hasRow('cp_user', 
    "cp_active 
    && !cp_blocked 
    && cp_known 
    && cp_org == 'Perelman School of Medicine' ") 
  && (!entity.memberOf('penn:ref:member') 
  || entity.memberOf('penn:ref:lockout') )
  && entity.hasAttribute('cp_role', 'desktop-user')
```

## Analyze the policy

## Make an ABAC GSH template

grouperGshTemplate.crashplanAbac.groupUuidCanRun = penn\u003Aisc\u003Aait\u003Aapps\u003AcrashPlan\u003Asecurity\u003AcrashPlanAdmins  
grouperGshTemplate.crashplanAbac.gshTemplate = import org.apache.commons.lang3.StringUtils;\n\  
\n\  
import edu.internet2.middleware.grouper.GrouperSession;\n\  
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;\n\  
\n\  
//public class Test60abacTemplate {\n\  
//\n\  
// public static void main(String[] args) {\n\  
//\n\  
// GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();\n\  
// GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();\n\  
//\n\  
// String gsh_input_cp_active = "inactiveOnly"; // all, activeOnly, inactiveOnly\n\  
// String gsh_input_cp_blocked = "blockedOnly"; // all, blockedOnly, unblockedOnly\n\  
// String gsh_input_userType = "matchesPennPerson"; // all, matchesPennPerson, unknownEmailsOnly\n\  
// int gsh_input_orgCount = 1; // 0, 1, 2, 3\n\  
// String gsh_input_org0 = "Perelman School of Medicine"; // Perelman School of Medicine\n\  
// int gsh_input_roleCount = 1; // 0, 1, 2, 3, 4\n\  
// String gsh_input_role0 = "desktop-user"; // desktop-user\n\  
// String gsh_input_lockedOut = "lockedOutOnly"; // all, lockedOutOnly, notLockedOut\n\  
// String gsh_input_userAffiliation = "notAffiliates"; // all, employeesOnly, notEmployees, affiliatesOnly, notAffiliates\n\  
// String gsh_input_mfa = "all"; // all, twoStepEnrolled, notTwoStepEnrolled\n\  
 \n\  
 // entity.hasRow('cp_user',\n\  
 // "cp_active\n\  
 // && !cp_blocked\n\  
 // && cp_known\n\  
 // && cp_org == 'Perelman School of Medicine' ")\n\  
 // && (!entity.memberOf('penn\u003Aref\u003Amember')\n\  
 // || entity.memberOf('penn\u003Aref\u003Alockout') )\n\  
 // && entity.hasAttribute('cp_role', 'desktop-user')\n\  
 \n\  
 StringBuilder script = new StringBuilder('\u0024' + "{ \u005Cn");\n\  
 boolean firstClause = true;\n\  
 if (!StringUtils.equals(gsh_input_cp_active, "all") || !StringUtils.equals(gsh_input_cp_blocked, "all")\n\  
 || !StringUtils.equals(gsh_input_userType, "all") || gsh_input_orgCount > 0) {\n\  
 \n\  
 \n\  
 script.append(" entity.hasRow('cp_user',\u005Cn \u005C" ");\n\  
\n\  
 if (!StringUtils.equals(gsh_input_cp_active, "all")) {\n\  
 if (StringUtils.equals(gsh_input_cp_active, "inactiveOnly")) {\n\  
 script.append("! ");\n\  
 }\n\  
 script.append("cp_active\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 if (!StringUtils.equals(gsh_input_cp_blocked, "all")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 if (StringUtils.equals(gsh_input_cp_blocked, "unblockedOnly")) {\n\  
 script.append("! ");\n\  
 }\n\  
 script.append("cp_blocked\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 if (!StringUtils.equals(gsh_input_userType, "all")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 if (StringUtils.equals(gsh_input_userType, "unknownEmailsOnly")) {\n\  
 script.append("! ");\n\  
 }\n\  
 script.append("cp_known\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 if (gsh_input_orgCount > 0) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append("cp_org == '" + gsh_input_org0 + "'\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 script.delete(script.length()-1, script.length());\n\  
 script.append(" \u005C")\u005Cn");\n\  
\n\  
 }\n\  
\n\  
 if (gsh_input_roleCount > 0) {\n\  
 \n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append("entity.hasAttribute('cp_role', '" + gsh_input_role0 + "')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
\n\  
 if (StringUtils.equals(gsh_input_lockedOut, "notLockedOut") || StringUtils.equals(gsh_input_mfa, "twoStepEnrolled") || \n\  
 StringUtils.equalsAny(gsh_input_userAffiliation, "employeesOnly", "affiliatesOnly")) {\n\  
 if (StringUtils.equals(gsh_input_lockedOut, "notLockedOut")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append("! entity.memberOf('penn\u003Aref\u003Alockout')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 if (StringUtils.equals(gsh_input_mfa, "twoStepEnrolled")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append("entity.memberOf('penn\u003Aref\u003AmfaEnrolled')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 if (StringUtils.equals(gsh_input_userAffiliation, "employeesOnly")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append("entity.memberOf('penn\u003Aref\u003Aemployee')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 if (StringUtils.equals(gsh_input_userAffiliation, "affiliatesOnly")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append("entity.memberOf('penn\u003Aref\u003Amember')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
 }\n\  
\n\  
 if (StringUtils.equals(gsh_input_lockedOut, "lockedOutOnly") || StringUtils.equals(gsh_input_mfa, "notTwoStepEnrolled") || \n\  
 StringUtils.equalsAny(gsh_input_userAffiliation, "notEmployees", "notAffiliates")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("&& ");\n\  
 }\n\  
 script.append(" (\u005Cn");\n\  
 firstClause = true;\n\  
 if (StringUtils.equals(gsh_input_lockedOut, "lockedOutOnly")) {\n\  
 script.append(" entity.memberOf('penn\u003Aref\u003Alockout')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
\n\  
 if (StringUtils.equals(gsh_input_mfa, "notTwoStepEnrolled")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("|| ");\n\  
 }\n\  
 script.append("! entity.memberOf('penn\u003Aref\u003AmfaEnrolled')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
\n\  
 if (StringUtils.equals(gsh_input_userAffiliation, "notEmployees")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("|| ");\n\  
 }\n\  
 script.append("! entity.memberOf('penn\u003Aref\u003Aemployee')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
\n\  
 if (StringUtils.equals(gsh_input_userAffiliation, "notAffiliates")) {\n\  
 script.append(" ");\n\  
 if (!firstClause) {\n\  
 script.append("|| ");\n\  
 }\n\  
 script.append("! entity.memberOf('penn\u003Aref\u003Amember')\u005Cn");\n\  
 firstClause = false;\n\  
 }\n\  
\n\  
 script.append(" )\u005Cn");\n\  
 }\n\  
\n\  
 if (StringUtils.equalsAny(gsh_input_userType, "all", "unknownEmailsOnly")) {\n\  
 gsh_builtin_gshTemplateOutput.assignAbacIncludeInternalSubjectSources(true);\n\  
 }\n\  
 \n\  
 script.append("} ");\n\  
 gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());\n\  
\n\  
 //System.out.println(script);\n\  
\n\  
// }\n\  
//\n\  
//}  
grouperGshTemplate.crashplanAbac.input.0.defaultValue = all  
grouperGshTemplate.crashplanAbac.input.0.description = If the user is marked as Active in Crashplan. Default is all users.  
grouperGshTemplate.crashplanAbac.input.0.dropdownCsvValue = all, activeOnly, inactiveOnly  
grouperGshTemplate.crashplanAbac.input.0.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.0.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.0.label = Active in Crashplan  
[grouperGshTemplate.crashplanAbac.input.0.name](http://grouperGshTemplate.crashplanAbac.input.0.name) = gsh_input_cp_active  
grouperGshTemplate.crashplanAbac.input.0.type = string  
grouperGshTemplate.crashplanAbac.input.1.defaultValue = all  
grouperGshTemplate.crashplanAbac.input.1.description = If the user is marked as Blocked in Crashplan. Default is all users.  
grouperGshTemplate.crashplanAbac.input.1.dropdownCsvValue = all, blockedOnly, unblockedOnly  
grouperGshTemplate.crashplanAbac.input.1.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.1.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.1.label = Blocked in Crashplan  
[grouperGshTemplate.crashplanAbac.input.1.name](http://grouperGshTemplate.crashplanAbac.input.1.name) = gsh_input_cp_blocked  
grouperGshTemplate.crashplanAbac.input.1.type = string  
grouperGshTemplate.crashplanAbac.input.2.defaultValue = all  
grouperGshTemplate.crashplanAbac.input.2.description = If the user matches a Penn user, or is an email of unknown user, or all. Defaults to all  
grouperGshTemplate.crashplanAbac.input.2.dropdownCsvValue = all, matchesPennPerson, unknownEmailsOnly  
grouperGshTemplate.crashplanAbac.input.2.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.2.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.2.label = User type  
[grouperGshTemplate.crashplanAbac.input.2.name](http://grouperGshTemplate.crashplanAbac.input.2.name) = gsh_input_userType  
grouperGshTemplate.crashplanAbac.input.3.defaultValue = 0  
grouperGshTemplate.crashplanAbac.input.3.description = Number of Crashplan orgs to match  
grouperGshTemplate.crashplanAbac.input.3.dropdownCsvValue = 0, 1, 2, 3  
grouperGshTemplate.crashplanAbac.input.3.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.3.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.3.label = Org count  
[grouperGshTemplate.crashplanAbac.input.3.name](http://grouperGshTemplate.crashplanAbac.input.3.name) = gsh_input_orgCount  
grouperGshTemplate.crashplanAbac.input.3.type = integer  
grouperGshTemplate.crashplanAbac.input.4.description = Select the first org  
grouperGshTemplate.crashplanAbac.input.4.dropdownSqlDatabase = grouper  
grouperGshTemplate.crashplanAbac.input.4.dropdownSqlValue = select distinct org_name, org_name from penn_crashplan_user order by 1  
grouperGshTemplate.crashplanAbac.input.4.dropdownValueFormat = sql  
grouperGshTemplate.crashplanAbac.input.4.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.4.label = Crashplan org 1  
[grouperGshTemplate.crashplanAbac.input.4.name](http://grouperGshTemplate.crashplanAbac.input.4.name) = gsh_input_org0  
grouperGshTemplate.crashplanAbac.input.4.required = true  
grouperGshTemplate.crashplanAbac.input.4.showEl = \u0024{ gsh_input_orgCount >= 1 }  
grouperGshTemplate.crashplanAbac.input.4.type = string  
grouperGshTemplate.crashplanAbac.input.5.defaultValue = 0  
grouperGshTemplate.crashplanAbac.input.5.description = Select the number of roles to match  
grouperGshTemplate.crashplanAbac.input.5.dropdownCsvValue = 0, 1, 2, 3, 4  
grouperGshTemplate.crashplanAbac.input.5.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.5.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.5.label = Number of roles  
[grouperGshTemplate.crashplanAbac.input.5.name](http://grouperGshTemplate.crashplanAbac.input.5.name) = gsh_input_roleCount  
grouperGshTemplate.crashplanAbac.input.5.type = integer  
grouperGshTemplate.crashplanAbac.input.6.description = Select the first role to filter on  
grouperGshTemplate.crashplanAbac.input.6.dropdownSqlDatabase = grouper  
grouperGshTemplate.crashplanAbac.input.6.dropdownSqlValue = select distinct role_name, role_name from penn_crashplan_role order by 1  
grouperGshTemplate.crashplanAbac.input.6.dropdownValueFormat = sql  
grouperGshTemplate.crashplanAbac.input.6.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.6.label = Crashplan role 1  
[grouperGshTemplate.crashplanAbac.input.6.name](http://grouperGshTemplate.crashplanAbac.input.6.name) = gsh_input_role0  
grouperGshTemplate.crashplanAbac.input.6.required = true  
grouperGshTemplate.crashplanAbac.input.6.showEl = \u0024{ gsh_input_roleCount >= 1 }  
grouperGshTemplate.crashplanAbac.input.6.type = string  
grouperGshTemplate.crashplanAbac.input.7.defaultValue = all  
grouperGshTemplate.crashplanAbac.input.7.description = If filtering by users who are locked out by the CISO. Defaults to all users  
grouperGshTemplate.crashplanAbac.input.7.dropdownCsvValue = all, lockedOutOnly, notLockedOut  
grouperGshTemplate.crashplanAbac.input.7.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.7.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.7.label = Locked out users  
[grouperGshTemplate.crashplanAbac.input.7.name](http://grouperGshTemplate.crashplanAbac.input.7.name) = gsh_input_lockedOut  
grouperGshTemplate.crashplanAbac.input.7.type = string  
grouperGshTemplate.crashplanAbac.input.8.defaultValue = all  
grouperGshTemplate.crashplanAbac.input.8.description = If filtering by affiliation. Default to all users  
grouperGshTemplate.crashplanAbac.input.8.dropdownCsvValue = all, employeesOnly, notEmployees, affiliatesOnly, notAffiliates  
grouperGshTemplate.crashplanAbac.input.8.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.8.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.8.label = User affiliation  
[grouperGshTemplate.crashplanAbac.input.8.name](http://grouperGshTemplate.crashplanAbac.input.8.name) = gsh_input_userAffiliation  
grouperGshTemplate.crashplanAbac.input.8.type = string  
grouperGshTemplate.crashplanAbac.input.9.defaultValue = all  
grouperGshTemplate.crashplanAbac.input.9.description = If filtering by MFA status, if the user is enrolled in Two-step verification. Default to all users  
grouperGshTemplate.crashplanAbac.input.9.dropdownCsvValue = all, twoStepEnrolled, notTwoStepEnrolled  
grouperGshTemplate.crashplanAbac.input.9.dropdownValueFormat = csv  
grouperGshTemplate.crashplanAbac.input.9.formElementType = dropdown  
grouperGshTemplate.crashplanAbac.input.9.label = MFA users  
[grouperGshTemplate.crashplanAbac.input.9.name](http://grouperGshTemplate.crashplanAbac.input.9.name) = gsh_input_mfa  
grouperGshTemplate.crashplanAbac.input.9.type = string  
grouperGshTemplate.crashplanAbac.numberOfInputs = 10  
grouperGshTemplate.crashplanAbac.runAsType = GrouperSystem  
grouperGshTemplate.crashplanAbac.securityRunType = specifiedGroup  
grouperGshTemplate.crashplanAbac.templateDescription = Generates an ABAC script for Crashplan groups  
grouperGshTemplate.crashplanAbac.templateName = Crashplan ABAC  
grouperGshTemplate.crashplanAbac.templateType = abac

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;  
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;

//public class Test60abacTemplate {  
//  
// public static void main(String[] args) {  
//  
// GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();  
// GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();  
//  
// String gsh_input_cp_active = "inactiveOnly"; // all, activeOnly, inactiveOnly  
// String gsh_input_cp_blocked = "blockedOnly"; // all, blockedOnly, unblockedOnly  
// String gsh_input_userType = "matchesPennPerson"; // all, matchesPennPerson, unknownEmailsOnly  
// int gsh_input_orgCount = 1; // 0, 1, 2, 3  
// String gsh_input_org0 = "Perelman School of Medicine"; // Perelman School of Medicine  
// int gsh_input_roleCount = 1; // 0, 1, 2, 3, 4  
// String gsh_input_role0 = "desktop-user"; // desktop-user  
// String gsh_input_lockedOut = "lockedOutOnly"; // all, lockedOutOnly, notLockedOut  
// String gsh_input_userAffiliation = "notAffiliates"; // all, employeesOnly, notEmployees, affiliatesOnly, notAffiliates  
// String gsh_input_mfa = "all"; // all, twoStepEnrolled, notTwoStepEnrolled  
   
 // entity.hasRow('cp_user',  
 // "cp_active  
 // && !cp_blocked  
 // && cp_known  
 // && cp_org == 'Perelman School of Medicine' ")  
 // && (!entity.memberOf('penn:ref:member')  
 // || entity.memberOf('penn:ref:lockout') )  
 // && entity.hasAttribute('cp_role', 'desktop-user')  
   
 StringBuilder script = new StringBuilder('$' + "{ \n");  
 boolean firstClause = true;  
 if (!StringUtils.equals(gsh_input_cp_active, "all") || !StringUtils.equals(gsh_input_cp_blocked, "all")  
 || !StringUtils.equals(gsh_input_userType, "all") || gsh_input_orgCount > 0) {  
   
   
 script.append(" entity.hasRow('cp_user',\n \" ");

if (!StringUtils.equals(gsh_input_cp_active, "all")) {  
 if (StringUtils.equals(gsh_input_cp_active, "inactiveOnly")) {  
 script.append("! ");  
 }  
 script.append("cp_active\n");  
 firstClause = false;  
 }  
 if (!StringUtils.equals(gsh_input_cp_blocked, "all")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 if (StringUtils.equals(gsh_input_cp_blocked, "unblockedOnly")) {  
 script.append("! ");  
 }  
 script.append("cp_blocked\n");  
 firstClause = false;  
 }  
 if (!StringUtils.equals(gsh_input_userType, "all")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 if (StringUtils.equals(gsh_input_userType, "unknownEmailsOnly")) {  
 script.append("! ");  
 }  
 script.append("cp_known\n");  
 firstClause = false;  
 }  
 if (gsh_input_orgCount > 0) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append("cp_org == '" + gsh_input_org0 + "'\n");  
 firstClause = false;  
 }  
 script.delete(script.length()-1, script.length());  
 script.append(" \")\n");

}

if (gsh_input_roleCount > 0) {  
   
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append("entity.hasAttribute('cp_role', '" + gsh_input_role0 + "')\n");  
 firstClause = false;  
 }

if (StringUtils.equals(gsh_input_lockedOut, "notLockedOut") || StringUtils.equals(gsh_input_mfa, "twoStepEnrolled") ||   
 StringUtils.equalsAny(gsh_input_userAffiliation, "employeesOnly", "affiliatesOnly")) {  
 if (StringUtils.equals(gsh_input_lockedOut, "notLockedOut")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append("! entity.memberOf('penn:ref:lockout')\n");  
 firstClause = false;  
 }  
 if (StringUtils.equals(gsh_input_mfa, "twoStepEnrolled")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append("entity.memberOf('penn:ref:mfaEnrolled')\n");  
 firstClause = false;  
 }  
 if (StringUtils.equals(gsh_input_userAffiliation, "employeesOnly")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append("entity.memberOf('penn:ref:employee')\n");  
 firstClause = false;  
 }  
 if (StringUtils.equals(gsh_input_userAffiliation, "affiliatesOnly")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append("entity.memberOf('penn:ref:member')\n");  
 firstClause = false;  
 }  
 }

if (StringUtils.equals(gsh_input_lockedOut, "lockedOutOnly") || StringUtils.equals(gsh_input_mfa, "notTwoStepEnrolled") ||   
 StringUtils.equalsAny(gsh_input_userAffiliation, "notEmployees", "notAffiliates")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("&& ");  
 }  
 script.append(" (\n");  
 firstClause = true;  
 if (StringUtils.equals(gsh_input_lockedOut, "lockedOutOnly")) {  
 script.append(" entity.memberOf('penn:ref:lockout')\n");  
 firstClause = false;  
 }

if (StringUtils.equals(gsh_input_mfa, "notTwoStepEnrolled")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("|| ");  
 }  
 script.append("! entity.memberOf('penn:ref:mfaEnrolled')\n");  
 firstClause = false;  
 }

if (StringUtils.equals(gsh_input_userAffiliation, "notEmployees")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("|| ");  
 }  
 script.append("! entity.memberOf('penn:ref:employee')\n");  
 firstClause = false;  
 }

if (StringUtils.equals(gsh_input_userAffiliation, "notAffiliates")) {  
 script.append(" ");  
 if (!firstClause) {  
 script.append("|| ");  
 }  
 script.append("! entity.memberOf('penn:ref:member')\n");  
 firstClause = false;  
 }

script.append(" )\n");  
 }

if (StringUtils.equalsAny(gsh_input_userType, "all", "unknownEmailsOnly")) {  
 gsh_builtin_gshTemplateOutput.assignAbacIncludeInternalSubjectSources(true);  
 }  
   
 script.append("} ");  
 gsh_builtin_gshTemplateOutput.assignAbacScript(script.toString());

//System.out.println(script);

// }  
//  
//}

Screen
