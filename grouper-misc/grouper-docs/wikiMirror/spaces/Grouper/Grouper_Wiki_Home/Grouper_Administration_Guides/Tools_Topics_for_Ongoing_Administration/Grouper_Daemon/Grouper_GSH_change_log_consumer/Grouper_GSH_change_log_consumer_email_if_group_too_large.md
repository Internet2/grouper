---
title: "Grouper GSH change log consumer - email if group too large"
space: Grouper
pageId: 28555212
version: 2
lastUpdated: 2026-07-01T05:38:43.873Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555212/Grouper+GSH+change+log+consumer+-+email+if+group+too+large
---

This is a GSH script change log consumer that send email if a group is too large

## Add a daemon

Note: customize the group name in the JEXL filter

## Daemon config

Note: customize the group name in the EL filter

```
changeLog.consumer.maxGroupSizeTestGroup.changeLogFileType = script
changeLog.consumer.maxGroupSizeTestGroup.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.maxGroupSizeTestGroup.elfilter = (event.eventType == 'MEMBERSHIP_DELETE' || event.eventType == 'MEMBERSHIP_ADD') && event.groupName == 'test\u003AtestGroup'
changeLog.consumer.maxGroupSizeTestGroup.publisher.class = edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript
changeLog.consumer.maxGroupSizeTestGroup.quartzCron = 0 * * * * ?

```

## Daemon GSH source

Note, customize these variables

```
    String groupIdPath = "test:testGroup";
    String subjectSourceId = "jdbc";
    int maxSize = 3;
    String emailToCommaSeparated = "whomever@school.edu";
    String emailSubject = "Group test:testGroup got big";
    String emailBody = "The group too large";
```

```
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

//public class Test70changeLogGshMaxSize {
//
//  public static void main(String[] args) {
//
//    EsbPublisherChangeLogScript esbPublisherChangeLogScript = EsbPublisherChangeLogScript.retrieveFromThreadLocal();
//    Map<String, Object> gsh_builtin_debugMap = esbPublisherChangeLogScript.getDebugMap();
//    ChangeLogProcessorMetadata gsh_builtin_changeLogProcessorMetadata = esbPublisherChangeLogScript.getChangeLogProcessorMetadata();
//    ProvisioningSyncConsumerResult gsh_builtin_provisioningSyncConsumerResult = esbPublisherChangeLogScript.getProvisioningSyncConsumerResult();
//    List<EsbEventContainer> gsh_builtin_esbEventContainers = esbPublisherChangeLogScript.getEsbEventContainers();
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.staticGrouperSession();
//    Hib3GrouperLoaderLog gsh_builtin_hib3GrouperLoaderLog = gsh_builtin_changeLogProcessorMetadata.getHib3GrouperLoaderLog();

    String groupIdPath = "test:testGroup";
    String subjectSourceId = "jdbc";
    int maxSize = 3;
    String emailToCommaSeparated = "whomever@school.edu";
    String emailSubject = "Group test:testGroup got big";
    String emailBody = "The group too large";
    
    int size = new GcDbAccess().sql("select count(1) from grouper_memberships_lw_v gmlv where group_name = ? and list_name = 'members' and subject_source = ?").
        addBindVar(groupIdPath).addBindVar(subjectSourceId).select(int.class);
    
    gsh_builtin_debugMap.put("currentSize", size);
    
    if (size > maxSize) {
      new GrouperEmail().setTo(emailToCommaSeparated).setSubject(emailSubject).setBody(emailBody).send();
      gsh_builtin_debugMap.put("tooBig", true);
      gsh_builtin_hib3GrouperLoaderLog.addUpdateCount(1); 
    } else {
      gsh_builtin_debugMap.put("tooBig", false);
    }
    
    long lastSequenceProcessed = -1;
    for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) { 
      EsbEvent esbEvent = esbEventContainer.getEsbEvent(); 
      
      lastSequenceProcessed = esbEventContainer.getSequenceNumber(); 
    } 

    return lastSequenceProcessed;
//    
//  }
//
//}
```

## Daemon output

Note if update count is 1 an email was sent out

```
method: dispatchEventList, eventCount: 1, lastSequenceAvailable: 6493, 
changeLogFileType: script, 
currentSize: 7, tooBig: true, 
lastSequenceProcessed: 6493, tookMillis: 387
```

## Email
