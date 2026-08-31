---
title: "Grouper daemon \"other job\" GSH script to sync GrouperAll READ privileges"
space: Grouper
pageId: 28560037
version: 2
lastUpdated: 2026-07-01T05:36:30.728Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560037/Grouper+daemon+other+job+GSH+script+to+sync+GrouperAll+READ+privileges
---

Script

```
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

//// uncomment to run in eclipse
//public class Test43syncGrouperAll {
//
//  public static void main(String[] args) {

    GrouperStartup.startup();
    GrouperSession.startRootSession();
    
    String folderToSync = "test2";

    Subject grouperAll = SubjectFinder.findAllSubject();
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    List<String> groupNamesNew = new GcDbAccess().connectionName("grouper").sql("select name from grouper_groups where name in ('test2:testGroup1', 'test2:testGroup3')").selectList(String.class);

    debugMap.put("groupNamesNewCount", GrouperUtil.length(groupNamesNew));
    
    List<String> groupNamesExisting = new GcDbAccess().connectionName("grouper").sql("select group_name from grouper_memberships_lw_v gmlv where group_name like '" + folderToSync + ":%' and list_name = 'readers' and subject_source = 'g:isa' and subject_id = 'GrouperAll'").selectList(String.class);

    debugMap.put("groupNamesExistingCount", GrouperUtil.length(groupNamesExisting));

    Set<String> groupNamesToAdd = new HashSet<String>(groupNamesNew);
    groupNamesToAdd.removeAll(groupNamesExisting);
    
    for (String groupNameToAdd : groupNamesToAdd) {
      Group group = GroupFinder.findByName(groupNameToAdd, false);
      if (group == null) {
        debugMap.put("cantFind_" + groupNameToAdd, groupNamesToAdd);
        continue;
      }
      debugMap.put("add_" + groupNameToAdd, true);
      group.grantPriv(grouperAll, AccessPrivilege.READ, false);
    }
        
    Set<String> groupNamesToRemove = new HashSet<String>(groupNamesExisting);
    groupNamesToRemove.removeAll(groupNamesNew);
    
    for (String groupNameToRemove : groupNamesToRemove) {
      Group group = GroupFinder.findByName(groupNameToRemove, false);
      if (group == null) {
        continue;
      }
      debugMap.put("remove_" + groupNameToRemove, true);
      group.revokePriv(grouperAll, AccessPrivilege.READ, false);
    }
        
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));

    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(groupNamesNew));
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(GrouperUtil.length(groupNamesToAdd));
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(GrouperUtil.length(groupNamesToRemove));
//  }
//
//}
```
