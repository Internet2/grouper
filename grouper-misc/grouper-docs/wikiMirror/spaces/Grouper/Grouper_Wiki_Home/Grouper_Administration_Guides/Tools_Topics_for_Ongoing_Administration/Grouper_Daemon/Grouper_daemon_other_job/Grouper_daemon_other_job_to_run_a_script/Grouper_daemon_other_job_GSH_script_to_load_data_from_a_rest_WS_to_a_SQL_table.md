---
title: "Grouper daemon \"other job\" GSH script to load data from a rest WS to a SQL table"
space: Grouper
pageId: 28560397
version: 9
lastUpdated: 2022-02-24T22:04:02.337Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560397/Grouper+daemon+other+job+GSH+script+to+load+data+from+a+rest+WS+to+a+SQL+table
---

This example will load data from a REST WS to a SQL table, and then a loader can load that into Grouper

Hopefully this isnt too confusing, but this will use the Grouper WS from another Grouper

## Setup data in remote source Grouper

## Configure the connection information in local Grouper

## WS REST call to get group names

GET https://gro************u/grouperWs/servicesRest/v2_6_000/groups?wsLiteObjectType=WsRestFindGroupsLiteRequest&queryFilterType=FIND_BY_STEM_NAME&stemName=test:isc:astt:chris:someFolderToLoadOverWs

```
{
   "WsFindGroupsResults":{
      "groupResults":[
         {
            "extension":"anotherGroup234",
            "displayName":"test:isc:astt:chris:someFolderToLoadOverWs:anotherGroup234",
            "uuid":"e378b185deb84592835e46287b2ba2b5",
            "enabled":"T",
            "displayExtension":"anotherGroup234",
            "name":"test:isc:astt:chris:someFolderToLoadOverWs:anotherGroup234",
            "typeOfGroup":"group",
            "idIndex":"585190"
         },
         {
            "extension":"group123",
            "displayName":"test:isc:astt:chris:someFolderToLoadOverWs:group123",
            "uuid":"174388f6b33746fda4c9e4a7a7eb49a5",
            "enabled":"T",
            "displayExtension":"group123",
            "name":"test:isc:astt:chris:someFolderToLoadOverWs:group123",
            "typeOfGroup":"group",
            "idIndex":"585189"
         }
      ],
      "resultMetadata":{
         "success":"T",
         "resultCode":"SUCCESS",
         "resultMessage":"Success for: clientVersion: 2.6.0, wsQueryFilter: WsQueryFilter[queryFilterType=FIND_BY_STEM_NAME,stemName=test:isc:astt:chris:someFolderToLoadOverWs]\n, includeGroupDetail: false, actAsSubject: null, paramNames: \n, params: null\n, wsGroupLookups: null"
      },
      "responseMetadata":{
         "serverVersion":"2.6.5",
         "millis":"12"
      }
   }
}
```

## Make a table for groups

Lets save the extension (to load to local grouper), and the name (to retrieve from endpoint). Note, this does not need to be in the Grouper database, could be in any external system

mysql

```
CREATE TABLE load_ws_to_groups (
	the_group_name varchar(100) NOT NULL,
	the_group_extension varchar(100) NOT NULL
);
```

## Write a script to get group names and extensions

Note if you are on Grouper version pre v2.6.8, you need to add a couple of functions and refactor: [GRP-3852](https://todos.internet2.edu/browse/GRP-3852), [GRP-3851](https://todos.internet2.edu/browse/GRP-3851), [GRP-3853](https://todos.internet2.edu/browse/GRP-3853), [GRP-3854](https://todos.internet2.edu/browse/GRP-3854), [GRP-3855](https://todos.internet2.edu/browse/GRP-3855), [GRP-3856](https://todos.internet2.edu/browse/GRP-3856), [GRP-3857](https://todos.internet2.edu/browse/GRP-3857)

```
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncFromData;
import edu.internet2.middleware.morphString.Morph;

//public class Test33WsLoader {
  
//  public static void main(String[] args) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();
    try {
      // get groups
      String endpoint = GrouperConfig.retrieveConfig().propertyValueString("myWsLoaderEndpoint");
      endpoint = GrouperUtil.stripLastSlashIfExists(endpoint);
      String user = GrouperConfig.retrieveConfig().propertyValueString("myWsLoaderUser");
      String password = Morph.decryptIfFile(GrouperConfig.retrieveConfig().propertyValueString("myWsLoaderPassword"));
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignGrouperHttpMethod(GrouperHttpMethod.get).assignUser(user).assignPassword(password).
          assignDebugMap(debugMap).assignAssertResponseCode(200).
          assignAssertJsonPointer("/WsFindGroupsResults/resultMetadata/success").assignAssertJsonPointerExpectedValueString("T").
          assignUrl(endpoint 
          + "/v2_6_000/groups?wsLiteObjectType=WsRestFindGroupsLiteRequest&queryFilterType=FIND_BY_STEM_NAME&stemName=" + GrouperUtil.escapeUrlEncode("test:isc:astt:chris:someFolderToLoadOverWs")).executeRequest();
      
      // convert the response into a list of rows
      List<Object[]> wsRows = GrouperUtil.jsonJacksonListObjectArrayFromJsonPointers(grouperHttpClient.retrieveJsonNode(), 
          "/WsFindGroupsResults/groupResults", GrouperUtil.toList("/name", "/extension"));
      
      hib3GrouperLoaderLog.setTotalCount(GrouperUtil.length(wsRows));
      debugMap.put("wsGroups", GrouperUtil.length(wsRows));
      
      // sync that to the table
      String connectionName = "grouper";
      String tableName = "load_ws_to_groups";
      List<String> columnNames = GrouperUtil.toList("the_group_name", "the_group_extension");
      List<String> columnNamesPrimaryKey = GrouperUtil.toList("the_group_name");
  
      new GcTableSyncFromData().assignDebugMap(debugMap).assignConnectionName(connectionName).assignTableName(tableName)
        .assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(wsRows).sync();
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      hib3GrouperLoaderLog.setInsertCount(GrouperUtil.intObjectValue(debugMap.get("insertsCount"), true));
      hib3GrouperLoaderLog.setUpdateCount(GrouperUtil.intObjectValue(debugMap.get("updatesCount"), true));
      hib3GrouperLoaderLog.setDeleteCount(GrouperUtil.intObjectValue(debugMap.get("deletesCount"), true));
      hib3GrouperLoaderLog.setJobMessage(GrouperUtil.toStringForLog(debugMap));
    }
//  }
//}
```

## Schedule a daemon job that will never run

```
0 0 1 * * ? 2099
```

## Run groups daemon and see groups

## WS REST call to get memberships

Note, with grouper you can get all memberships in groups in a folder in one call, but in this case lets do it the long way since that might be what another REST WS supports

GET https://gro************u//grouperWs/servicesRest/v2_6_000/groups/test:isc:astt:chris:someFolderToLoadOverWs:anotherGroup234/members

```
{
   "WsGetMembersLiteResult":{
      "resultMetadata":{
         "success":"T",
         "resultCode":"SUCCESS",
         "resultMessage":"Success for: clientVersion: 2.6.0, wsGroupLookups: Array size: 1: [0]: WsGroupLookup[pitGroups=[],groupName=test:isc:astt:chris:someFolderToLoadOverWs:anotherGroup234]\n\n, memberFilter: All, includeSubjectDetail: false, actAsSubject: null, fieldName: null, subjectAttributeNames: null\n, paramNames: \n, params: null\n, sourceIds: null\n, pointInTimeFrom: null, pointInTimeTo: null, pageSize: null, pageNumber: null, sortString: null, ascending: null"
      },
      "wsGroup":{
         "extension":"anotherGroup234",
         "displayName":"test:isc:astt:chris:someFolderToLoadOverWs:anotherGroup234",
         "uuid":"e378b185deb84592835e46287b2ba2b5",
         "enabled":"T",
         "displayExtension":"anotherGroup234",
         "name":"test:isc:astt:chris:someFolderToLoadOverWs:anotherGroup234",
         "typeOfGroup":"group",
         "idIndex":"585190"
      },
      "responseMetadata":{
         "serverVersion":"2.6.5",
         "millis":"35"
      },
      "wsSubjects":[
         {
            "sourceId":"pennperson",
            "success":"T",
            "resultCode":"SUCCESS",
            "id":"10021368",
            "memberId":"c5c8ef55-76be-4b0d-9910-9efbf465cff3"
         },
         {
            "sourceId":"pennperson",
            "success":"T",
            "resultCode":"SUCCESS",
            "id":"10754302",
            "memberId":"5c6da933-07b9-4c56-a932-22f88d83c8dc"
         }
      ]
   }
}
```

## Make a table for memberships

We will use the extension (which will be unique since all groups in one folder), to load to local grouper, and the subject_id

mysql

```
CREATE TABLE load_ws_to_memberships (
	the_group_extension varchar(100) NOT NULL,
	the_subject_id varchar(100) NOT NULL
);
```

## Write a script to get memberships

Note if you are on Grouper version pre v2.6.8, you need to add a couple of functions and refactor: [GRP-3852](https://todos.internet2.edu/browse/GRP-3852), [GRP-3851](https://todos.internet2.edu/browse/GRP-3851), [GRP-3853](https://todos.internet2.edu/browse/GRP-3853), [GRP-3854](https://todos.internet2.edu/browse/GRP-3854), [GRP-3855](https://todos.internet2.edu/browse/GRP-3855), [GRP-3856](https://todos.internet2.edu/browse/GRP-3856), [GRP-3857](https://todos.internet2.edu/browse/GRP-3857)

```
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncFromData;
import edu.internet2.middleware.morphString.Morph;

//public class Test34WsLoaderMship {

  
//  public static void main(String[] args) {

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    try {
      // get groups
      String endpoint = GrouperConfig.retrieveConfig().propertyValueString("myWsLoaderEndpoint");
      endpoint = GrouperUtil.stripLastSlashIfExists(endpoint);
      String user = GrouperConfig.retrieveConfig().propertyValueString("myWsLoaderUser");
      String password = Morph.decryptIfFile(GrouperConfig.retrieveConfig().propertyValueString("myWsLoaderPassword"));
      String connectionName = "grouper";
      
      // get the group names from database
      List<String> groupNames = new GcDbAccess().connectionName(connectionName).sql("select the_group_name from load_ws_to_groups").selectList(String.class);

      List<Object[]> wsRows = new ArrayList<Object[]>();

      for (String groupName : groupNames) {
                
        GrouperHttpClient grouperHttpClient = new GrouperHttpClient().assignGrouperHttpMethod(GrouperHttpMethod.get).assignUser(user).assignPassword(password).
            assignDebugMap(debugMap).assignAssertResponseCode(200).
            assignAssertJsonPointer("/WsGetMembersLiteResult/resultMetadata/success").assignAssertJsonPointerExpectedValueString("T").
            assignUrl(endpoint + "/v2_6_000/groups/" + GrouperUtil.escapeUrlEncode(groupName) + "/members").executeRequest();
        
        // convert the response into a list of rows
        wsRows.addAll(GrouperUtil.jsonJacksonListObjectArrayFromJsonPointers(grouperHttpClient.retrieveJsonNode(), 
            "/WsGetMembersLiteResult/wsSubjects", GrouperUtil.toList("JSON_NODE_ROOT/WsGetMembersLiteResult/wsGroup/extension", "/id")));

      }     

      debugMap.put("wsMemberships", GrouperUtil.length(wsRows));
      hib3GrouperLoaderLog.setTotalCount(GrouperUtil.length(wsRows));
      
      // sync that to the table
      String tableName = "load_ws_to_memberships";
      List<String> columnNames = GrouperUtil.toList("the_group_extension", "the_subject_id");
      List<String> columnNamesPrimaryKey = GrouperUtil.toList("the_group_extension", "the_subject_id");
  
      new GcTableSyncFromData().assignDebugMap(debugMap).assignConnectionName(connectionName).assignTableName(tableName)
        .assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(wsRows).sync();

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      hib3GrouperLoaderLog.setInsertCount(GrouperUtil.intObjectValue(debugMap.get("insertsCount"), true));
      hib3GrouperLoaderLog.setUpdateCount(GrouperUtil.intObjectValue(debugMap.get("updatesCount"), true));
      hib3GrouperLoaderLog.setDeleteCount(GrouperUtil.intObjectValue(debugMap.get("deletesCount"), true));
      hib3GrouperLoaderLog.setJobMessage(GrouperUtil.toStringForLog(debugMap));
    }
//  }
//}
```

## Schedule a daemon that will not fire

## Run membership daemon and see data

## Make a loader to load the data from SQL

membership query

```
select concat('test2:', lwtm.the_group_extension) as group_name, lwtm.the_subject_id as subject_id, 'jdbc' as subject_source_id from load_ws_to_memberships lwtm 
```

group query

```
select concat('test2:', lwtg.the_group_extension) as group_name from load_ws_to_groups lwtg 
```

## Run loader job and see groups and memberships

## Make a job that runs hourly that runs all the jobs sequentially

```
loaderRunOneJob("OTHER_JOB_syncWsGroups");
loaderRunOneJob("OTHER_JOB_syncWsMemberships");
loaderRunOneJob("SQL_GROUP_LIST__test:testLoader__746852981a1248418cceb8e8e0534821");
```

Delete all the data in those database tables, the loaded stem, and run the overall job. Both syncs and the loader will run.
