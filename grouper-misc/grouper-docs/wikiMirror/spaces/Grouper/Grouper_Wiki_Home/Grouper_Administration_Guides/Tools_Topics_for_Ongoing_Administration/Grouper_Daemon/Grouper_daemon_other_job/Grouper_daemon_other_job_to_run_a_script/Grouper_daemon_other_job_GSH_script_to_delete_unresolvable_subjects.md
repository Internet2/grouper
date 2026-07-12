---
title: "Grouper daemon \"other job\" GSH script to delete unresolvable subjects"
space: Grouper
pageId: 28560066
version: 5
lastUpdated: 2026-07-01T05:36:26.639Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560066/Grouper+daemon+other+job+GSH+script+to+delete+unresolvable+subjects
---

Run this script every 15 minutes

Check a table, and if there are subject ids or identifiers, then see if unresolvable, and if so, delete their memberships and privileges

Afterward delete from the table

## Configure daemon

## Output for a subject delete

```
subjectIdsFromQueryCount: 1, subjectIds: a-jsmith, subjectsResolvable: 0, membersNotFound: 0, membersCount: 1, deletedCount: 1, deleteFromTableCount: 1
scriptType: gsh
scriptSource: import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Gro...

null
```

## GSH script

(configure the database, table, column, and subject source at the top)

```
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.usdu.UsduJob;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;

////uncomment to compile in eclipse (and last line)
//// these are standard imports, can be commented out in script but needed in eclipse
//
//
//public class Test24 {
//
//  public static void main(String[] args) throws Exception {
//
//    edu.internet2.middleware.grouper.misc.GrouperStartup.startup();

    // settings
    String databaseConfigId = "grouper";
    String table = "subjects_to_delete";
    String column = "subject_id";
    String sourceId = "personLdapSource";
    
    GrouperSession.startRootSession();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    // get current batch
    List<String> subjectIds = new GcDbAccess().connectionName(databaseConfigId).sql("select distinct " + column + " from " + table).selectList(String.class);

    debugMap.put("subjectIdsFromQueryCount", GrouperUtil.length(subjectIds));
    
    // make total show up on ui
    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(GrouperUtil.length(subjectIds));

    Set<Member> members = new HashSet<Member>();

    int subjectsResolvable = 0;
    int membersNotFound = 0;
    
    // resolve them and make sure unresolvable
    if (GrouperUtil.length(subjectIds) > 0) {
      
      debugMap.put("subjectIds", GrouperUtil.abbreviate(GrouperUtil.join(subjectIds.iterator(), ", "), 200));
      
      for (String subjectId : GrouperUtil.nonNull(subjectIds)) {
        
        // dont use cache here
        Subject subject = SourceManager.getInstance().getSource(sourceId).getSubjectByIdOrIdentifier(subjectId, false);
        
        if (subject != null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);
          subjectsResolvable++;
          continue;
        }
        
        Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subjectId, sourceId, false);
        if (member == null) {
          OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(1);
          membersNotFound++;
          continue;
        }
        
        members.add(member);
      }
    }
    debugMap.put("subjectsResolvable", subjectsResolvable);
    debugMap.put("membersNotFound", membersNotFound);
    debugMap.put("membersCount", GrouperUtil.length(members));

    if (GrouperUtil.length(members) > 0) {
    
      int globalMaxAllowed = GrouperConfig.retrieveConfig().propertyValueInt("usdu.failsafe.maxUnresolvableSubjects", 500);
      
      boolean globalRemoveUpToFailSafe = GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.failsafe.removeUpToFailsafe", false);
      int deletedCount = 0;
      if (members.size() > globalMaxAllowed) {
        
        if (!globalRemoveUpToFailSafe) {
          debugMap.put("failsafe", "For table "+table+" found " +
              members.size()+" unresolvable members. max limit is "+globalMaxAllowed+". "+
              "removeUpToFailsafe is set to false hence not going to delete any members.");
        } else {
          debugMap.put("failsafe", "For table "+table+" found " +
              members.size()+" unresolvable members. max limit is "+globalMaxAllowed+". "+
              "removeUpToFailsafe is set to true hence going to delete "+globalMaxAllowed+" members.");
          
          deletedCount += UsduJob.deleteUnresolvableMembers(members, globalMaxAllowed);
          
        }
        
      } else {
        deletedCount += UsduJob.deleteUnresolvableMembers(members, members.size());
      }
      debugMap.put("deletedCount", deletedCount);

      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setDeleteCount(deletedCount);
    }
    if (GrouperUtil.length(subjectIds) > 0) {
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(databaseConfigId);
      gcDbAccess.sql("delete from " + table + " where " + column + " = ?");
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      for (String subjectId : subjectIds) {
        batchBindVars.add(GrouperUtil.toList(subjectId));
      }
      gcDbAccess.batchBindVars(batchBindVars);
      int[] results = gcDbAccess.executeBatchSql();
      int deleteCount = 0;
      for (int i=0; i<GrouperUtil.length(results); i++) {
        deleteCount += results[i];
      }
      debugMap.put("deleteFromTableCount", deleteCount);
    }

    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));

//  }
//
//}
```
