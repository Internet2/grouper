---
title: "Grouper automatic group disable if not attested"
space: Grouper
pageId: 28548530
version: 2
lastUpdated: 2026-07-01T05:44:16.911Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548530/Grouper+automatic+group+disable+if+not+attested
---

Simple script to disable groups which have 0 days left in attestation. The disabled group will be deprovisioned from wherever it is provisioned, and those indirect memberships will be removed from other groups (if applicable).

Note, this is just an example, we can make it more sophisticated to give a little bit of a grace period (e.g. X days after there are 0 days left in attestation), or we can send an email out about it, or whatever other features.

Script:

```
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

//public class Test144attestationWithTeeth {
  
//  public static void main(String[] args) {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    try {
  
      List<String> groupNames = new GcDbAccess().sql("""
          select name from grouper_groups gg 
        where exists (select 1 from grouper_aval_asn_asn_group_v AS gaaagv
        where gaaagv.group_name = gg.name and attribute_def_name_name2 = 'etc:attribute:attestation:attestationHasAttestation'
        and value_string = 'true')
        and exists (select 1 from grouper_aval_asn_asn_group_v AS gaaagv
        where gaaagv.group_name = gg.name and attribute_def_name_name2 = 'etc:attribute:attestation:attestationCalculatedDaysLeft'
        and value_string = '0')
          """).selectList(String.class);

      hib3GrouperLoaderLog.addTotalCount(GrouperUtil.length(groupNames));
      
      for (String groupName : groupNames) {
        debugMap.put("group_" + groupName, "disabling");
        new GroupSave().assignGroupNameToEdit(groupName).assignDisabledTimestamp(new Timestamp(System.currentTimeMillis())).save();
        hib3GrouperLoaderLog.addUpdateCount(1);
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMapForLog);
      if (OtherJobScript.retrieveFromThreadLocal() == null) {
        System.out.println(debugMapForLog);
      }
    }
    
//  }

//}

```

Make a script daemon:

See groups disabled if not attested

Group that is disabled and needs attestation

Edit the group to enable it after attesting (take out disabled date)
