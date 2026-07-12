---
title: "Provisioner to mark as provisionable and use in GSH change log consumer"
space: Grouper
pageId: 28560169
version: 13
lastUpdated: 2026-07-01T05:36:08.237Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560169/Provisioner+to+mark+as+provisionable+and+use+in+GSH+change+log+consumer
---

## Summary

Use a dummy provisioner to mark groups as provisionable, but then process events in a GSH change log consumer to call a web service to Sailpoint IIQ

## Set up a dummy provisioner

[Here is another wiki on this](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554518/Using+a+placeholder+provisioner+configuration+for+the+provisionable+metadata+screen)

```
provisioner.iiqWebService.class = edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner
provisioner.iiqWebService.customizeGroupCrud = true
provisioner.iiqWebService.dbExternalSystemConfigId = grouper
provisioner.iiqWebService.deleteGroups = false
provisioner.iiqWebService.groupAttributeValueCacheHas = true
provisioner.iiqWebService.groupTableIdColumn = id
provisioner.iiqWebService.groupTableName = some_table_doesnt_exist
provisioner.iiqWebService.insertGroups = false
provisioner.iiqWebService.numberOfGroupAttributes = 1
provisioner.iiqWebService.operateOnGrouperGroups = true
provisioner.iiqWebService.selectGroups = false
provisioner.iiqWebService.startWith = this is start with read only
provisioner.iiqWebService.targetGroupAttribute.0.name = id
provisioner.iiqWebService.targetGroupAttribute.0.translateExpressionType = grouperProvisioningGroupField
provisioner.iiqWebService.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField = id
provisioner.iiqWebService.updateGroups = false
provisioner.iiqWebService.addDisabledIncrementalSyncDaemon = true
```

Import this to grouper-loader.properties

Edit the daemon, make sure advanced → add disabled incremental daemon is true, and hit save. Enable the incremental daemon. This will keep a list of provisionable groups in the sync table

## Identify which folders or groups are provisionable

## Configure the IIQ web service

If you are before v4.9.0 just import this in grouper-loader.properties

```
grouper.wsBearerToken.iiqWebService.basicAuthPassword = *******
grouper.wsBearerToken.iiqWebService.basicAuthUser = someUser
grouper.wsBearerToken.iiqWebService.endpoint = https://a.b.c/identityiq/
grouper.wsBearerToken.iiqWebService.httpAuthnType = basicAuth
```

After v4.9.0+ you could instead do this in the UI

## Write your GSH change log daemon

Note, this will process all memberships and send only things that are provisionable to IIQ. Note: configure the subject source id's to provision in the script. Note: the cron time is 10 seconds after minute so the "provisionable changelog" has time to process new provisionable groups and insert into the sync table.

## Script

```
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

//public class Test70changeLogGshTest {
//
//  public static long method() {
//    
//    EsbPublisherChangeLogScript esbPublisherChangeLogScript = EsbPublisherChangeLogScript.retrieveFromThreadLocal();
//    Map<String, Object> gsh_builtin_debugMap = esbPublisherChangeLogScript.getDebugMap();
//    ChangeLogProcessorMetadata gsh_builtin_changeLogProcessorMetadata = esbPublisherChangeLogScript.getChangeLogProcessorMetadata();
//    ProvisioningSyncConsumerResult gsh_builtin_provisioningSyncConsumerResult = esbPublisherChangeLogScript.getProvisioningSyncConsumerResult();
//    List<EsbEventContainer> gsh_builtin_esbEventContainers = esbPublisherChangeLogScript.getEsbEventContainers();
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.staticGrouperSession();
//    Hib3GrouperLoaderLog gsh_builtin_hib3GrouperLoaderLog = gsh_builtin_changeLogProcessorMetadata.getHib3GrouperLoaderLog();
        
    // cache in this run if group is provisionable to reduce queries
    Map<String, Boolean> provisionableGroup = new HashMap<String, Boolean>();
    
    // cache in this run if the user was sent, no need to send twice
    Set<String> userProvisioned = new HashSet<String>();
    
    // which subject source ids should be sent
    Set<String> subjectSourceIdsToProvision = GrouperUtil.toSet("jdbc");
    
    // log the first 50 subject ids sent
    Set<String> someSubjectIdsSent = new HashSet<String>();

    // WS credentials and endpoint
    String url = GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken.iiqWebService.endpoint")) + 
      "/plugin/rest/ReconcileIdentityPlugin/Grouper/subjectId=";
    String user = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken.iiqWebService.basicAuthUser");
    String pass = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken.iiqWebService.basicAuthPassword");
    
    for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      
      // if not the right subject source, continue
      if (!subjectSourceIdsToProvision.contains(esbEvent.getSourceId())) {
        GrouperUtil.mapAddValue(gsh_builtin_debugMap, "wrongSubjectSource", 1);
        continue;
      }

      // if user was provisioned in this run, then skip
      if (userProvisioned.contains(esbEvent.getSubjectId())) {
        GrouperUtil.mapAddValue(gsh_builtin_debugMap, "userAlreadyProvisioned", 1);
        continue;
      }
      
      Boolean provisionable = provisionableGroup.get(esbEvent.getGroupName());
      
      // only check to see if group is provisionable if we dont know yet
      if (provisionable == null) {

        GrouperUtil.mapAddValue(gsh_builtin_debugMap, "seeIfGroupProvisionable", 1);

        int count = new GcDbAccess().sql("select count(1) from grouper_sync gs, grouper_sync_group gsg " +
            " where gs.id = gsg.grouper_sync_id and gsg.provisionable = 'T' and gs.provisioner_name = 'iiqWebService' " +
            " and gsg.group_name = ?").addBindVar(esbEvent.getGroupName()).select(int.class);
        provisionable = count > 0;
        provisionableGroup.put(esbEvent.getGroupName(), provisionable);
        
      }
      
      // if group is not provisionable then skip
      if (!provisionable) {
        GrouperUtil.mapAddValue(gsh_builtin_debugMap, "skipNonProvisionableGroup", 1);
        continue;
      }

      // call iiq
      new GrouperHttpClient().assignUrl(url + esbEvent.getSubjectId()).assignGrouperHttpMethod("post").
        assignUser(user).assignPassword(pass).assignAssertResponseCode(200).executeRequest();

      GrouperUtil.mapAddValue(gsh_builtin_debugMap, "userCountSentToIiq", 1);
      userProvisioned.add(esbEvent.getSubjectId());
      
      if (someSubjectIdsSent.size() < 50) {
        someSubjectIdsSent.add(esbEvent.getSubjectId());
      }
      
      gsh_builtin_hib3GrouperLoaderLog.addUpdateCount(1);
      
    }
    // log some user ids
    gsh_builtin_hib3GrouperLoaderLog.appendJobMessage("Some subject IDs sent: " + GrouperUtil.toStringForLog(someSubjectIdsSent) + ", ");

    // tell grouper which change log event we reached, which is the last one.  otherwise there was an exception and it wont run this anyways
    return gsh_builtin_esbEventContainers.get(gsh_builtin_esbEventContainers.size()-1).getSequenceNumber();
    
//  }
//
//}
   
```

## Daemon output from UI

From daemon logs page on UI

```
  Some subject IDs sent: HashSet size: 1: [0]: test.subject.1
  , method: dispatchEventList, eventCount: 4, lastSequenceAvailable: 2076, changeLogFileType: script, seeIfGroupProvisionable: 2, userCountSentToIiq: 1, userAlreadyProvisioned: 2, skipNonProvisionableGroup: 1, lastSequenceProcessed: 2076, tookMillis: 45
```

## Internal run a web service endpoint

```
docker run -p 8090:80 kennethreitz/httpbin
```

Endpoint

```
http://localhost:8090/anything/identityiq/
```
